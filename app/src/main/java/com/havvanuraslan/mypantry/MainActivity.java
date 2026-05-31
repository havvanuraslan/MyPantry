package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
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

    private FirebaseAuth mAuth;
    private AppDatabase pantryDb;
    private Recipe_Dao recipeDao;

    // 🌟 Profil senkronizasyonu için SharedPreferences tanımı
    private SharedPreferences userPrefs;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        pantryDb = AppDatabase.getDbInstance(this);
        recipeDao = Recipe_Database.getDbInstance(this).recipeDao();

        // 🌟 Profil ortak hafızası ilklendirildi
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeStatistics();
        updateNavHeader(); // 🌟 Kullanıcı profil düzenleyip döndüğünde verileri anında tazelemek için kritik alan
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(MainActivity.this, FavoriteRecipesActivity.class));
        } else if (id == R.id.nav_language) {
            Toast.makeText(this, "Language settings coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_theme) {
            Toast.makeText(this, "Theme settings coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_history) {
            Toast.makeText(this, "Pantry history coming soon!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
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

    private void setupDynamicGreeting() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Ana ekrandaki hoş geldin mesajı ismini de SharedPreferences üzerinden canlı besliyoruz
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

    private void loadHomeStatistics() {
        executorService.execute(() -> {
            try {
                List<PantryItem> allItems = pantryDb.pantryDao().getAllItems();
                final int pantryCount = allItems != null ? allItems.size() : 0;
                final int favCount = recipeDao.getFavoriteRecipes().size();

                SharedPreferences sharedPrefs = getSharedPreferences("PantryExpiryPrefs", Context.MODE_PRIVATE);
                int criticalItemsCount = 0;

                if (allItems != null) {
                    Date today = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (PantryItem item : allItems) {
                        String itemExpiryStr = sharedPrefs.getString(item.getName() + "_expiry", null);

                        if (itemExpiryStr != null && !itemExpiryStr.isEmpty()) {
                            try {
                                Date expiryDate = dateFormat.parse(itemExpiryStr);
                                if (expiryDate != null) {
                                    long diffInMillis = expiryDate.getTime() - today.getTime();
                                    long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                                    if (diffInDays <= 3) {
                                        criticalItemsCount++;
                                    }
                                }
                            } catch (Exception dateEx) {
                                // Bozuk format pas geçilir
                            }
                        }
                    }
                }

                final int finalExpiringCount = criticalItemsCount;

                mainHandler.post(() -> {
                    if (tvHomePantryCount != null) tvHomePantryCount.setText(String.valueOf(pantryCount));
                    if (tvHomeFavCount != null) tvHomeFavCount.setText(String.valueOf(favCount));
                    if (tvHomeExpiringCount != null) tvHomeExpiringCount.setText(String.valueOf(finalExpiringCount));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 🌟 GÜNCELLENDİ: Yan menü açıldığında tüm verileri SharedPreferences havuzundan taze olarak çeker
    private void updateNavHeader() {
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
                TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
                ImageView ivNavProfileCircle = headerView.findViewById(R.id.ivNavProfileCircle); // 🌟 Fotoğraf Kimliği Yakalandı

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {

                    // 1. KULLANICI ADI SENKRONİZASYONU (@username)
                    String savedUsername = userPrefs.getString("saved_username", "");
                    if (tvHeaderEmail != null) {
                        if (!savedUsername.isEmpty()) {
                            // Profilde kaydedilen güncel kullanıcı adı gelir
                            tvHeaderEmail.setText("@" + savedUsername);
                        } else if (user.getEmail() != null) {
                            // Boşsa ilk kayıt esnasındaki e-posta ön ekini korur
                            String usernamePrefix = user.getEmail().split("@")[0];
                            tvHeaderEmail.setText("@" + usernamePrefix);
                        }
                    }

                    // 2. AD SOYAD SENKRONİZASYONU
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

                    // 3. PROFİL FOTOĞRAFI SENKRONİZASYONU (Çökmesiz Kalıcı Base64 Dönüştürücü)
                    String savedImageBase64 = userPrefs.getString("profile_image_base64", "");
                    if (!savedImageBase64.isEmpty() && ivNavProfileCircle != null) {
                        try {
                            byte[] decodedString = Base64.decode(savedImageBase64, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivNavProfileCircle.setImageBitmap(decodedByte);
                        } catch (Exception e) {
                            e.printStackTrace(); // Kodun kırılmasını / çökmesini kesinlikle engeller
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