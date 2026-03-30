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

import java.util.ArrayList;
import java.util.List;

public class GroceryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroceryAdapter adapter;
    private AppDatabase db;
    private List<GroceryItem> groceryList;

    // UI Elemanları
    private TextView tvEmptyState;
    private FloatingActionButton fabAdd;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        // 1. Veritabanını Başlat (SharedPreferences yerine Room kullanıyoruz)
        db = AppDatabase.getDbInstance(this.getApplicationContext());

        // 2. UI Bağlantıları
        recyclerView = findViewById(R.id.rvGroceryList);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAdd = findViewById(R.id.fabAddGrocery);
        btnBack = findViewById(R.id.btnBack);

        // 3. Listeyi Kur
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadGroceryList();

        // 4. Buton Tıklamaları
        fabAdd.setOnClickListener(v -> showAddGroceryDialog()); // Özel tasarım diyaloğu açar

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadGroceryList() {
        // Veritabanından verileri çek
        groceryList = db.groceryDao().getAllItems();

        // Boş durum kontrolü
        checkEmptyState();

        // Adapter ayarları
        adapter = new GroceryAdapter(groceryList, new GroceryAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                // Listeden sil
                GroceryItem item = groceryList.get(position);
                db.groceryDao().delete(item); // Veritabanından sil
                groceryList.remove(position); // Listeden sil
                adapter.notifyItemRemoved(position); // Arayüzü güncelle
                checkEmptyState();
            }

            @Override
            public void onCheckChanged(int position, boolean isChecked) {
                // Checkbox durumunu güncelle
                GroceryItem item = groceryList.get(position);
                item.setChecked(isChecked);
                db.groceryDao().update(item); // Veritabanını güncelle
                // notifyItemChanged yapmıyoruz ki animasyon bozulmasın
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showAddGroceryDialog() {
        // --- ÖZEL TASARIM KISMI ---
        // Standart Builder yerine özel layout kullanıyoruz (Pantry'deki gibi)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_grocery, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        // Köşelerin oval görünmesi için arka planı şeffaf yapıyoruz
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Tasarım içindeki elemanları bul
        EditText etName = view.findViewById(R.id.etGroceryName);
        Button btnAdd = view.findViewById(R.id.btnAddGrocery);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                // Yeni ürünü oluştur ve kaydet
                GroceryItem newItem = new GroceryItem(name);
                db.groceryDao().insert(newItem);

                // Listeyi yenile
                loadGroceryList();

                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void checkEmptyState() {
        if (groceryList == null || groceryList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}