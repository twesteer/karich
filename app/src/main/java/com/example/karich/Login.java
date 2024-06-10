package com.example.karich;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private EditText etEmailPhone;
    private EditText etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация Firebase Auth и Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Инициализация элементов интерфейса
        etEmailPhone = findViewById(R.id.et_email_phone);
        etPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailPhone.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(email, password);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Добавьте здесь логику для восстановления пароля
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Вход успешен
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                DocumentReference docRef = db.collection("users").document(userId);
                                docRef.get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document = task1.getResult();
                                        if (document != null && document.exists()) {
                                            // Документ существует, переходим к профилю
                                            Toast.makeText(Login.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Login.this, Profile.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Документ не существует, создаем новый
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("username", email);
                                            userData.put("bio", "");
                                            userData.put("profilePictureUrl", "");

                                            docRef.set(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(Login.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(Login.this, Profile.class);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(Login.this, "Ошибка создания документа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(Login.this, "Ошибка получения документа: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            // Ошибка входа
                            Toast.makeText(Login.this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
