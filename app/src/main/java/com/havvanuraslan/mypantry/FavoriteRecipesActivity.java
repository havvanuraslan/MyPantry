package com.havvanuraslan.mypantry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteRecipesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private RecipeAdapter adapter;
    private EditText etSearch;
    private LinearLayout layoutEmptyState;

    // Yeni eklenen animasyon bileşenleri
    private LinearLayout layoutLoading;
    private LottieAnimationView lottieLoader;

    private Recipe_Dao recipeDao;
    private AppDatabase pantryDb;
    private Recommendation_Engine aiEngine;

    private List<Recommendation_Engine.RecipeScore> fullFavList = new ArrayList<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_recipes);

        // UI Bindings
        rvFavorites = findViewById(R.id.rvFavorites);
        etSearch = findViewById(R.id.etSearchFav);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutLoading = findViewById(R.id.layoutLoadingFav); // XML'deki yeni ID
        lottieLoader = findViewById(R.id.lottieLoaderFav);   // XML'deki yeni ID
        ImageButton btnBack = findViewById(R.id.btnBack);

        recipeDao = Recipe_Database.getDbInstance(this).recipeDao();
        pantryDb = AppDatabase.getDbInstance(this);
        aiEngine = new Recommendation_Engine();

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFavorites(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadFavoritesWithScores();
    }

    private void loadFavoritesWithScores() {
        // Sayfa yüklenmeye başlarken animasyonları görünür yap, eski listeyi gizle
        if (fullFavList.isEmpty()) {
            layoutLoading.setVisibility(View.VISIBLE);
            if (lottieLoader != null) lottieLoader.playAnimation();
            rvFavorites.setVisibility(View.GONE);
        }
        layoutEmptyState.setVisibility(View.GONE);

        executorService.execute(() -> {
            try {
                // 1. AŞAMA: Favorileri SQLite'tan çek
                final List<Recipe_Entity> favorites = recipeDao.getFavoriteRecipes();

                if (favorites == null || favorites.isEmpty()) {
                    mainHandler.post(() -> {
                        fullFavList.clear();
                        if (adapter != null) adapter.updateList(new ArrayList<>());

                        // Favori yoksa animasyonu durdur ve boş durum ekranını aç
                        hideLoadingAnimation();
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    });
                    return;
                }

                // AI hesaplamasını beklemeden kullanıcıya listeyi önbellekten (0.0 skorla) anında göster
                mainHandler.post(() -> {
                    if (fullFavList.isEmpty()) {
                        List<Recommendation_Engine.RecipeScore> temporaryList = new ArrayList<>();
                        for (Recipe_Entity r : favorites) {
                            temporaryList.add(new Recommendation_Engine.RecipeScore(r, 0.0));
                        }
                        setupOrUpdateAdapter(temporaryList);
                    }
                });

                // 2. AŞAMA: Arka planda kileri al ve AI skorlarını hesapla
                List<PantryItem> pantryItems = pantryDb.pantryDao().getAllItems();
                List<String> ingredientNames = new ArrayList<>();
                for (PantryItem item : pantryItems) {
                    ingredientNames.add(item.getName());
                }

                // AI motorunu tetikle, bitince skorları pürüzsüzce güncelleyecek
                mainHandler.post(() -> {
                    aiEngine.calculateScoresForSpecificList(this, favorites, ingredientNames, new Recommendation_Engine.OnRecommendationsReady() {
                        @Override
                        public void onSuccess(List<Recommendation_Engine.RecipeScore> data) {
                            fullFavList = data;

                            // Yükleme başarıyla tamamlandı, animasyonu gizle ve listeyi tazele
                            hideLoadingAnimation();
                            setupOrUpdateAdapter(data);
                        }

                        @Override
                        public void onError(String error) {
                            hideLoadingAnimation();
                            // Hata durumunda da önbellekteki liste (0.0 skorlu olan) ekranda kalmaya devam eder
                            Toast.makeText(FavoriteRecipesActivity.this, "AI Engine Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    hideLoadingAnimation();
                    Toast.makeText(FavoriteRecipesActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupOrUpdateAdapter(List<Recommendation_Engine.RecipeScore> list) {
        layoutEmptyState.setVisibility(View.GONE);
        rvFavorites.setVisibility(View.VISIBLE);

        if (adapter == null) {
            adapter = new RecipeAdapter(this, new ArrayList<>(list), new RecipeAdapter.OnRecipeClickListener() {
                @Override
                public void onRecipeClick(int recipeId) {
                    Intent intent = new Intent(FavoriteRecipesActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("RECIPE_ID", recipeId);
                    startActivity(intent);
                }

                @Override
                public void onFavoriteClick(Recipe_Entity recipe) {
                    handleFavoriteToggle(recipe);
                }
            });
            rvFavorites.setAdapter(adapter);
        } else {
            adapter.updateList(list);
        }
    }

    private void handleFavoriteToggle(Recipe_Entity recipe) {
        executorService.execute(() -> {
            recipeDao.updateFavoriteStatus(recipe.id, 0);

            mainHandler.post(() -> {
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                // Favoriden eleman silinirken fullFavList tamamen boşalmasın diye sadece bu fonksiyonu çağırıyoruz
                loadFavoritesWithScores();
            });
        });
    }

    private void filterFavorites(String text) {
        if (fullFavList == null) return;

        List<Recommendation_Engine.RecipeScore> filtered = new ArrayList<>();
        for (Recommendation_Engine.RecipeScore item : fullFavList) {
            if (item.recipe.name != null && item.recipe.name.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(item);
            }
        }
        if (adapter != null) adapter.updateList(filtered);
    }

    /**
     * Lottie yükleme animasyonunu güvenli bir şekilde durdurur ve gizler.
     */
    private void hideLoadingAnimation() {
        if (lottieLoader != null) {
            lottieLoader.cancelAnimation();
        }
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritesWithScores();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}