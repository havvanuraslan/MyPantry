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
    private ImageButton btnBack;

    // --- YENİ ÇEVRİMDIŞI (OFFLINE) MİMARİ DEĞİŞKENLERİ ---
    private Recipe_Dao recipeDao;
    private AppDatabase db;
    private SubstitutionEngine subEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Başlatmalar (Kendi Veritabanlarımız)
        recipeDao = Recipe_Database.getDbInstance(getApplicationContext()).recipeDao();
        db = AppDatabase.getDbInstance(getApplicationContext());
        subEngine = new SubstitutionEngine();

        // UI Bağlantıları
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvCategory = findViewById(R.id.tvCategory);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvInstructions = findViewById(R.id.tvInstructions);
        llSubstitutionTip = findViewById(R.id.llSubstitutionTip);
        tvSubstitutionText = findViewById(R.id.tvSubstitutionText);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Tıklanan tarifin ID'sini (Artık Integer) alıyoruz
        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);

        if (recipeId != -1) {
            fetchRecipeDetails(recipeId);
        } else {
            Toast.makeText(this, "Error: No Recipe ID found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchRecipeDetails(int recipeId) {
        progressBar.setVisibility(View.VISIBLE);
        llSubstitutionTip.setVisibility(View.GONE);

        // Room veritabanı işlemleri UI'ı dondurmasın diye arka planda (Background Thread) yapıyoruz
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // SQLite'dan tek bir tarifi çek
                Recipe_Entity recipe = recipeDao.getRecipeById(recipeId);

                // Sonucu ekrana basmak için Ana Thread'e dön
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (recipe != null) {
                        displayRecipe(recipe);
                    } else {
                        Toast.makeText(this, "Recipe not found in local database", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayRecipe(Recipe_Entity recipe) {
        // 1. İsimlendirme ve Görsellik
        tvRecipeName.setText(capitalizeWords(recipe.name));

        // Kategori yerine Hazırlanma Süresi ve Etiketleri gösteriyoruz
        String info = "⏱️ Takes " + recipe.minutes + " minutes\nTags: " +
                (recipe.tags != null ? recipe.tags : "");
        tvCategory.setText(info);

        // İnternet kullanmadığımız için varsayılan yemek ikonunu koyuyoruz
        ivRecipeImage.setImageResource(R.drawable.ic_launcher_foreground);

        // 2. YAPILIŞ ADIMLARI (Yeni Eklediğimiz Kısım!)
        if (recipe.steps != null && !recipe.steps.isEmpty() && !recipe.steps.equals("nan")) {
            tvInstructions.setText(recipe.steps);
        } else {
            tvInstructions.setText("📝 Instructions are not available for this recipe.");
        }

        // --- 3. ZEKA KISMI: İkame (Substitution) Analizi ---
        StringBuilder ingredientsText = new StringBuilder();

        // Arka planda kilerdeki malzemeleri çekmek için küçük bir thread daha
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<PantryItem> pantryItems = db.pantryDao().getAllItems();
            Set<String> pantrySet = new HashSet<>();
            for (PantryItem item : pantryItems) {
                pantrySet.add(item.getName().toLowerCase().trim());
            }

            mainHandler.post(() -> {
                boolean tipShown = false;

                // Malzemeleri virgülden ayırıyoruz (Python'da virgüllü birleştirmiştik)
                String rawIngredients = recipe.ingredients != null ? recipe.ingredients : "";
                String[] ingredientsList = rawIngredients.split(",");

                for (String ingredient : ingredientsList) {
                    String cleanIng = ingredient.toLowerCase().trim();
                    if(cleanIng.isEmpty()) continue;

                    ingredientsText.append("• ").append(cleanIng);

                    // Durum 1: Malzeme Kilerde VAR
                    if (pantrySet.contains(cleanIng)) {
                        ingredientsText.append(" ✅ (Have)\n");
                    }
                    // Durum 2: Malzeme YOK -> İkame Bakalım
                    else {
                        String substitute = subEngine.getSubstitute(cleanIng);

                        if (substitute != null) {
                            boolean haveSubstitute = pantrySet.contains(substitute.toLowerCase().trim());

                            if (haveSubstitute) {
                                ingredientsText.append("\n    💡 Use: ").append(substitute).append(" (You have this!)\n");
                                if (!tipShown) {
                                    showSubstitutionTip(cleanIng, substitute + " (Available in Pantry!)");
                                    tipShown = true;
                                }
                            } else {
                                ingredientsText.append("\n    💡 Tip: You can use ").append(substitute).append("\n");
                                if (!tipShown) {
                                    showSubstitutionTip(cleanIng, substitute);
                                    tipShown = true;
                                }
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