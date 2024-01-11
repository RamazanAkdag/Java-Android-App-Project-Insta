package com.example.chatinstablog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatinstablog.Adapters.PostAdapter;
import com.example.chatinstablog.Models.Post;
import com.example.chatinstablog.Models.User;
import com.example.chatinstablog.R;
import com.example.chatinstablog.databinding.ActivityMyProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends AppCompatActivity {

    private ActivityMyProfileBinding binding;

    User user;
    ImageView imageViewProfile;
    TextView textViewUsername;
    TextView textViewFollowers;
    TextView textViewFollowing;
    Button btnFollow;
    RecyclerView recyclerViewPosts;

    PostAdapter postAdapter; // PostAdapter'ı tanımla
    List<Post> userPosts = new ArrayList<>(); // Kullanıcının gönderilerini tutacak liste

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageViewProfile = binding.imageViewMyProfileProfile;
        textViewUsername = binding.textViewUsername;
        textViewFollowers = binding.textViewFollowers;
        textViewFollowing = binding.textViewFollowing;
        btnFollow = binding.btnFollow;
        recyclerViewPosts = binding.recyclerViewPosts;

        // RecyclerView için layoutManager ve adapter tanımla
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewPosts.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(userPosts);
        recyclerViewPosts.setAdapter(postAdapter);


        fetchUserInfo();
        // Firestore'dan kullanıcının gönderilerini çek
        fetchUserPosts();

        // Diğer gerekli işlemleri buraya ekleyebilirsin
    }

    private void fetchUserInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userUid = currentUser.getUid();
        db.collection("Users").document(userUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    user = doc.toObject(User.class);

                    Glide.with(MyProfileActivity.this).load(user.ProfileImgUrl).into(imageViewProfile);
                    textViewUsername.setText(user.UserName);
                    textViewFollowers.setText("Takipçi : "+user.FollowerCount);
                    textViewFollowing.setText("Takip : "+user.FollowedCount);

                }
            }
        });

    }

    private void fetchUserPosts() {
        // Firestore'dan kullanıcının gönderilerini çek
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("Posts")
                    .whereEqualTo("userId", userId).orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        userPosts.clear(); // Önceki gönderileri temizle
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Post post = document.toObject(Post.class);
                            System.out.println("My profile çekilen user : " + post);
                            post.id = document.getId();
                            System.out.println("My profile çekilen user : " + post);
                            userPosts.add(post);
                        }
                        postAdapter.notifyDataSetChanged(); // Adapter'a değişiklikleri bildir
                    })
                    .addOnFailureListener(e -> {
                        // Hata durumunda yapılacak işlemler
                        Toast.makeText(this, "Gönderileri çekerken hata oluştu", Toast.LENGTH_SHORT).show();
                        System.out.println( e.getMessage());
                    });
        }
    }
}
