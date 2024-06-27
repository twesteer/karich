package com.example.karich;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Glavnaya extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glavnaya);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        postList = new ArrayList<>();
        postsAdapter = new PostsAdapter(postList);
        recyclerView.setAdapter(postsAdapter);

        db = FirebaseFirestore.getInstance();
        loadPosts();
    }

    private void loadPosts() {
        db.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    postList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        postList.add(post);
                    }
                    postsAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
