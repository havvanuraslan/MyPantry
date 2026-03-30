package com.havvanuraslan.mypantry;

import android.os.Bundle;
import android.view.View;
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

import java.util.List;

public class PantryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private AppDatabase db;
    private List<PantryItem> items; // Listeyi sınıf seviyesinde tutuyoruz

    // UI Elemanları
    private FloatingActionButton fabAdd;
    private TextView tvEmptyState;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        // 1. Veritabanını Başlat
        db = AppDatabase.getDbInstance(this.getApplicationContext());

        // 2. UI Bağlantıları
        recyclerView = findViewById(R.id.rvPantryList);
        fabAdd = findViewById(R.id.fabAdd);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);

        // 3. Listeyi Kur
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadPantryList();

        // 4. Buton Tıklamaları
        fabAdd.setOnClickListener(v -> showAddItemDialog());
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadPantryList() {
        // Verileri çekip sınıf seviyesindeki değişkene atıyoruz
        items = db.pantryDao().getAllItems();

        // Boş Liste Kontrolü
        if (items.isEmpty()) {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        // --- DÜZELTİLEN KISIM: Adapter Bağlantısı ---
        adapter = new PantryAdapter(items, new PantryAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                // Pozisyona göre öğeyi bul ve sil
                PantryItem itemToDelete = items.get(position);
                db.pantryDao().delete(itemToDelete);
                loadPantryList(); // Listeyi yenile
                Toast.makeText(PantryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onIncrease(int position) {
                // Miktarı artır
                PantryItem item = items.get(position);
                item.quantity += 1; // Miktarı 1 artır
                db.pantryDao().update(item); // Veritabanını güncelle
                loadPantryList(); // Listeyi yenile
            }

            @Override
            public void onDecrease(int position) {
                // Miktarı azalt
                PantryItem item = items.get(position);
                if (item.quantity > 1) {
                    item.quantity -= 1; // Miktarı 1 azalt
                    db.pantryDao().update(item);
                    loadPantryList();
                } else {
                    // Miktar 1 ise kullanıcıya silmek ister misin diye sorabiliriz veya direkt silebiliriz
                    // Şimdilik 1'in altına düşürmeyelim
                    Toast.makeText(PantryActivity.this, "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(adapter);
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
        Button btnAdd = view.findViewById(R.id.btnAdd);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQty.getText().toString().trim();

            if (!name.isEmpty() && !qtyStr.isEmpty()) {
                PantryItem newItem = new PantryItem(name, Integer.parseInt(qtyStr));
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