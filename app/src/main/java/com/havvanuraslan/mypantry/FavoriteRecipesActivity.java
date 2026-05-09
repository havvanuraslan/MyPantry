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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteRecipesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private RecipeAdapter adapter;
    private EditText etSearch;
    private LinearLayout layoutEmptyState;

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

        rvFavorites = findViewById(R.id.rvFavorites);
        etSearch = findViewById(R.id.etSearchFav);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        ImageButton btnBack = findViewById(R.id.btnBack);

        recipeDao = Recipe_Database.getDbInstance(this).recipeDao();
        pantryDb = AppDatabase.getDbInstance(this);
        aiEngine = new Recommendation_Engine();

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterFavorites(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadFavoritesWithScores();
    }

    private void loadFavoritesWithScores() {
        executorService.execute(() -> {
            try {
                List<Recipe_Entity> favorites = recipeDao.getFavoriteRecipes();

                if (favorites.isEmpty()) {
                    mainHandler.post(() -> {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    });
                    return;
                }

                List<PantryItem> pantryItems = pantryDb.pantryDao().getAllItems();
                List<String> ingredientNames = new ArrayList<>();
                for (PantryItem item : pantryItems) {
                    ingredientNames.add(item.getName());
                }

                aiEngine.calculateScoresForSpecificList(this, favorites, ingredientNames, new Recommendation_Engine.OnRecommendationsReady() {
                    @Override
                    public void onSuccess(List<Recommendation_Engine.RecipeScore> data) {
                        mainHandler.post(() -> {
                            fullFavList = data;
                            setupAdapter(data);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        mainHandler.post(() -> Toast.makeText(FavoriteRecipesActivity.this, "AI Error: " + error, Toast.LENGTH_SHORT).show());
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(FavoriteRecipesActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupAdapter(List<Recommendation_Engine.RecipeScore> list) {
        layoutEmptyState.setVisibility(View.GONE);
        rvFavorites.setVisibility(View.VISIBLE);

        adapter = new RecipeAdapter(this, list, new RecipeAdapter.OnRecipeClickListener() {
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
    }

    private void handleFavoriteToggle(Recipe_Entity recipe) {
        executorService.execute(() -> {
            recipeDao.updateFavoriteStatus(recipe.id, 0);

            mainHandler.post(() -> {
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                loadFavoritesWithScores();
            });
        });
    }

    private void filterFavorites(String text) {
        if (fullFavList == null) return;

        List<Recommendation_Engine.RecipeScore> filtered = new ArrayList<>();
        for (Recommendation_Engine.RecipeScore item : fullFavList) {
            if (item.recipe.name.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(item);
            }
        }
        if (adapter != null) adapter.updateList(filtered);
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