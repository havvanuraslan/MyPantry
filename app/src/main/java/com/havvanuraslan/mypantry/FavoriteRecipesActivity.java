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

    // Animasyon bileşenleri
    private LinearLayout layoutLoading;
    private LottieAnimationView lottieLoader;
    private LottieAnimationView lottieEmpty;

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
        layoutLoading = findViewById(R.id.layoutLoadingFav);
        lottieLoader = findViewById(R.id.lottieLoaderFav);
        lottieEmpty = findViewById(R.id.lottieEmpty);
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
        if (fullFavList.isEmpty()) {
            layoutLoading.setVisibility(View.VISIBLE);
            if (lottieLoader != null) lottieLoader.playAnimation();
            rvFavorites.setVisibility(View.GONE);
        }
        layoutEmptyState.setVisibility(View.GONE);
        if (lottieEmpty != null) lottieEmpty.cancelAnimation();

        executorService.execute(() -> {
            try {
                final List<Recipe_Entity> favorites = recipeDao.getFavoriteRecipes();

                if (favorites == null || favorites.isEmpty()) {
                    mainHandler.post(() -> {
                        fullFavList.clear();
                        if (adapter != null) adapter.updateList(new ArrayList<>());

                        hideLoadingAnimation();
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        if (lottieEmpty != null) {
                            lottieEmpty.setVisibility(View.VISIBLE);
                            lottieEmpty.playAnimation();
                        }
                        rvFavorites.setVisibility(View.GONE);
                    });
                    return;
                }

                mainHandler.post(() -> {
                    if (fullFavList.isEmpty()) {
                        List<Recommendation_Engine.RecipeScore> temporaryList = new ArrayList<>();
                        for (Recipe_Entity r : favorites) {
                            temporaryList.add(new Recommendation_Engine.RecipeScore(r, 0.0));
                        }
                        setupOrUpdateAdapter(temporaryList);
                    }
                });

                List<PantryItem> pantryItems = pantryDb.pantryDao().getAllItems();
                List<String> ingredientNames = new ArrayList<>();
                for (PantryItem item : pantryItems) {
                    ingredientNames.add(item.getName());
                }

                mainHandler.post(() -> {
                    aiEngine.calculateScoresForSpecificList(this, favorites, ingredientNames, new Recommendation_Engine.OnRecommendationsReady() {
                        @Override
                        public void onSuccess(List<Recommendation_Engine.RecipeScore> data) {
                            fullFavList = data;
                            hideLoadingAnimation();
                            setupOrUpdateAdapter(data);
                        }

                        @Override
                        public void onError(String error) {
                            hideLoadingAnimation();
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
        if (list == null || list.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            if (lottieEmpty != null) lottieEmpty.playAnimation();
            rvFavorites.setVisibility(View.GONE);
            return;
        }

        layoutEmptyState.setVisibility(View.GONE);
        if (lottieEmpty != null) lottieEmpty.cancelAnimation();
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

                Recommendation_Engine.RecipeScore targetToRemove = null;
                for (Recommendation_Engine.RecipeScore scoreItem : fullFavList) {
                    if (scoreItem.recipe.id == recipe.id) {
                        targetToRemove = scoreItem;
                        break;
                    }
                }

                if (targetToRemove != null) {
                    fullFavList.remove(targetToRemove);
                }

                setupOrUpdateAdapter(fullFavList);
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