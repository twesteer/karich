package com.example.karich; // Объявление пакета

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etPostTitle, etPostContent;
    private Button btnPublish, btnChooseImage;
    private ImageView ivPostImage;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etPostTitle = findViewById(R.id.et_post_title);
        etPostContent = findViewById(R.id.et_post_content);
        btnPublish = findViewById(R.id.btn_publish);
        btnChooseImage = findViewById(R.id.btn_choose_image);
        ivPostImage = findViewById(R.id.iv_post_image);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishPost();
            }
        });
    }

    private void openImageChooser() {
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
            Picasso.get().load(selectedImageUri).into(ivPostImage);
        }
    }

    private void publishPost() {
        String title = etPostTitle.getText().toString().trim();
        String content = etPostContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etPostTitle.setError("Заголовок обязателен");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            etPostContent.setError("Содержание обязательно");
            return;
        }

        if (selectedImageUri != null) {
            StorageReference imageReference = storageReference.child("postImages").child(selectedImageUri.getLastPathSegment());
            imageReference.putFile(selectedImageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> downloadTask) {
                                        if (downloadTask.isSuccessful()) {
                                            String imageUrl = downloadTask.getResult().toString();
                                            savePostToFirestore(title, content, imageUrl);
                                        } else {
                                            Toast.makeText(CreatePostActivity.this, "Ошибка получения URL изображения: " + downloadTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(CreatePostActivity.this, "Ошибка загрузки изображения: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            savePostToFirestore(title, content, null);
        }
    }

    private void savePostToFirestore(String title, String content, String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String postId = db.collection("posts").document().getId();

            Map<String, Object> post = new HashMap<>();
            post.put("title", title);
            post.put("content", content);
            post.put("userId", userId);
            post.put("postId", postId);
            post.put("timestamp", System.currentTimeMillis());
            if (imageUrl != null) {
                post.put("imageUrl", imageUrl);
            }

            db.collection("posts").document(postId).set(post)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CreatePostActivity.this, "Пост опубликован", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(CreatePostActivity.this, "Ошибка публикации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}