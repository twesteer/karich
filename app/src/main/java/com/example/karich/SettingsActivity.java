package com.example.karich;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

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
        StorageReference fileReference = storageReference.child("profilePictures/" + UUID.randomUUID().toString());


        loadUserProfile();

        btnChangePicture.setOnClickListener(v -> checkStoragePermission());

        btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void checkStoragePermission() {
        Log.d("SettingsActivity", "Checking storage permission");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                Log.d("SettingsActivity", "Requesting READ_MEDIA_IMAGES permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
            } else {
                openImagePicker();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d("SettingsActivity", "Requesting READ_EXTERNAL_STORAGE permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                openImagePicker();
            }
        }
    }

    private void openImagePicker() {
        Log.d("SettingsActivity", "Opening image picker");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Picasso.get().load(selectedImageUri).into(ivEditProfilePicture);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("SettingsActivity", "onRequestPermissionsResult: " + requestCode);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("SettingsActivity", "Permission granted");
                openImagePicker();
            } else {
                Log.d("SettingsActivity", "Permission denied");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    showPermissionDeniedDialog();
                } else {
                    Toast.makeText(this, "Разрешение на чтение хранилища отклонено", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Разрешение отклонено")
                .setMessage("Чтобы изменить аватарку, перейдите в настройки приложения и включите разрешение на доступ к хранилищу.")
                .setPositiveButton("Перейти в настройки", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
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

    private void saveUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        String newUsername = etEditUsername.getText().toString().trim();
        String newBio = etEditBio.getText().toString().trim();

        if (selectedImageUri != null) {
            StorageReference fileReference = storageReference.child("profilePictures/" + UUID.randomUUID().toString());
            Log.d("SettingsActivity", "Uploading file to: " + fileReference.getPath());
            fileReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("SettingsActivity", "File uploaded successfully");
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profilePictureUrl = uri.toString();
                            updateUserProfile(userId, newUsername, newBio, profilePictureUrl);
                        }).addOnFailureListener(e -> {
                            Log.e("SettingsActivity", "Failed to get download URL", e);
                            Toast.makeText(SettingsActivity.this, "Ошибка получения URL изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SettingsActivity", "Failed to upload file", e);
                        Toast.makeText(SettingsActivity.this, "Ошибка загрузки изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateUserProfile(userId, newUsername, newBio, null);
        }
    }

    private void updateUserProfile(String userId, String username, String bio, @Nullable String profilePictureUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("bio", bio);
        if (profilePictureUrl != null) {
            userData.put("profilePictureUrl", profilePictureUrl);
        }

        if (username.isEmpty()) {
            Toast.makeText(SettingsActivity.this, "Имя пользователя не может быть пустым", Toast.LENGTH_SHORT).show();
            return; // Exit the method if username is empty
        }

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        // Update the document in Firestore
        userRef.update(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingsActivity.this, "Профиль обновлен успешно", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Ошибка при обновлении профиля: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
