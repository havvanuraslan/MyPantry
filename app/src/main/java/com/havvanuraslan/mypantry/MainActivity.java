package com.havvanuraslan.mypantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView ivLogo;
    private TextView tvWelcome, tvSubtitle;
    private Button btnUpdatePantry, btnExploreRecipes, btnGroceryList;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        ivLogo = findViewById(R.id.ivLogo);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnUpdatePantry = findViewById(R.id.btnUpdatePantry);
        btnExploreRecipes = findViewById(R.id.btnExploreRecipes);
        btnGroceryList = findViewById(R.id.btnGroceryList);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        updateNavHeader();
        playStartAnimation();

        btnUpdatePantry.setOnClickListener(v -> startActivity(new Intent(this, PantryActivity.class)));

        btnExploreRecipes.setOnClickListener(v -> startActivity(new Intent(this, RecipeListActivity.class)));

        btnGroceryList.setOnClickListener(v -> startActivity(new Intent(this, GroceryListActivity.class)));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_favorites) {
            Intent intent = new Intent(MainActivity.this, FavoriteRecipesActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && tvHeaderEmail != null) {
                tvHeaderEmail.setText(user.getEmail());
            }
        }
    }

    private void playStartAnimation() {
        Animation slideDown = new TranslateAnimation(0, 0, -100, 0);
        slideDown.setDuration(1000);
        ivLogo.startAnimation(slideDown);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        tvWelcome.startAnimation(fadeIn);
        tvSubtitle.startAnimation(fadeIn);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}