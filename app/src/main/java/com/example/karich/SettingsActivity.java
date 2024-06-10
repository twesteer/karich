package com.example.karich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.util.Log;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 100;

    private ImageView ivEditProfilePicture;
    private EditText etEditUsername;
    private EditText etEditBio;
    private Button btnChangePicture;
    private Button btnSave;

    private FirebaseFirestore db;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ivEditProfilePicture = findViewById(R.id.iv_edit_profile_picture);
        etEditUsername = findViewById(R.id.et_edit_username);
        etEditBio = findViewById(R.id.et_edit_bio);
        btnChangePicture = findViewById(R.id.btn_change_picture);
        btnSave = findViewById(R.id.btn_save);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        loadUserProfile();

        btnChangePicture.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                openImagePicker();
            }
        });

        btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("username");
                            String bio = document.getString("bio");
                            String profilePictureUrl = document.getString("profilePictureUrl");

                            etEditUsername.setText(username);
                            etEditBio.setText(bio);
                            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                                Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_avatar).into(ivEditProfilePicture);
                            }
                        }
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение "), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Picasso.get().load(selectedImageUri).into(ivEditProfilePicture);
        }
    }

    private void saveUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        String newUsername = etEditUsername.getText().toString().trim();
        String newBio = etEditBio.getText().toString().trim();

        if (selectedImageUri != null) {
            StorageReference fileReference = storageReference.child("profilePictures/" + UUID.randomUUID().toString());
            fileReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profilePictureUrl = uri.toString();
                        updateUserProfile(userId, newUsername, newBio, profilePictureUrl);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(SettingsActivity.this, "Ошибка загрузки изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("SettingsActivity", "Ошибка загрузки изображения", e);
                    });
        } else {
            updateUserProfile(userId, newUsername, newBio, null);
        }
    }

    private void updateUserProfile(String userId, String username, String bio, @Nullable String profilePictureUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Составляем данные для обновления
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("bio", bio);
        if (profilePictureUrl != null) {
            userData.put("profilePictureUrl", profilePictureUrl);
        }

        // Проверяем, что данные не пустые перед сохранением
        if (username.isEmpty()) {
            Toast.makeText(SettingsActivity.this, "Имя пользователя не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SettingsActivity.this, "Профиль обновлен", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show();
                        Log.e("SettingsActivity", "Ошибка обновления профиля", task.getException());
                    }
                });
    }

    private void saveUserData(String userId, String username, String bio, @Nullable String profilePictureUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(userId)
                .update("username", username, "bio", bio, "profilePictureUrl", profilePictureUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SettingsActivity.this, "Профиль обновлен", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
