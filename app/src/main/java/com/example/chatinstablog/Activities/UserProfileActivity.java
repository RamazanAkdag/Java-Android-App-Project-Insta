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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatinstablog.Adapters.PostAdapter;
import com.example.chatinstablog.Models.Following;
import com.example.chatinstablog.Models.Post;
import com.example.chatinstablog.Models.User;
import com.example.chatinstablog.R;
import com.example.chatinstablog.databinding.ActivityMyProfileBinding;
import com.example.chatinstablog.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {



    User user;
    User currentUser;
    String userId;
    Toolbar toolbar;
    ImageView imageViewProfile;
    TextView textViewUsername;
    TextView textViewFollowers;
    TextView textViewFollowing;
    RecyclerView recyclerViewPosts;
    Button followButton;


    FirebaseFirestore db;
    FirebaseAuth auth;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    PostAdapter postAdapter; // PostAdapter'ı tanımla
    List<Post> userPosts = new ArrayList<>(); // Kullanıcının gönderilerini tutacak liste

    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //intent in içerisinden ekstra gönderilen user id değerini aldık veri tabanından buna göre çekicez
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageViewProfile = binding.imageViewUserProfileProfile;
        textViewUsername = binding.textViewUserProfileUsername;
        textViewFollowers = binding.textViewUserProfileFollowers;
        textViewFollowing = binding.textViewUserProfileFollowing;
        recyclerViewPosts = binding.recyclerViewUserProfilePosts;
        followButton = binding.buttonUserProfileFollow;
        //db baslat
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        // RecyclerView için layoutManager ve adapter tanımlama
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewPosts.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(userPosts);
        recyclerViewPosts.setAdapter(postAdapter);


        toolbar = findViewById(R.id.toolbar_user_profile);
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
        navigationView = findViewById(R.id.UserProfileNavView);

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
                    Intent intent = new Intent(UserProfileActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(id == R.id.nav_world){
                    Intent intent = new Intent(UserProfileActivity.this,WorldActivity.class);
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

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Takip durumuna göre işlem yap
                if (followButton.getText().equals("Takip Et")) {
                    followUser(); // Takip etme işlemi
                } else if (followButton.getText().equals("Takipten Çık")) {
                    unfollowUser(); // Takipten çıkma işlemi
                }
            }
        });

        // ActionBarDrawerToggle'ın durumunu senkronize et (hamburger icon vs.)
        drawerToggle.syncState();

        updateFollowButton();
        getNavViewHeaders();
        //firestoreden kullanıcının profil bilgilerini çek
        fetchUserInfo();

        // Firestore'dan kullanıcının gönderilerini çekme
        fetchUserPosts();


    }

    public void goToMyProfileActivity() {
        Intent intent = new Intent(UserProfileActivity.this,MyProfileActivity.class);
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
        Intent intent = new Intent(UserProfileActivity.this,AddPostActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchUserInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userUid = userId;
        //kullanııcnın id sine göre bilgilerini çekicez

        db.collection("Users").document(userUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    user = doc.toObject(User.class);

                    Glide.with(UserProfileActivity.this).load(user.ProfileImgUrl).into(imageViewProfile);
                    textViewUsername.setText(user.UserName);
                    textViewFollowers.setText("Takipçi : "+user.FollowerCount);
                    textViewFollowing.setText("Takip : "+user.FollowedCount);
                    toolbar.setTitle(user.UserName);
                }
            }
        });

    }

    private void fetchUserPosts() {
        // Firestore'dan kullanıcının gönderilerini çek
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //intenle aldığımız userid ye göre postları çekicez
            db.collection("Posts")
                    .whereEqualTo("userId", userId).orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        userPosts.clear(); // Önceki gönderileri temizle
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Post post = document.toObject(Post.class);
                            System.out.println(" User profile çekilen user : " + post);
                            post.id = document.getId();
                            System.out.println("User profile çekilen user : " + post);
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

    public void followUser() {
        String currentUserId = auth.getCurrentUser().getUid();
        String followingCollectionPath = "Followings";

        // Kullanıcının daha önce takip edip etmediğini kontrol etmek için sorgu
        db.collection(followingCollectionPath)
                .whereEqualTo("followerId", currentUserId)
                .whereEqualTo("followingId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Sorgu sonucunda belgeler varsa kullanıcı zaten takip edilmiş demektir
                            if (task.getResult().isEmpty()) {
                                // Kullanıcı daha önce takip etmemişse işlemi gerçekleştir
                                Following following = new Following(currentUserId, userId, Timestamp.now());
                                db.collection(followingCollectionPath).add(following)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    // Kullanıcıların takip takipçi sayılarını güncelleme
                                                    db.collection("Users").document(currentUserId).update("FollowedCount", FieldValue.increment(1));
                                                    db.collection("Users").document(userId).update("FollowerCount", FieldValue.increment(1));

                                                    //Takipçilerin var olan değerini 1 artıracağız
                                                    String[] textSplit = textViewFollowers.getText().toString().split(" ");
                                                    String text = textSplit[2];
                                                    int newFollowerCount = Integer.valueOf(text) + 1;
                                                    textViewFollowers.setText("Takipçi : "+ newFollowerCount );

                                                    updateFollowButton();
                                                } else {
                                                    System.out.println("Takip işlemi başarısız: " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            } else {
                                System.out.println("Kullanıcı zaten takip edilmiş.");
                            }
                        } else {
                            System.out.println("Takip durumu kontrolü başarısız: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void updateFollowButton() {
        String currentUserId = auth.getCurrentUser().getUid();
        String followingCollectionPath = "Followings";

        // Kullanıcının takip durumunu kontrol etmek için sorgu
        db.collection(followingCollectionPath)
                .whereEqualTo("followerId", currentUserId)
                .whereEqualTo("followingId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Sorgu sonucunda belgeler varsa kullanıcı zaten takip edilmiş demektir
                            if (task.getResult().isEmpty()) {
                                // Kullanıcı daha önce takip etmemişse "Takip Et" metnini ve işlemi göster
                                followButton.setText("Takip Et");
                            } else {
                                // Kullanıcı zaten takip ediliyorsa "Takipten Çık" metnini ve işlemi göster
                                followButton.setText("Takipten Çık");
                            }
                        } else {
                            System.out.println("Takip durumu kontrolü başarısız: " + task.getException().getMessage());
                        }
                    }
                });
    }

    public void unfollowUser() {
        // Takipten çıkma işlemi
        String currentUserId = auth.getCurrentUser().getUid();
        String followingCollectionPath = "Followings";

        // Kullanıcının takip durumunu kontrol etmek için sorgu
        db.collection(followingCollectionPath)
                .whereEqualTo("followerId", currentUserId)
                .whereEqualTo("followingId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Sorgu sonucunda belgeler varsa kullanıcı zaten takip edilmiş demektir
                            if (!task.getResult().isEmpty()) {
                                // Takipten çıkma işlemi gerçekleştir
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Document'ı silerek takipten çıkma işlemi
                                    db.collection(followingCollectionPath).document(document.getId()).delete();
                                }

                                // Kullanıcıların takip takipçi sayılarını güncelleme
                                db.collection("Users").document(currentUserId).update("FollowedCount", FieldValue.increment(-1));
                                db.collection("Users").document(userId).update("FollowerCount",FieldValue.increment(-1));

                                //Takipçilerin var olan değerini 1 azalt
                                String[] textSplit = textViewFollowers.getText().toString().split(" ");
                                String text = textSplit[2];
                                int newFollowerCount = Integer.valueOf(text) - 1;
                                textViewFollowers.setText("Takipçi : "+ newFollowerCount );
                            }
                        } else {
                            System.out.println("Takip durumu kontrolü başarısız: " + task.getException().getMessage());
                        }

                        // Takip butonunu güncelle
                        updateFollowButton();
                    }
                });
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
                        Glide.with(UserProfileActivity.this).load(user.ProfileImgUrl).into(navUserPhoto);
                    }





                } else {
                    System.out.println("get failed with "+ task.getException());
                }
            }
        });
    }
}
