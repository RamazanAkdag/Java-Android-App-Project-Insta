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
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    RecyclerView recyclerViewPosts;

    FirebaseFirestore db;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

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
        recyclerViewPosts = binding.recyclerViewPosts;
        //db baslat
        db = FirebaseFirestore.getInstance();
        // RecyclerView için layoutManager ve adapter tanımlama
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewPosts.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(userPosts);
        recyclerViewPosts.setAdapter(postAdapter);


        Toolbar toolbar = findViewById(R.id.toolbar_my_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // DrawerLayout'ı bul
        drawerLayout = findViewById(R.id.drawer_layout_profile);

        // ActionBarDrawerToggle oluştur ve DrawerLayout ile bağla
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.nav_open,
                R.string.nav_close
        );

        // ActionBarDrawerToggle'ı DrawerLayout'a ekleyin
        drawerLayout.addDrawerListener(drawerToggle);

        // NavigationView'ı bul
        navigationView = findViewById(R.id.MyProfileNavView);

        // NavigationView'da bir öğe tıklandığında ne yapılacağını belirtmek için bir Listener ekleyin
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_logout) {
                    logout();
                    //Toast.makeText(HomeActivity.this, "Nav Item 1 Tıklandı", Toast.LENGTH_SHORT).show();
                }
                if(id == R.id.nav_to_home_activity){
                    Intent intent = new Intent(MyProfileActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(id == R.id.nav_world){
                    Intent intent = new Intent(MyProfileActivity.this,WorldActivity.class);
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

        // ActionBarDrawerToggle'ın durumunu senkronize et (hamburger icon vs.)
        drawerToggle.syncState();

        getNavViewHeaders();
        //firestoreden kullanıcının profil bilgilerini çek
        fetchUserInfo();
        // Firestore'dan kullanıcının gönderilerini çekme
        fetchUserPosts();


    }

    public void goToMyProfileActivity() {
        Intent intent = new Intent(MyProfileActivity.this,MyProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void logout(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToAddPostActivity(){
        Intent intent = new Intent(MyProfileActivity.this,AddPostActivity.class);
        startActivity(intent);
        finish();
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

    public void getNavViewHeaders(){

        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_user_name);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);
        TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);

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
                        Glide.with(MyProfileActivity.this).load(user.ProfileImgUrl).into(navUserPhoto);
                    }





                } else {
                    System.out.println("get failed with "+ task.getException());
                }
            }
        });
    }
}
