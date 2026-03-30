package com.havvanuraslan.mypantry;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextView tvWelcome, tvSubtitle;
    private Button btnUpdatePantry, btnExploreRecipes, btnGroceryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivLogo = findViewById(R.id.ivLogo);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnUpdatePantry = findViewById(R.id.btnUpdatePantry);
        btnExploreRecipes = findViewById(R.id.btnExploreRecipes);
        btnGroceryList = findViewById(R.id.btnGroceryList);

        playStartAnimation();


        btnUpdatePantry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PantryActivity.class);
                startActivity(intent);
            }
        });

        btnExploreRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecipeListActivity.class);
                startActivity(intent);
            }
        });

        btnGroceryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroceryListActivity.class);
                startActivity(intent);
            }
        });



    }

    private void playStartAnimation() {
        Animation slideDown = new TranslateAnimation(0, 0, -100, 0);
        slideDown.setDuration(1000);
        slideDown.setFillAfter(true);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);

        ivLogo.startAnimation(slideDown);
        tvWelcome.startAnimation(fadeIn);
        tvSubtitle.startAnimation(fadeIn);
    }
}