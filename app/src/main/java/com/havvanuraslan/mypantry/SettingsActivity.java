package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    // SharedPreferences Anahtarları (İleride projenin diğer ekranlarından da bu isimlerle çağrılabilir)
    private static final String PREFS_NAME = "MyPantryPrefs";
    private static final String KEY_NOTIF = "notifications_enabled";
    private static final String KEY_EXPIRY_ALERT = "expiry_alerts_enabled";
    private static final String KEY_STRICT = "strict_match_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ortak Prefs Havuzu İlklendirmesi
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 🌟 1. XML BİLEŞENLERİNİN DOĞRU KİMLİKLERLE EŞLENMESİ
        ImageButton btnBack = findViewById(R.id.btnSettingsBack); // ID Güncellendi

        SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
        SwitchMaterial switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts); // Yeni Eklenen
        SwitchMaterial switchStrictMatch = findViewById(R.id.switchStrictMatch);

        View layoutLanguage = findViewById(R.id.layoutLanguage);
        View layoutTheme = findViewById(R.id.layoutTheme);
        View layoutPrivacy = findViewById(R.id.layoutPrivacy);
        View layoutHelpSupport = findViewById(R.id.layoutHelpSupport); // ID Güncellendi
        View layoutClearCache = findViewById(R.id.layoutClearCache);

        // Geri Dönüş Oku Mantığı
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 🌟 2. SWITCH MANTIKLARININ DİSKE İŞLENMESİ (Kalıcı Durum Yönetimi)
        if (switchNotifications != null) {
            switchNotifications.setChecked(sharedPreferences.getBoolean(KEY_NOTIF, true));
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean(KEY_NOTIF, isChecked).apply();
                String msg = isChecked ? "Push notifications enabled" : "Notifications muted";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        if (switchExpiryAlerts != null) {
            switchExpiryAlerts.setChecked(sharedPreferences.getBoolean(KEY_EXPIRY_ALERT, true));
            switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean(KEY_EXPIRY_ALERT, isChecked).apply();
                String msg = isChecked ? "Pantry expiry alerts activated" : "Expiry alerts disabled";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        if (switchStrictMatch != null) {
            switchStrictMatch.setChecked(sharedPreferences.getBoolean(KEY_STRICT, false));
            switchStrictMatch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean(KEY_STRICT, isChecked).apply();
                String msg = isChecked ? "Strict matching activated" : "Semantic matching active";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        // 🌟 3. SİSTEM VE INTERFACE TIKLAMA DİNLENMESİ (Gelecekte Kodlanacak Alanlar)
        if (layoutLanguage != null) {
            layoutLanguage.setOnClickListener(v ->
                    Toast.makeText(this, "Language selection coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (layoutTheme != null) {
            layoutTheme.setOnClickListener(v ->
                    Toast.makeText(this, "Theme toggling coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (layoutPrivacy != null) {
            layoutPrivacy.setOnClickListener(v ->
                    Toast.makeText(this, "Privacy Policy dialog coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (layoutHelpSupport != null) {
            layoutHelpSupport.setOnClickListener(v ->
                    Toast.makeText(this, "Opening support channel...", Toast.LENGTH_SHORT).show());
        }

        // 🌟 4. CACHE (ÖNBELLEK) TEMİZLEME MOTORU
        if (layoutClearCache != null) {
            layoutClearCache.setOnClickListener(v -> {
                try {
                    File dir = getCacheDir();
                    if (dir != null && deleteDir(dir)) {
                        Toast.makeText(this, "Application cache cleared successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Cache is already empty.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to clear cache.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Önbellek dizinlerini derinlemesine silen yardımcı algoritma
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}