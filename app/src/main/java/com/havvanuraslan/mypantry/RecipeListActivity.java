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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private LottieAnimationView lottieAnimation;
    private TextView tvError;
    private EditText etSearch;
    private ChipGroup chipGroupTags;
    private LinearLayout layoutLoading;

    private Recommendation_Engine aiEngine;
    private AppDatabase db;
    private Recipe_Dao recipeDao;
    private FirebaseAuth mAuth;

    private List<Recommendation_Engine.RecipeScore> fullRecipeList = new ArrayList<>();
    private String currentSearchText = "";
    private String currentSelectedTag = "";

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Initialization
        aiEngine = new Recommendation_Engine();
        db = AppDatabase.getDbInstance(getApplicationContext());
        recipeDao = Recipe_Database.getDbInstance(getApplicationContext()).recipeDao();
        mAuth = FirebaseAuth.getInstance();

        // UI Bindings
        recyclerView = findViewById(R.id.rvRecipeList);
        lottieAnimation = findViewById(R.id.lottieAnimation);
        tvError = findViewById(R.id.tvError);
        etSearch = findViewById(R.id.etSearch);
        layoutLoading = findViewById(R.id.layoutLoading);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Start AI Recommendation
        loadRecipes();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchText = s.toString().toLowerCase().trim();
                    applyFilters();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!fullRecipeList.isEmpty()) {
            executorService.execute(() -> {
                boolean changed = false;
                for (Recommendation_Engine.RecipeScore item : fullRecipeList) {
                    Recipe_Entity dbRecipe = recipeDao.getRecipeById(item.recipe.id);
                    if (dbRecipe != null) {
                        int dbFavStatus = (dbRecipe.favorite_recipe != null) ? dbRecipe.favorite_recipe : 0;
                        int currentStatus = (item.recipe.favorite_recipe != null) ? item.recipe.favorite_recipe : 0;
                        if (dbFavStatus != currentStatus) {
                            item.recipe.favorite_recipe = dbFavStatus;
                            item.recipe.isFavorite = (dbFavStatus == 1);
                            changed = true;
                        }
                    }
                }
                if (changed) mainHandler.post(this::applyFilters);
            });
        }
    }

    private void loadRecipes() {
        layoutLoading.setVisibility(View.VISIBLE);
        lottieAnimation.playAnimation();
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        executorService.execute(() -> {
            try {
                List<PantryItem> pantryItems = db.pantryDao().getAllItems();
                List<String> ingredientNames = new ArrayList<>();
                for (PantryItem item : pantryItems) {
                    ingredientNames.add(item.getName());
                }

                mainHandler.post(() -> {
                    if (ingredientNames.isEmpty()) {
                        showError("Your pantry is empty! Add items first to see recommendations.");
                        return;
                    }

                    aiEngine.findBestRecipes(this, ingredientNames, 30, new Recommendation_Engine.OnRecommendationsReady() {
                        @Override
                        public void onSuccess(List<Recommendation_Engine.RecipeScore> data) {
                            handleRecipeData(data);
                        }

                        @Override
                        public void onError(String error) {
                            showError("AI Recommendation Error: " + error);
                        }
                    });
                });
            } catch (Exception e) {
                mainHandler.post(() -> showError("Database Error: " + e.getMessage()));
            }
        });
    }

    private void handleRecipeData(List<Recommendation_Engine.RecipeScore> data) {
        lottieAnimation.cancelAnimation();
        layoutLoading.setVisibility(View.GONE);

        if (data == null || data.isEmpty()) {
            showError("No recipes found for your pantry items.");
            return;
        }

        for (Recommendation_Engine.RecipeScore item : data) {
            item.recipe.isFavorite = (item.recipe.favorite_recipe != null && item.recipe.favorite_recipe == 1);
        }

        fullRecipeList = new ArrayList<>(data);
        recyclerView.setVisibility(View.VISIBLE);
        if (chipGroupTags != null) chipGroupTags.setVisibility(View.VISIBLE);

        setupAdapter(fullRecipeList);
        setupTagsUI(fullRecipeList);
        applyFilters();
    }

    private void setupAdapter(List<Recommendation_Engine.RecipeScore> recipes) {
        adapter = new RecipeAdapter(this, new ArrayList<>(recipes), new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(int recipeId) {
                Intent intent = new Intent(RecipeListActivity.this, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", recipeId);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Recipe_Entity recipe) {
                saveFavoriteChange(recipe);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void saveFavoriteChange(Recipe_Entity recipe) {
        executorService.execute(() -> {
            recipeDao.updateFavoriteStatus(recipe.id, recipe.favorite_recipe);
        });

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference favRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("favorites")
                    .child(String.valueOf(recipe.id));

            if (recipe.isFavorite) favRef.setValue(true);
            else favRef.removeValue();
        }

        String msg = recipe.isFavorite ? "Added to favorites" : "Removed from favorites";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        applyFilters();
    }

    private void applyFilters() {
        if (fullRecipeList == null) return;

        List<Recommendation_Engine.RecipeScore> filteredList = new ArrayList<>();
        for (Recommendation_Engine.RecipeScore item : fullRecipeList) {
            boolean matchesSearch = currentSearchText.isEmpty() ||
                    (item.recipe.name != null && item.recipe.name.toLowerCase().contains(currentSearchText));

            boolean matchesTag = currentSelectedTag.isEmpty() ||
                    (item.recipe.tags != null && item.recipe.tags.toLowerCase().contains(currentSelectedTag));

            if (matchesSearch && matchesTag) {
                filteredList.add(item);
            }
        }

        if (adapter != null) {
            adapter.updateList(filteredList);
            if (filteredList.isEmpty()) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("No results found matching your search/filters.");
            } else {
                tvError.setVisibility(View.GONE);
            }
        }
    }

    private void setupTagsUI(List<Recommendation_Engine.RecipeScore> recipes) {
        if (chipGroupTags == null) return;
        chipGroupTags.removeAllViews();
        List<String> dynamicTags = extractUniqueTags(recipes);

        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(currentSelectedTag.isEmpty());
        allChip.setOnCheckedChangeListener((v, isChecked) -> {
            if(isChecked) {
                currentSelectedTag = "";
                applyFilters();
            }
        });
        chipGroupTags.addView(allChip);

        for (String tag : dynamicTags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChecked(tag.equals(currentSelectedTag));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentSelectedTag = tag;
                } else if (currentSelectedTag.equals(tag)) {
                    currentSelectedTag = "";
                }
                applyFilters();
            });
            chipGroupTags.addView(chip);
        }
    }

    private List<String> extractUniqueTags(List<Recommendation_Engine.RecipeScore> recipes) {
        Map<String, Integer> tagCounts = new HashMap<>();
        List<String> ignoredTags = Arrays.asList("time-to-make", "course", "main-ingredient", "preparation", "number-of-servings", "dietary", "equipment", "for-1-or-2");

        for (Recommendation_Engine.RecipeScore item : recipes) {
            if (item.recipe.tags != null) {
                String[] tags = item.recipe.tags.split(",");
                for (String tag : tags) {
                    String cleanTag = tag.trim().toLowerCase();
                    if (!cleanTag.isEmpty() && !ignoredTags.contains(cleanTag)) {
                        tagCounts.put(cleanTag, tagCounts.getOrDefault(cleanTag, 0) + 1);
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> sortedTags = new ArrayList<>(tagCounts.entrySet());
        sortedTags.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> finalTags = new ArrayList<>();
        int limit = Math.min(10, sortedTags.size());
        for (int i = 0; i < limit; i++) finalTags.add(sortedTags.get(i).getKey());
        return finalTags;
    }

    private void showError(String message) {
        lottieAnimation.cancelAnimation();
        layoutLoading.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(message);
        recyclerView.setVisibility(View.GONE);
        if (chipGroupTags != null) chipGroupTags.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}