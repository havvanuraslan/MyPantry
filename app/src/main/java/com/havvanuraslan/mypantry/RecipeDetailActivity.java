package com.havvanuraslan.mypantry;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView ivRecipeImage;
    private TextView tvRecipeName, tvCategory, tvIngredients, tvInstructions;
    private LinearLayout llSubstitutionTip;
    private TextView tvSubstitutionText;
    private ProgressBar progressBar;
    private ImageButton btnBack, btnFavorite;

    private Recipe_Dao recipeDao;
    private AppDatabase db;
    private SubstitutionEngine subEngine;
    private Recipe_Entity currentRecipe;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Initialization
        mAuth = FirebaseAuth.getInstance();
        recipeDao = Recipe_Database.getDbInstance(getApplicationContext()).recipeDao();
        db = AppDatabase.getDbInstance(getApplicationContext());
        subEngine = new SubstitutionEngine();

        // UI Bindings
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvCategory = findViewById(R.id.tvCategory);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvInstructions = findViewById(R.id.tvInstructions);
        llSubstitutionTip = findViewById(R.id.llSubstitutionTip);
        tvSubstitutionText = findViewById(R.id.tvSubstitutionText);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        if (recipeId != -1) {
            fetchRecipeDetails(recipeId);
        } else {
            Toast.makeText(this, "Error: No Recipe ID found", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> toggleFavorite());
        }
    }

    private void fetchRecipeDetails(int recipeId) {
        progressBar.setVisibility(View.VISIBLE);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                currentRecipe = recipeDao.getRecipeById(recipeId);

                // Initialize isFavorite based on SQLite's favorite_recipe (1 = true, 0 = false)
                if (currentRecipe != null) {
                    currentRecipe.isFavorite = (currentRecipe.favorite_recipe != null && currentRecipe.favorite_recipe == 1);
                }

                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (currentRecipe != null) {
                        displayRecipe(currentRecipe);
                        updateFavoriteIcon();
                    } else {
                        Toast.makeText(this, "Recipe details could not be loaded", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void toggleFavorite() {
        if (currentRecipe == null) return;

        currentRecipe.isFavorite = !currentRecipe.isFavorite;
        currentRecipe.favorite_recipe = currentRecipe.isFavorite ? 1 : 0;

        updateFavoriteIcon();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            recipeDao.updateFavoriteStatus(currentRecipe.id, currentRecipe.favorite_recipe);
        });

        saveFavoriteToFirebase(currentRecipe);

        String msg = currentRecipe.isFavorite ? "Added to favorites" : "Removed from favorites";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteIcon() {
        if (btnFavorite != null && currentRecipe != null) {
            btnFavorite.setImageResource(currentRecipe.isFavorite ?
                    android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
    }

    private void saveFavoriteToFirebase(Recipe_Entity recipe) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference favRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("favorites")
                    .child(String.valueOf(recipe.id));

            if (recipe.isFavorite) {
                favRef.setValue(true);
            } else {
                favRef.removeValue();
            }
        }
    }

    private void displayRecipe(Recipe_Entity recipe) {
        tvRecipeName.setText(capitalizeWords(recipe.name));
        String info = "⏱️ Ready in " + recipe.minutes + " minutes\nTags: " +
                (recipe.tags != null ? recipe.tags : "General");
        tvCategory.setText(info);

        if (ivRecipeImage != null) {
            int targetDrawableId = R.drawable.pantry_spices;

            StringBuilder sourceBuilder = new StringBuilder();
            if (recipe.ingredients != null && !recipe.ingredients.isEmpty()) {
                sourceBuilder.append(recipe.ingredients.toLowerCase()).append(" ");
            }
            if (recipe.name != null && !recipe.name.isEmpty()) {
                sourceBuilder.append(recipe.name.toLowerCase());
            }
            String sourceText = sourceBuilder.toString();

            if (!sourceText.isEmpty()) {
                if (sourceText.contains("coffee") || sourceText.contains("tea") || sourceText.contains("juice") || sourceText.contains("milkshake") || sourceText.contains("drink") || sourceText.contains("smoothie")) {
                    targetDrawableId = R.drawable.drinks;
                } else if (sourceText.contains("chocolate") || sourceText.contains("sugar") || sourceText.contains("cocoa") || sourceText.contains("cake") || sourceText.contains("cookie") || sourceText.contains("pudding") || sourceText.contains("dessert")) {
                    targetDrawableId = R.drawable.desserts;
                } else if (sourceText.contains("fish") || sourceText.contains("shrimp") || sourceText.contains("salmon") || sourceText.contains("tuna") || sourceText.contains("seafood")) {
                    targetDrawableId = R.drawable.seafood;
                } else if (sourceText.contains("meat") || sourceText.contains("chicken") || sourceText.contains("beef") || sourceText.contains("pork") || sourceText.contains("lamb") || sourceText.contains("steak") || sourceText.contains("tavuk") || sourceText.contains("et")) {
                    targetDrawableId = R.drawable.meat_and_veggies;
                } else if (sourceText.contains("rice") || sourceText.contains("lentil") || sourceText.contains("beans") || sourceText.contains("chickpea") || sourceText.contains("pasta") || sourceText.contains("spaghetti") || sourceText.contains("makarna") || sourceText.contains("pilav")) {
                    targetDrawableId = R.drawable.grains_and_pulses;
                } else if (sourceText.contains("flour") || sourceText.contains("yeast") || sourceText.contains("baking powder") || sourceText.contains("bread") || sourceText.contains("pie") || sourceText.contains("dough") || sourceText.contains("börek") || sourceText.contains("poğaça")) {
                    targetDrawableId = R.drawable.bakery_and_flour;
                } else if (sourceText.contains("breakfast") || sourceText.contains("toast") || sourceText.contains("pancake") || sourceText.contains("egg") || sourceText.contains("yumurta") || sourceText.contains("tost")) {
                    targetDrawableId = R.drawable.breakfast;
                } else if (sourceText.contains("salad") || sourceText.contains("mezze") || sourceText.contains("lettuce") || sourceText.contains("cucumber") || sourceText.contains("olive oil") || sourceText.contains("salata")) {
                    targetDrawableId = R.drawable.salads;
                } else if (sourceText.contains("tomato") || sourceText.contains("potato") || sourceText.contains("onion") || sourceText.contains("garlic") || sourceText.contains("carrot") || sourceText.contains("broccoli") || sourceText.contains("sebze") || sourceText.contains("patates")) {
                    targetDrawableId = R.drawable.mixed_vegetables;
                } else {
                    targetDrawableId = R.drawable.world_cuisine_1;
                }
            }

            ivRecipeImage.setImageResource(targetDrawableId);
        }

        if (recipe.steps != null && !recipe.steps.isEmpty() && !recipe.steps.equals("nan")) {
            tvInstructions.setText(recipe.steps);
        } else {
            tvInstructions.setText("📝 Instructions are not available.");
        }
        processIngredients(recipe);
    }

    private void processIngredients(Recipe_Entity recipe) {
        StringBuilder ingredientsText = new StringBuilder();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<PantryItem> pantryItems = db.pantryDao().getAllItems();
            Set<String> pantrySet = new HashSet<>();
            for (PantryItem item : pantryItems) {
                pantrySet.add(IngredientNormalizer.normalize(item.getName()));
            }

            mainHandler.post(() -> {
                boolean tipShown = false;
                String[] ingredientsList = (recipe.ingredients != null ? recipe.ingredients : "").split(",");
                for (String ingredient : ingredientsList) {
                    String originalIng = ingredient.toLowerCase().trim();
                    if(originalIng.isEmpty()) continue;

                    String normalizedIng = IngredientNormalizer.normalize(originalIng);
                    ingredientsText.append("• ").append(originalIng);

                    if (pantrySet.contains(normalizedIng)) {
                        ingredientsText.append(" ✅ (Have)\n");
                    } else {
                        String substitute = subEngine.getSubstitute(normalizedIng);
                        if (substitute != null) {
                            boolean haveSubstitute = pantrySet.contains(IngredientNormalizer.normalize(substitute));
                            ingredientsText.append("\n    💡 Use: ").append(substitute)
                                    .append(haveSubstitute ? " (You have this!)" : "").append("\n");
                            if (!tipShown) {
                                showSubstitutionTip(originalIng, substitute);
                                tipShown = true;
                            }
                        } else {
                            ingredientsText.append(" ❌ (Missing)\n");
                        }
                    }
                }
                tvIngredients.setText(ingredientsText.toString());
            });
        });
    }

    private void showSubstitutionTip(String missing, String substitute) {
        llSubstitutionTip.setVisibility(View.VISIBLE);
        tvSubstitutionText.setText("Missing " + missing + "? Use " + substitute + " instead!");
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder capitalizeWord = new StringBuilder();
        for (String w : words) {
            if(w.length() > 0) {
                capitalizeWord.append(w.substring(0, 1).toUpperCase()).append(w.substring(1).toLowerCase()).append(" ");
            }
        }
        return capitalizeWord.toString().trim();
    }
}