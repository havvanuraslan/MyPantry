package com.havvanuraslan.mypantry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout; // EKLENDİ
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // EKLENDİ

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword; // 🌟 Boş alan uyarısı için eklendi
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private ImageButton btnRegisterBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // TextInputLayout Eşlemeleri
        tilName = findViewById(R.id.tilRegName);
        tilEmail = findViewById(R.id.tilRegEmail);
        tilPassword = findViewById(R.id.tilRegPassword);
        tilConfirmPassword = findViewById(R.id.tilRegConfirmPassword);

        // Girdi (EditText) Eşlemeleri
        etName = findViewById(R.id.etRegName);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        btnRegisterBack = findViewById(R.id.btnRegisterBack);

        if (btnRegisterBack != null) {
            btnRegisterBack.setOnClickListener(v -> navigateToLogin());
        }

        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> navigateToLogin());
        }

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Önceki tüm hata mesajlarını temizle
            tilName.setError(null);
            tilEmail.setError(null);
            tilPassword.setError(null);
            tilConfirmPassword.setError(null);

            boolean hasError = false;

            // 🌟 MODERN INPUT DOĞRULAMALARI (Boş bırakılamaz hataları doğrudan kutuların altına yazılır)
            if (name.isEmpty()) {
                tilName.setError("Full name cannot be left empty");
                hasError = true;
            }
            if (email.isEmpty()) {
                tilEmail.setError("Email address cannot be left empty");
                hasError = true;
            }
            if (password.isEmpty()) {
                tilPassword.setError("Password cannot be left empty");
                hasError = true;
            }
            if (confirmPassword.isEmpty()) {
                tilConfirmPassword.setError("Please confirm your password");
                hasError = true;
            }

            if (hasError) return;

            // Şifre mantık kontrolleri
            if (password.length() < 6) {
                tilPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                tilConfirmPassword.setError("Passwords do not match!");
                return;
            }

            registerUserInFirebase(email, password);
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerUserInFirebase(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // 🌟 Gelişmiş Hata Yakalama: Eğer e-posta zaten kayıtlıysa tetiklenir
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            tilEmail.setError("This email address is already in use by another account.");
                        } else {
                            // Diğer beklenmeyen Firebase hataları için genel uyarı
                            tilEmail.setError("Registration failed: " + task.getException().getMessage());
                        }
                    }
                });
    }
}