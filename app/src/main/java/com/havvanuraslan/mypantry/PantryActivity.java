package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView; // Spinner yerine EKLENDİ
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout; // EKLENDİ

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

        adapter = new PantryAdapter(this, items);

        adapter.setListener(new PantryAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                PantryItem itemToDelete = items.get(position);

                SharedPreferences sharedPrefs = getSharedPreferences("PantryExpiryPrefs", Context.MODE_PRIVATE);
                sharedPrefs.edit().remove(itemToDelete.getName() + "_expiry").apply();

                db.pantryDao().delete(itemToDelete);
                loadPantryList();
                Toast.makeText(PantryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onIncrease(int position) {
                PantryItem item = items.get(position);
                double step = getStepSize(item.getUnit());
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

                if (newQty > 0) {
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
        EditText etExpiryDate = view.findViewById(R.id.etExpiryDate);

        // 🌟 DÜZELTİLDİ: Spinner yerine AutoCompleteTextView casting işlemi yapıldı
        AutoCompleteTextView spinnerUnit = view.findViewById(R.id.spinnerUnit);

        Button btnAdd = view.findViewById(R.id.btnAdd);
        TextView btnCancel = view.findViewById(R.id.btnCancel);

        // 🌟 DÜZELTİLDİ: Material 3 Exposed Dropdown şablonuna uygun ArrayAdapter
        if (spinnerUnit != null) {
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.unit_options,
                    android.R.layout.simple_dropdown_item_1line
            );
            spinnerUnit.setAdapter(spinnerAdapter);
        }

        // 🌟 DÜZELTİLDİ: Hem kutucuk hem de sağdaki takvim simgesi için ortak tetikleyici
        if (etExpiryDate != null) {
            View.OnClickListener showDatePicker = v -> {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                        PantryActivity.this,
                        (view1, selectedYear, selectedMonth, selectedDay) -> {
                            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                    selectedYear, (selectedMonth + 1), selectedDay);
                            etExpiryDate.setText(formattedDate);
                        },
                        year, month, day
                );

                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            };

            // Yazı alanına tıklandığında takvimi aç
            etExpiryDate.setOnClickListener(showDatePicker);

            // Sağdaki takvim simgesine (End Icon) tıklandığında da takvimi aç
            View parentLayout = (View) etExpiryDate.getParent().getParent();
            if (parentLayout instanceof TextInputLayout) {
                ((TextInputLayout) parentLayout).setEndIconOnClickListener(showDatePicker);
            }
        }

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQty.getText().toString().trim();
            String expiryDate = etExpiryDate != null ? etExpiryDate.getText().toString().trim() : "";

            if (!name.isEmpty() && !qtyStr.isEmpty() && !expiryDate.isEmpty()) {
                double quantity = Double.parseDouble(qtyStr);

                // AutoCompleteTextView değerini güvenle string'e döküyoruz
                String selectedUnit = spinnerUnit != null ? spinnerUnit.getText().toString() : "";

                PantryItem newItem = new PantryItem(name, quantity, selectedUnit);
                db.pantryDao().insert(newItem);

                SharedPreferences sharedPrefs = getSharedPreferences("PantryExpiryPrefs", Context.MODE_PRIVATE);
                sharedPrefs.edit().putString(name + "_expiry", expiryDate).apply();

                loadPantryList();
                dialog.dismiss();
                Toast.makeText(this, "Item Added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please fill all fields, including Expiry Date", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}