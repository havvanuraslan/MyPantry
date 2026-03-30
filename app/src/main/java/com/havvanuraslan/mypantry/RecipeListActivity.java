package com.havvanuraslan.mypantry;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    private LottieAnimationView lottieAnimation;
    private TextView tvError;
    private EditText etSearch;
    private ImageButton btnBack;

    // YENİ EKLENEN KUTUMUZ (Animasyon + Yazı)
    private LinearLayout layoutLoading;

    private Recommendation_Engine aiEngine;
    private AppDatabase db;

    private List<Recommendation_Engine.RecipeScore> fullRecipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Başlatmalar
        aiEngine = new Recommendation_Engine();
        db = AppDatabase.getDbInstance(getApplicationContext());

        // UI Bağlantıları
        recyclerView = findViewById(R.id.rvRecipeList);
        lottieAnimation = findViewById(R.id.lottieAnimation);
        tvError = findViewById(R.id.tvError);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        layoutLoading = findViewById(R.id.layoutLoading); // Kutu bağlandı

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Akıllı Aramayı Başlat
        loadSmartRecipes();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // --- ARAMA KUTUSU ---
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadSmartRecipes() {
        // Animasyonu başlat ve İÇİNDE YAZI OLAN KUTUYU (layoutLoading) göster
        layoutLoading.setVisibility(View.VISIBLE);
        lottieAnimation.playAnimation();
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        // --- ÇÖKMEYİ ENGELLEYEN KISIM: Veritabanını Arka Planda Okuyoruz ---
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Kilerdeki malzemeleri arka planda çek
                List<PantryItem> pantryItems = db.pantryDao().getAllItems();
                List<String> ingredientNames = new ArrayList<>();

                for (PantryItem item : pantryItems) {
                    ingredientNames.add(item.getName());
                }

                // UI (Arayüz) işlemlerini yapmak için Ana Thread'e dön
                mainHandler.post(() -> {
                    if (ingredientNames.isEmpty()) {
                        lottieAnimation.cancelAnimation();
                        layoutLoading.setVisibility(View.GONE); // DİKKAT: Sadece animasyonu değil, tüm kutuyu gizledik!
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Your pantry is empty! Add items first.");
                        return;
                    }

                    // Kiler doluysa YAPAY ZEKA MOTORUNDAN ÖNERİLERİ AL
                    aiEngine.findBestRecipes(RecipeListActivity.this, ingredientNames, 20, new Recommendation_Engine.OnRecommendationsReady() {
                        @Override
                        public void onSuccess(List<Recommendation_Engine.RecipeScore> data) {
                            lottieAnimation.cancelAnimation();
                            layoutLoading.setVisibility(View.GONE); // DİKKAT: Kutuyu gizledik!
                            tvError.setVisibility(View.GONE);

                            if (data == null || data.isEmpty()) {
                                tvError.setVisibility(View.VISIBLE);
                                tvError.setText("No matching recipes found for your ingredients.");
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                setupAdapter(data);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            lottieAnimation.cancelAnimation();
                            layoutLoading.setVisibility(View.GONE); // DİKKAT: Kutuyu gizledik!
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText("AI Error: " + error);
                        }
                    });
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    lottieAnimation.cancelAnimation();
                    layoutLoading.setVisibility(View.GONE); // DİKKAT: Kutuyu gizledik!
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Database Error: " + e.getMessage());
                });
            }
        });
    }

    private void setupAdapter(List<Recommendation_Engine.RecipeScore> recipes) {
        fullRecipeList = new ArrayList<>(recipes);

        adapter = new RecipeAdapter(this, recipes, new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(int recipeId) {
                Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", recipeId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void filter(String text) {
        if (fullRecipeList == null || fullRecipeList.isEmpty()) return;

        List<Recommendation_Engine.RecipeScore> filteredList = new ArrayList<>();
        String searchText = text.toLowerCase().trim();

        for (Recommendation_Engine.RecipeScore item : fullRecipeList) {
            if (item.recipe.name != null && item.recipe.name.toLowerCase().contains(searchText)) {
                filteredList.add(item);
            }
        }

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }
}