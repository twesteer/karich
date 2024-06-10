package com.example.karich;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etPostTitle, etPostContent;
    private Button btnPublish;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etPostTitle = findViewById(R.id.et_post_title);
        etPostContent = findViewById(R.id.et_post_content);
        btnPublish = findViewById(R.id.btn_publish);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishPost();
            }
        });
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
