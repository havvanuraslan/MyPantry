package com.havvanuraslan.mypantry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        Button btnResetPassword = findViewById(R.id.btnResetPassword);
        Button btnLogout = findViewById(R.id.btnProfileLogout);
        ImageButton btnBack = findViewById(R.id.btnBack);

        if (user != null) {
            tvEmail.setText(user.getEmail());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Firebase üzerinden şifre sıfırlama e-postası gönderme
        btnResetPassword.setOnClickListener(v -> {
            if (user != null && user.getEmail() != null) {
                mAuth.sendPasswordResetEmail(user.getEmail())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Reset email sent to your inbox!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Hesaptan Güvenli Çıkış
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}