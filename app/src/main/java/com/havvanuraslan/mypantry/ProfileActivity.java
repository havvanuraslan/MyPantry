package com.havvanuraslan.mypantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    private EditText etName, etUsername, etEmail, etPhone, etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextInputLayout tilName, tilUsername, tilEmail, tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private MaterialButton btnSaveProfile;
    private ImageButton btnBack;

    private ImageView ivProfileImage;
    private View btnChangeProfileImage;
    private String encodedImage = "";

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null && ivProfileImage != null) {
                        try {
                            InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                            ivProfileImage.setImageBitmap(bitmap);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] imageBytes = baos.toByteArray();
                            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        sharedPreferences = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);

        etName = findViewById(R.id.etProfileName);
        etUsername = findViewById(R.id.etProfileUsername);
        etEmail = findViewById(R.id.etProfileEmail);
        etPhone = findViewById(R.id.etProfilePhone);

        etCurrentPassword = findViewById(R.id.etProfileCurrentPassword);
        etNewPassword = findViewById(R.id.etProfileNewPassword);
        etConfirmPassword = findViewById(R.id.etProfileConfirmPassword);

        tilName = findViewById(R.id.tilProfileName);
        tilUsername = findViewById(R.id.tilProfileUsername);
        tilEmail = findViewById(R.id.tilProfileEmail);
        tilCurrentPassword = findViewById(R.id.tilProfileCurrentPassword);
        tilNewPassword = findViewById(R.id.tilProfileNewPassword);
        tilConfirmPassword = findViewById(R.id.tilProfileConfirmPassword);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnProfileBack);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnChangeProfileImage = findViewById(R.id.btnChangeProfileImage);

        AutoCompleteTextView actvGender = findViewById(R.id.actvProfileGender);
        if (actvGender != null) {
            String[] genderOptions = {"Male", "Female", "Other", "Prefer not to say"};
            actvGender.setAdapter(new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, genderOptions));
        }

        AutoCompleteTextView actvCountry = findViewById(R.id.actvCountryCode);
        if (actvCountry != null) {
            String[] countryCodes = {"+90", "+1", "+44", "+34"};
            actvCountry.setAdapter(new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, countryCodes));
        }

        if (btnChangeProfileImage != null) {
            btnChangeProfileImage.setOnClickListener(v -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                galleryLauncher.launch(galleryIntent);
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (user != null) {
            etEmail.setText(user.getEmail());

            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                etName.setText(user.getDisplayName());
            } else {
                etName.setText(sharedPreferences.getString("saved_name", ""));
            }

            etUsername.setText(sharedPreferences.getString("saved_username", ""));
            etPhone.setText(sharedPreferences.getString("saved_phone", ""));

            String savedCountryCode = sharedPreferences.getString("saved_country_code", "+90");
            if (actvCountry != null) {
                actvCountry.setText(savedCountryCode, false);
            }

            String savedGender = sharedPreferences.getString("saved_gender", "");
            if (actvGender != null && !savedGender.isEmpty()) {
                actvGender.setText(savedGender, false);
            }

            String savedImage = sharedPreferences.getString("profile_image_base64", "");
            if (!savedImage.isEmpty() && ivProfileImage != null) {
                byte[] decodedString = Base64.decode(savedImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivProfileImage.setImageBitmap(decodedByte);
            }
        }

        btnSaveProfile.setOnClickListener(v -> {
            tilName.setError(null);
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            String name = etName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String countryCode = actvCountry != null ? actvCountry.getText().toString() : "+90";
            String gender = actvGender != null ? actvGender.getText().toString() : "";

            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty()) {
                tilName.setError("Name cannot be left empty");
                return;
            }

            if (user != null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(profileTask -> {
                            if (profileTask.isSuccessful()) {

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("saved_name", name);
                                editor.putString("saved_username", username);
                                editor.putString("saved_phone", phone);
                                editor.putString("saved_country_code", countryCode);
                                editor.putString("saved_gender", gender);
                                if (!encodedImage.isEmpty()) {
                                    editor.putString("profile_image_base64", encodedImage);
                                }
                                editor.commit();

                                if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                                    if (currentPassword.isEmpty()) {
                                        tilCurrentPassword.setError("Please enter your current password");
                                        return;
                                    }
                                    if (newPassword.length() < 6) {
                                        tilNewPassword.setError("New password must be at least 6 characters");
                                        return;
                                    }
                                    if (!newPassword.equals(confirmPassword)) {
                                        tilConfirmPassword.setError("Passwords do not match!");
                                        return;
                                    }

                                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                                    user.reauthenticate(credential)
                                            .addOnCompleteListener(reAuthTask -> {
                                                if (reAuthTask.isSuccessful()) {
                                                    user.updatePassword(newPassword)
                                                            .addOnCompleteListener(passwordTask -> {
                                                                if (passwordTask.isSuccessful()) {
                                                                    Toast.makeText(ProfileActivity.this, "Profile and Password updated!", Toast.LENGTH_SHORT).show();
                                                                    etCurrentPassword.setText("");
                                                                    etNewPassword.setText("");
                                                                    etConfirmPassword.setText("");
                                                                }
                                                            });
                                                } else {
                                                    tilCurrentPassword.setError("Current password is incorrect!");
                                                }
                                            });
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}