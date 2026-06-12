package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvWelcomeTitle, tvHomeUserName;
    private TextView tvHomePantryCount, tvHomeFavCount, tvHomeExpiringCount;

    private View btnUpdatePantry, btnExploreRecipes, btnGroceryList;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvHomeSuggestions;

    private SwitchMaterial switchLanguageToggle, switchThemeToggle;
    private TextView tvLangEN, tvLangTR;

    private FirebaseAuth mAuth;
    private AppDatabase pantryDb;
    private Recipe_Dao recipeDao;

    private SharedPreferences userPrefs;
    private SharedPreferences systemPrefs;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        systemPrefs = getSharedPreferences("MyPantryPrefs", Context.MODE_PRIVATE);
        applySavedLanguage();

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        pantryDb = AppDatabase.getDbInstance(this);

        try {
            recipeDao = Recipe_Database.getDbInstance(this).recipeDao();
        } catch (Exception e) {
            e.printStackTrace();
        }

        userPrefs = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        rvHomeSuggestions = findViewById(R.id.rvHomeSuggestions);

        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        tvHomeUserName = findViewById(R.id.tvHomeUserName);
        tvHomePantryCount = findViewById(R.id.tvHomePantryCount);
        tvHomeFavCount = findViewById(R.id.tvHomeFavCount);
        tvHomeExpiringCount = findViewById(R.id.tvHomeExpiringCount);

        btnUpdatePantry = findViewById(R.id.btnUpdatePantry);
        btnExploreRecipes = findViewById(R.id.btnExploreRecipes);
        btnGroceryList = findViewById(R.id.btnGroceryList);
        TextView tvViewAllRecipes = findViewById(R.id.tvHomeViewAllRecipes);

        navigationView.setNavigationItemSelectedListener(this);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (rvHomeSuggestions != null) {
            rvHomeSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        setupDynamicGreeting();

        if (btnUpdatePantry != null) {
            btnUpdatePantry.setOnClickListener(v -> startActivity(new Intent(this, PantryActivity.class)));
        }
        if (btnExploreRecipes != null) {
            btnExploreRecipes.setOnClickListener(v -> startActivity(new Intent(this, RecipeListActivity.class)));
        }
        if (btnGroceryList != null) {
            btnGroceryList.setOnClickListener(v -> startActivity(new Intent(this, GroceryListActivity.class)));
        }
        if (tvViewAllRecipes != null) {
            tvViewAllRecipes.setOnClickListener(v -> startActivity(new Intent(this, RecipeListActivity.class)));
        }

        if (navigationView != null) {
            switchLanguageToggle = navigationView.findViewById(R.id.switchLanguageToggle);
            switchThemeToggle = navigationView.findViewById(R.id.switchThemeToggle);
            tvLangEN = navigationView.findViewById(R.id.tvLangEN);
            tvLangTR = navigationView.findViewById(R.id.tvLangTR);
        }

        setupCustomToggles();

        loadAllHomeDataPipeline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllHomeDataPipeline();
        updateNavHeader();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(MainActivity.this, FavoriteRecipesActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupCustomToggles() {
        if (switchLanguageToggle != null) {
            String currentLang = systemPrefs.getString("app_language", "en");
            switchLanguageToggle.setChecked(currentLang.equals("tr"));
            updateLanguageTextHighlights(currentLang.equals("tr"));

            switchLanguageToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String targetLang = isChecked ? "tr" : "en";
                systemPrefs.edit().putString("app_language", targetLang).apply();
                updateLanguageTextHighlights(isChecked);

                Locale locale = new Locale(targetLang);
                Locale.setDefault(locale);
                Resources resources = getResources();
                Configuration config = resources.getConfiguration();
                config.setLocale(locale);
                resources.updateConfiguration(config, resources.getDisplayMetrics());

                String toastMessage = isChecked ? "Uygulama dili Türkçe yapıldı!" : "Language switched to English!";
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

                recreate();
            });
        }

        if (switchThemeToggle != null) {
            String currentTheme = systemPrefs.getString("app_theme_mode", "light");
            switchThemeToggle.setChecked(currentTheme.equals("light"));

            switchThemeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String targetTheme = isChecked ? "light" : "dark";
                systemPrefs.edit().putString("app_theme_mode", targetTheme).apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(this, "Light Mode Activated ☀️", Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(this, "Dark Mode Activated 🌙", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateLanguageTextHighlights(boolean isTurkish) {
        if (tvLangEN != null && tvLangTR != null) {
            if (isTurkish) {
                tvLangTR.setTextColor(Color.parseColor("#9A5B30"));
                tvLangEN.setTextColor(Color.parseColor("#BCAAA4"));
            } else {
                tvLangEN.setTextColor(Color.parseColor("#9A5B30"));
                tvLangTR.setTextColor(Color.parseColor("#BCAAA4"));
            }
        }
    }

    private void applySavedLanguage() {
        String langCode = systemPrefs.getString("app_language", "en");
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void loadAllHomeDataPipeline() {
        executorService.execute(() -> {
            int pantryCount = 0;
            int favCount = 0;
            int criticalItemsCount = 0;
            List<Recipe_Entity> smartSuggestions = null;

            try {
                if (pantryDb != null && pantryDb.pantryDao() != null) {
                    List<PantryItem> allItems = pantryDb.pantryDao().getAllItems();
                    pantryCount = allItems != null ? allItems.size() : 0;

                    if (recipeDao != null) {
                        List<Recipe_Entity> rawMatches = null;

                        if (allItems != null && !allItems.isEmpty()) {
                            String topIngredient = allItems.get(0).getName();
                            if (topIngredient != null && !topIngredient.isEmpty()) {
                                rawMatches = recipeDao.getSmartSuggestionsByIngredient(topIngredient.trim());
                            }
                        }

                        if (rawMatches == null || rawMatches.isEmpty()) {
                            rawMatches = recipeDao.getSmartSuggestionsByIngredient("tomato");
                        }

                        if (rawMatches != null && !rawMatches.isEmpty()) {
                            java.util.Collections.shuffle(rawMatches);

                            int limitCount = Math.min(5, rawMatches.size());
                            smartSuggestions = new ArrayList<>(rawMatches.subList(0, limitCount));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (recipeDao != null) {
                    favCount = recipeDao.getFavoriteRecipes().size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final int finalPantryCount = pantryCount;
            final int finalFavCount = favCount;
            final int finalExpiringCount = criticalItemsCount;
            final List<Recipe_Entity> finalSuggestions = smartSuggestions;

            mainHandler.post(() -> {
                try {
                    if (tvHomePantryCount != null) tvHomePantryCount.setText(String.valueOf(finalPantryCount));
                    if (tvHomeFavCount != null) tvHomeFavCount.setText(String.valueOf(finalFavCount));
                    if (tvHomeExpiringCount != null) tvHomeExpiringCount.setText(String.valueOf(finalExpiringCount));

                    if (finalSuggestions != null && !finalSuggestions.isEmpty() && rvHomeSuggestions != null) {
                        HomeSuggestionsAdapter adapter = new HomeSuggestionsAdapter(MainActivity.this, finalSuggestions);
                        rvHomeSuggestions.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void setupDynamicGreeting() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String savedName = userPrefs.getString("saved_name", "");
            if (!savedName.isEmpty()) {
                if (tvHomeUserName != null) tvHomeUserName.setText(savedName + "!");
            } else if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                if (tvHomeUserName != null) tvHomeUserName.setText(user.getDisplayName() + "!");
            } else if (user.getEmail() != null) {
                String email = user.getEmail();
                String namePart = email.split("@")[0];
                String formattedName = namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
                if (tvHomeUserName != null) tvHomeUserName.setText(formattedName + "!");
            }
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (tvWelcomeTitle != null) {
            if (hour >= 5 && hour < 12) {
                tvWelcomeTitle.setText("Good morning,");
            } else if (hour >= 12 && hour < 17) {
                tvWelcomeTitle.setText("Good afternoon,");
            } else {
                tvWelcomeTitle.setText("Good evening,");
            }
        }
    }

    private void updateNavHeader() {
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
                TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
                ImageView ivNavProfileCircle = headerView.findViewById(R.id.ivNavProfileCircle);

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {

                    String savedUsername = userPrefs.getString("saved_username", "");
                    if (tvHeaderEmail != null) {
                        if (!savedUsername.isEmpty()) {
                            tvHeaderEmail.setText("@" + savedUsername);
                        } else if (user.getEmail() != null) {
                            String usernamePrefix = user.getEmail().split("@")[0];
                            tvHeaderEmail.setText("@" + usernamePrefix);
                        }
                    }

                    String savedName = userPrefs.getString("saved_name", "");
                    if (tvHeaderName != null) {
                        if (!savedName.isEmpty()) {
                            tvHeaderName.setText(savedName);
                        } else if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                            tvHeaderName.setText(user.getDisplayName());
                        } else if (user.getEmail() != null) {
                            String namePart = user.getEmail().split("@")[0];
                            String formattedName = namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
                            tvHeaderName.setText(formattedName);
                        }
                    }

                    String savedImageBase64 = userPrefs.getString("profile_image_base64", "");
                    if (!savedImageBase64.isEmpty() && ivNavProfileCircle != null) {
                        try {
                            byte[] decodedString = Base64.decode(savedImageBase64, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivNavProfileCircle.setImageBitmap(decodedByte);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}