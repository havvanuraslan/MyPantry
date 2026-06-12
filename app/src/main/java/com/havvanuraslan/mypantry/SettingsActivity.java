package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    // Persistent SharedPreferences Storage Keys
    private static final String PREFS_NAME = "MyPantryPrefs";
    private static final String KEY_NOTIF = "notifications_enabled";
    private static final String KEY_EXPIRY_ALERT = "expiry_alerts_enabled";
    private static final String KEY_STRICT = "strict_match_enabled";

    private static final String KEY_THEME = "app_theme_mode";
    private static final String KEY_LANG = "app_language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        applySavedLanguage();

        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btnSettingsBack);

        SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
        SwitchMaterial switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        SwitchMaterial switchStrictMatch = findViewById(R.id.switchStrictMatch);

        View layoutLanguage = findViewById(R.id.layoutLanguage);
        View layoutTheme = findViewById(R.id.layoutTheme);
        View layoutPrivacy = findViewById(R.id.layoutPrivacy);
        View layoutHelpSupport = findViewById(R.id.layoutHelpSupport);
        View layoutClearCache = findViewById(R.id.layoutClearCache);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (switchNotifications != null) {
            switchNotifications.setChecked(sharedPreferences.getBoolean(KEY_NOTIF, true));
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean(KEY_NOTIF, isChecked).apply();
            });
        }

        if (switchExpiryAlerts != null) {
            switchExpiryAlerts.setChecked(sharedPreferences.getBoolean(KEY_EXPIRY_ALERT, true));
            switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean(KEY_EXPIRY_ALERT, isChecked).apply();
            });
        }

        if (switchStrictMatch != null) {
            switchStrictMatch.setChecked(sharedPreferences.getBoolean(KEY_STRICT, false));
            switchStrictMatch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean(KEY_STRICT, isChecked).apply();
            });
        }

        if (layoutTheme != null) {
            layoutTheme.setOnClickListener(v -> {
                String currentTheme = sharedPreferences.getString(KEY_THEME, "light");
                if (currentTheme.equals("light")) {
                    sharedPreferences.edit().putString(KEY_THEME, "dark").apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(this, "Dark Mode Activated 🌙", Toast.LENGTH_SHORT).show();
                } else {
                    sharedPreferences.edit().putString(KEY_THEME, "light").apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(this, "Light Mode Activated ☀️", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (layoutLanguage != null) {
            layoutLanguage.setOnClickListener(v -> {
                String currentLang = sharedPreferences.getString(KEY_LANG, "en");
                if (currentLang.equals("en")) {
                    setAppLocale("tr");
                    Toast.makeText(this, "Uygulama dili Türkçe olarak değiştirildi!", Toast.LENGTH_SHORT).show();
                } else {
                    setAppLocale("en");
                    Toast.makeText(this, "Language switched to English!", Toast.LENGTH_SHORT).show();
                }
                recreate();
            });
        }

        if (layoutPrivacy != null) {
            layoutPrivacy.setOnClickListener(v -> Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show());
        }

        if (layoutHelpSupport != null) {
            layoutHelpSupport.setOnClickListener(v -> Toast.makeText(this, "Opening support...", Toast.LENGTH_SHORT).show());
        }

        if (layoutClearCache != null) {
            layoutClearCache.setOnClickListener(v -> {
                File dir = getCacheDir();
                if (dir != null && deleteDir(dir)) {
                    Toast.makeText(this, "Cache cleared successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setAppLocale(String langCode) {
        sharedPreferences.edit().putString(KEY_LANG, langCode).apply();
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void applySavedLanguage() {
        String langCode = sharedPreferences.getString(KEY_LANG, "en");
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    if (!deleteDir(new File(dir, child))) return false;
                }
            }
            return dir.delete();
        } else return dir != null && dir.isFile() ? dir.delete() : false;
    }
}