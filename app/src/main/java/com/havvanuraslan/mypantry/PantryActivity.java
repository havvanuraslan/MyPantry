package com.havvanuraslan.mypantry;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter; // EKLENDİ
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner; // EKLENDİ
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PantryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private AppDatabase db;
    private List<PantryItem> items;

    private FloatingActionButton fabAdd;
    private TextView tvEmptyState;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        db = AppDatabase.getDbInstance(this.getApplicationContext());

        recyclerView = findViewById(R.id.rvPantryList);
        fabAdd = findViewById(R.id.fabAdd);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadPantryList();

        fabAdd.setOnClickListener(v -> showAddItemDialog());
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadPantryList() {
        items = db.pantryDao().getAllItems();

        if (items.isEmpty()) {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new PantryAdapter(items, new PantryAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                PantryItem itemToDelete = items.get(position);
                db.pantryDao().delete(itemToDelete);
                loadPantryList();
                Toast.makeText(PantryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onIncrease(int position) {
                PantryItem item = items.get(position);
                double step = getStepSize(item.getUnit());

                // Add the step and round to 1 decimal place to prevent floating-point errors (e.g., 3.10000000000004)
                double newQty = Math.round((item.getQuantity() + step) * 10.0) / 10.0;

                item.setQuantity(newQty);
                db.pantryDao().update(item);
                loadPantryList();
            }

            @Override
            public void onDecrease(int position) {
                PantryItem item = items.get(position);
                double step = getStepSize(item.getUnit());

                double newQty = Math.round((item.getQuantity() - step) * 10.0) / 10.0;

                if (newQty > 0) { // En az 0.1 veya 1.0'a kadar düşebilmeli
                    item.setQuantity(newQty);
                    db.pantryDao().update(item);
                    loadPantryList();
                } else {
                    Toast.makeText(PantryActivity.this, "Minimum quantity reached", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private double getStepSize(String unit) {
        if (unit == null) return 1.0;

        if (unit.contains("pcs") || unit.contains("Package") || unit.contains("Bunch")) {
            return 1.0;
        }
        return 0.1;
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = view.findViewById(R.id.etItemName);
        EditText etQty = view.findViewById(R.id.etQuantity);
        Spinner spinnerUnit = view.findViewById(R.id.spinnerUnit);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        TextView btnCancel = view.findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.unit_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(spinnerAdapter);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQty.getText().toString().trim();

            if (!name.isEmpty() && !qtyStr.isEmpty()) {
                double quantity = Double.parseDouble(qtyStr);
                String selectedUnit = spinnerUnit.getSelectedItem().toString();

                PantryItem newItem = new PantryItem(name, quantity, selectedUnit);

                db.pantryDao().insert(newItem);
                loadPantryList();
                dialog.dismiss();
                Toast.makeText(this, "Item Added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}