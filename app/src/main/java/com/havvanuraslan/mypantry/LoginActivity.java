package com.havvanuraslan.mypantry;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button; // EKLENDİ
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout; // EKLENDİ
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private TextView tvGoToRegister, tvForgotPassword;
    private ImageButton btnAbout;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // TextInputLayout ve EditText eşlemeleri
        tilEmail = findViewById(R.id.tilLoginEmail);
        tilPassword = findViewById(R.id.tilLoginPassword);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnAbout = findViewById(R.id.btnAbout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1047173677540-n1co3k08f6glfuqd8am6a2791va7fqa9.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> showAboutDialog());
        }

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // 🌟 DÜZELTİLDİ: Şifremi unuttum linki artık doğrudan popup açıyor
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            tilEmail.setError(null);
            tilPassword.setError(null);

            boolean hasError = false;

            if (email.isEmpty()) {
                tilEmail.setError("Email address cannot be left empty");
                hasError = true;
            }
            if (password.isEmpty()) {
                tilPassword.setError("Password cannot be left empty");
                hasError = true;
            }

            if (hasError) return;

            loginUserWithEmail(email, password);
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void showAboutDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);

        builder.setTitle("About MyPantry 🏷️");
        builder.setMessage("Welcome to MyPantry!\n\n" +
                "This app is your smart virtual kitchen assistant designed to track, organize, " +
                "and manage your pantry supplies seamlessly.\n\n" +
                "• Prevent waste by tracking expiry dates.\n" +
                "• Get personal recipe recommendations.\n" +
                "• Keep your grocery list up to date.\n\n" +
                "Enjoy smart cooking!");

        builder.setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setColor(android.graphics.Color.WHITE);
            float cornerRadiusPx = 24 * getResources().getDisplayMetrics().density;
            drawable.setCornerRadius(cornerRadiusPx);
            dialog.getWindow().setBackgroundDrawable(drawable);
        }

        dialog.show();
    }

    // 🌟 YENİ METOD: Giriş ekranı üzerinde açılan modern Şifre Sıfırlama Penceresi
    private void showForgotPasswordDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);

        builder.setTitle("Reset Password 🔑");
        builder.setMessage("Enter your registered email address below. We will send a secure link to reset your account password.");

        // Dinamik Material OutlinedBox oluşturuyoruz
        TextInputLayout til = new TextInputLayout(this, null,
                com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
        til.setHint("Email Address");
        til.setBoxStrokeColor(android.graphics.Color.parseColor("#D7C4B7"));
        til.setHintTextColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8C8C8C")));

        // Köşe kavisini form alanlarıyla eşitliyoruz (16dp)
        float radius = 16 * getResources().getDisplayMetrics().density;
        til.setBoxCornerRadii(radius, radius, radius, radius);

        EditText etResetEmail = new EditText(this);
        etResetEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etResetEmail.setTextColor(android.graphics.Color.parseColor("#2D2D2D"));
        etResetEmail.setTextSize(14);

        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        etResetEmail.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        til.addView(etResetEmail);

        // Düzen konteyneri ve kenar boşlukları (Margin)
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int marginPx = (int) (22 * getResources().getDisplayMetrics().density);
        lp.setMargins(marginPx, (int) (8 * getResources().getDisplayMetrics().density), marginPx, 0);
        container.addView(til, lp);

        builder.setView(container);

        builder.setPositiveButton("Send Link", null); // Otomatik kapanmayı önlemek için null veriyoruz
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Opak beyaz ve yuvarlak köşeli arka plan şablonu (Giriş kartıyla ikiz)
        if (dialog.getWindow() != null) {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setColor(android.graphics.Color.WHITE);
            float cardRadiusPx = 24 * getResources().getDisplayMetrics().density;
            drawable.setCornerRadius(cardRadiusPx);
            dialog.getWindow().setBackgroundDrawable(drawable);
        }

        // Tıklama kontrolleri ve boş alan denetimi (Hata durumunda pencerenin kapanmasını önler)
        dialog.setOnShowListener(dialogInterface -> {
            Button btnSend = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSend.setTextColor(android.graphics.Color.parseColor("#9A5B30")); // Taba rengi

            Button btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            btnCancel.setTextColor(android.graphics.Color.parseColor("#8C8C8C"));

            btnSend.setOnClickListener(v -> {
                String email = etResetEmail.getText().toString().trim();
                til.setError(null);

                if (email.isEmpty()) {
                    til.setError("Email address cannot be left empty");
                    return;
                }

                // Firebase resmi şifre sıfırlama bağlantısı mekanizması
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Reset link sent! Check your inbox.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                til.setError(task.getException().getMessage());
                            }
                        });
            });
        });

        dialog.show();
    }

    private void loginUserWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        tilPassword.setError("Invalid credentials: " + task.getException().getMessage());
                    }
                });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w("GoogleSignIn", "Google sign in failed", e);
                        tilPassword.setError("Google Sign-In failed.");
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        tilPassword.setError("Firebase Google Authentication failed.");
                    }
                });
    }
}