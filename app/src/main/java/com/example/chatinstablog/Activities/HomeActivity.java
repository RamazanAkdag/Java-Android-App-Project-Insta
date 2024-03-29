package com.example.chatinstablog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatinstablog.Adapters.PostAdapter;
import com.example.chatinstablog.Models.Following;
import com.example.chatinstablog.Models.Post;
import com.example.chatinstablog.Models.User;
import com.example.chatinstablog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth auth;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    private RecyclerView recyclerView;

    FirebaseFirestore db;

    ImageView navUserPhoto;

    TextView navUserName;

    TextView navUserEmail;

    View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar_home);

        // ActionBar olarak kullan
        setSupportActionBar(toolbar);



        // ActionBar'ın geri butonunu göster
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = findViewById(R.id.homeNavView);


        /*nav viewin headerindekileri veritabanından alma*/
        getNavViewHeaders();





        recyclerView = findViewById(R.id.recyclerViewHomePosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();


        // Kullanıcının takip ettiği kullanıcıların ID'lerini içeren liste
        List<String> followingUserIds = new ArrayList<>();

// Kullanıcının takip ettiği kullanıcıların ID'lerini Firestore'dan çekme
        db.collection("Followings")
                .whereEqualTo("followerId", auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Following following = documentSnapshot.toObject(Following.class);
                        followingUserIds.add(following.followingId);
                    }

                    // Kullanıcının takip ettiği kullanıcıların gönderilerini çek
                    if (followingUserIds.isEmpty()) {
                        // Takip edilen kimse yoksa ekranda bir mesaj göster
                        Toast.makeText(HomeActivity.this, "Görünüşe göre takip listende kimse yok. Hemen Takibe başla!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(HomeActivity.this,WorldActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        db.collection("Posts")
                                .whereIn("userId", followingUserIds)
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    List<Post> posts = new ArrayList<>();
                                    for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1) {
                                        Post post = documentSnapshot1.toObject(Post.class);
                                        post.id = documentSnapshot1.getId();
                                        posts.add(post);
                                    }

                                    // Gönderileri RecyclerView'da göster
                                    PostAdapter adapter = new PostAdapter(posts);
                                    recyclerView.setAdapter(adapter);
                                })
                                .addOnFailureListener(e -> {
                                    // Hata durumunda yapılacak işlemler
                                    Toast.makeText(HomeActivity.this, "Gönderileri çekerken hata oluştu", Toast.LENGTH_SHORT).show();
                                    Log.e("TAG", "Error getting posts: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda yapılacak işlemler
                    Toast.makeText(HomeActivity.this, "Takip edilen kullanıcıları çekerken hata oluştu", Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "Error getting following users: " + e.getMessage());
                });


        // NavigationView'a bir Listener ekle
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_logout) {
                    logout();
                    //Toast.makeText(HomeActivity.this, "Nav Item 1 Tıklandı", Toast.LENGTH_SHORT).show();
                }
                if(id == R.id.nav_world){
                    Intent intent = new Intent(HomeActivity.this,WorldActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(id == R.id.nav_add_post){
                    goToAddPostActivity();
                }
                if (id==R.id.nav_profile){
                    goToMyProfileActivity();
                }


                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }


        });
    }

    public void goToMyProfileActivity() {
        Intent intent = new Intent(HomeActivity.this,MyProfileActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout(){
        auth.signOut();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToAddPostActivity(){
        Intent intent = new Intent(HomeActivity.this,AddPostActivity.class);
        startActivity(intent);
        finish();
    }

    public void getNavViewHeaders(){

        headerView = navigationView.getHeaderView(0);
        navUserName = headerView.findViewById(R.id.nav_user_name);
        navUserPhoto = headerView.findViewById(R.id.nav_user_photo);
        navUserEmail = headerView.findViewById(R.id.nav_user_email);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        System.out.println("userid : " + uid);

        DocumentReference docRef = db.collection("Users").document(uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()){
                        User user = document.toObject(User.class);
                        System.out.println(user);
                        navUserName.setText(user.UserName);
                        navUserEmail.setText(user.Email);
                        Glide.with(HomeActivity.this).load(user.ProfileImgUrl).into(navUserPhoto);
                    }





                } else {
                    System.out.println("get failed with "+ task.getException());
                }
            }
        });

    }
}