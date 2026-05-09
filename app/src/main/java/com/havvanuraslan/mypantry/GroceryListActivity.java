package com.havvanuraslan.mypantry;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GroceryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroceryAdapter adapter;
    private AppDatabase db;
    private List<GroceryItem> groceryList;

    private TextView tvEmptyState;
    private FloatingActionButton fabAdd;
    private ImageButton btnBack;

    private MaterialButton btnClearList, btnMoveToPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        db = AppDatabase.getDbInstance(this.getApplicationContext());

        recyclerView = findViewById(R.id.rvGroceryList);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAdd = findViewById(R.id.fabAddGrocery);
        btnBack = findViewById(R.id.btnBack);

        btnClearList = findViewById(R.id.btnClearList);
        btnMoveToPantry = findViewById(R.id.btnMoveToPantry);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadGroceryList();

        fabAdd.setOnClickListener(v -> showAddGroceryDialog());

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearList != null) {
            btnClearList.setOnClickListener(v -> {
                if (groceryList != null && !groceryList.isEmpty()) {
                    for (GroceryItem item : groceryList) {
                        db.groceryDao().delete(item);
                    }
                    Toast.makeText(this, "List cleared!", Toast.LENGTH_SHORT).show();
                    loadGroceryList();
                }
            });
        }

        if (btnMoveToPantry != null) {
            btnMoveToPantry.setOnClickListener(v -> {
                int movedCount = 0;

                for (GroceryItem item : groceryList) {
                    if (item.isChecked()) {
                        PantryItem newPantryItem = new PantryItem(item.getName(), item.getQuantity(), item.getUnit());
                        db.pantryDao().insert(newPantryItem);

                        db.groceryDao().delete(item);
                        movedCount++;
                    }
                }

                Toast.makeText(this, movedCount + " items moved to Pantry!", Toast.LENGTH_SHORT).show();
                loadGroceryList();
            });
        }
    }

    private void loadGroceryList() {
        groceryList = db.groceryDao().getAllItems();
        checkEmptyState();
        checkMoveButtonState();

        adapter = new GroceryAdapter(groceryList, new GroceryAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                GroceryItem item = groceryList.get(position);
                db.groceryDao().delete(item);
                groceryList.remove(position);
                adapter.notifyItemRemoved(position);
                checkEmptyState();
                checkMoveButtonState();
            }

            @Override
            public void onCheckChanged(int position, boolean isChecked) {
                GroceryItem item = groceryList.get(position);
                item.setChecked(isChecked);
                db.groceryDao().update(item);

                checkMoveButtonState();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void checkMoveButtonState() {
        if (btnMoveToPantry == null || groceryList == null) return;

        boolean hasCheckedItems = false;
        for (GroceryItem item : groceryList) {
            if (item.isChecked()) {
                hasCheckedItems = true;
                break;
            }
        }
        btnMoveToPantry.setEnabled(hasCheckedItems);
    }

    private void showAddGroceryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_grocery, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = view.findViewById(R.id.etGroceryName);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        Spinner spinnerUnit = view.findViewById(R.id.spinnerUnit);

        MaterialButton btnAdd = view.findViewById(R.id.btnAddGrocery);
        TextView btnCancel = view.findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.unit_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerUnit != null) {
            spinnerUnit.setAdapter(spinnerAdapter);
        }

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();

            if (!name.isEmpty() && !qtyStr.isEmpty()) {
                double quantity = Double.parseDouble(qtyStr);
                String selectedUnit = spinnerUnit.getSelectedItem().toString();

                GroceryItem newItem = new GroceryItem(name, quantity, selectedUnit);
                db.groceryDao().insert(newItem);

                loadGroceryList();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter a name and quantity", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void checkEmptyState() {
        if (groceryList == null || groceryList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (btnClearList != null) btnClearList.setVisibility(View.GONE);
            if (btnMoveToPantry != null) btnMoveToPantry.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (btnClearList != null) btnClearList.setVisibility(View.VISIBLE);
            if (btnMoveToPantry != null) btnMoveToPantry.setVisibility(View.VISIBLE);
        }
    }
}