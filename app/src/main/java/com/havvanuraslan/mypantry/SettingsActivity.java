package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPantryPrefs";
    private static final String KEY_NOTIF = "notifications_enabled";
    private static final String KEY_STRICT = "strict_match_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SwitchCompat switchNotifications = findViewById(R.id.switchNotifications);
        SwitchCompat switchStrictMatch = findViewById(R.id.switchStrictMatch);
        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Eski kayıtlı verileri oku ve Switch durumlarını ayarla
        switchNotifications.setChecked(sharedPreferences.getBoolean(KEY_NOTIF, true));
        switchStrictMatch.setChecked(sharedPreferences.getBoolean(KEY_STRICT, false));

        // Değişiklikleri dinle ve anında kaydet
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_NOTIF, isChecked).apply();
            String status = isChecked ? "Notifications On" : "Notifications Off";
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        });

        switchStrictMatch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_STRICT, isChecked).apply();
        });
    }
}