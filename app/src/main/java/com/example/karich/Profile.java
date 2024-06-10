package com.example.karich;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {

    private ImageView ivProfilePicture;
    private TextView tvUsername, tvUserDescription;
    private Button btnPosts, btnPhotos, btnSettings, btnLogout, navAdd;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        tvUsername = findViewById(R.id.tv_username);
        tvUserDescription = findViewById(R.id.tv_user_description);
        btnPosts = findViewById(R.id.btn_posts);
        btnPhotos = findViewById(R.id.btn_photos);
        btnSettings = findViewById(R.id.btn_settings);
        btnLogout = findViewById(R.id.btn_logout);
        navAdd = findViewById(R.id.nav_add);

        btnPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Загрузка фрагмента с публикациями
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new PostsFragment())
                        .commit();
            }
        });

        btnPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Загрузка фрагмента с фотографиями
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new PhotosFragment())
                        .commit();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открытие настроек
                Intent intent = new Intent(Profile.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Логаут
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Profile.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        navAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открытие CreatePostActivity
                Intent intent = new Intent(Profile.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });

        // Загрузка данных профиля
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String username = document.getString("username");
                        String bio = document.getString("bio");
                        String profilePictureUrl = document.getString("profilePictureUrl");

                        tvUsername.setText(username != null && !username.isEmpty() ? username : user.getEmail());
                        tvUserDescription.setText(bio != null ? bio : "");

                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Picasso.get().load(profilePictureUrl).placeholder(R.drawable.default_avatar).into(ivProfilePicture);
                        } else {
                            ivProfilePicture.setImageResource(R.drawable.default_avatar);
                        }
                    } else {
                        Toast.makeText(Profile.this, "Документ не существует", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Profile.this, "Ошибка получения данных: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
