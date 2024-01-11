package com.example.chatinstablog.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatinstablog.Models.Comment;
import com.example.chatinstablog.Models.CommentsDialog;
import com.example.chatinstablog.Models.LikeView;
import com.example.chatinstablog.Models.Post;
import com.example.chatinstablog.Models.User;
import com.example.chatinstablog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    Context context;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    private List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_post_item, parent, false);
        context = parent.getContext();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        // Kullanıcı bilgilerini al

        DocumentReference userRef = db.collection("Users").document(post.userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot userSnapshot) {
                String username = userSnapshot.getString("UserName");
                String profileImageUrl = userSnapshot.getString("ProfileImgUrl");

                // Verileri view
                try {

                    holder.textViewUsername.setText(username);

                    holder.textViewUsername2.setText(username);
                    Glide.with(holder.itemView.getContext())
                            .load(profileImageUrl)
                            .into(holder.imageViewProfile);

                    Glide.with(holder.itemView.getContext())
                            .load(profileImageUrl)
                            .into(holder.imageViewProfile2);

                    // Gönderi resmi
                    Glide.with(holder.itemView.getContext())
                            .load(post.imageUri)
                            .into(holder.imageViewPost);

                    //beğeni balonunun renklerini ayarla
                    isPostLikedByCurrentUser(holder.getAdapterPosition(), holder);

                    holder.textViewDescription.setText(post.description);

                    holder.textViewComments.setText(String.valueOf(post.commentsCount));
                    holder.textViewLikes.setText(String.valueOf(post.likesCount));

                } catch (Exception e) {
                    System.out.println(e.getLocalizedMessage());
                }
                holder.imageViewLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //like basılan post beğenilecek
                        int updatedPosition = holder.getAdapterPosition();


                        onLikeClicked(updatedPosition);

                    }
                });

                holder.textViewLikes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int updatedPosition = holder.getAdapterPosition();

                        showLikesDialog(updatedPosition);

                    }
                });
                holder.imageViewComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Yorumları gösteren bir diyalog penceresi oluştur
                        int position = holder.getAdapterPosition();
                        showCommentsDialog(position);
                    }
                });



            }


        });


    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageViewProfile;

        public TextView textViewUsername;

        public TextView textViewUsername2;
        public ImageView imageViewPost;
        public TextView textViewDescription;
        public TextView textViewLikes;
        public TextView textViewComments;

        public ImageView imageViewProfile2;

        public ImageView imageViewLike;
        public ImageView imageViewComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            imageViewProfile2 = itemView.findViewById(R.id.imageViewProfile2);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            imageViewPost = itemView.findViewById(R.id.imageViewPost);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewLikes = itemView.findViewById(R.id.textViewLikes);
            textViewComments = itemView.findViewById(R.id.textViewComments);
            textViewUsername2 = itemView.findViewById(R.id.textViewUsername2);
            imageViewLike = itemView.findViewById(R.id.imageViewLike);
            imageViewComment = itemView.findViewById(R.id.imageViewComment);


        }
    }


    /*public void showLikesDialog(int position){
        // Tıklanan postun likes listesini alma
        Post post = posts.get(position);

        // Tablolara referans
        CollectionReference likesRef = db.collection("Likes");
        CollectionReference usersRef = db.collection("Users");

        // Postu beğenen kullanıcılar
        Query query = likesRef.whereEqualTo("PostId", post.id);

        List<LikeView> likeViews = new ArrayList<>();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //her bir like ın kullanıcısını alacağız
                    for (QueryDocumentSnapshot likeSnapshot : task.getResult()) {
                        // Kullanıcı id
                        String userId = likeSnapshot.get("UserId",String.class);


                        //kullanıcıyı çekme
                        DocumentReference userRef = usersRef.document(userId);
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Kullanıcıyı listeye ekle
                                    DocumentSnapshot userSnapshot = task.getResult();
                                    User user = userSnapshot.toObject(User.class);

                                    LikeView likeView = new LikeView(user.ProfileImgUrl, user.UserName);
                                    likeViews.add(likeView);//en üst for döngüsü bitince kullanıcıları setleyeceğim fakat nasıl yapacağım


                                }
                            }
                        });
                    }

                    // Tüm işlemler bitince yapılacak işlemler
                    if (task.isSuccessful()) {

                        // Dialog'u oluştur
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Beğenen Kişiler");

                        // RecyclerView oluştur
                        RecyclerView recyclerView = new RecyclerView(context);
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));

                        // Adapter oluştur
                        LikesAdapter adapter = new LikesAdapter(likeViews);



                        // RecyclerView'e adapter'ı setle
                        recyclerView.setAdapter(adapter);

                        // RecyclerView'i dialog'a setle
                        builder.setView(recyclerView);
                        builder.show();
                    }

                }


            }
        });









    }*/

    public void showLikesDialog(int position) {
        Post post = posts.get(position);

        CollectionReference likesRef = db.collection("Likes");
        CollectionReference usersRef = db.collection("Users");

        Query query = likesRef.whereEqualTo("PostId", post.id);
        //begenen herkesi getiriyoruz
        query.orderBy("Timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LikeView> likeViews = new ArrayList<>();
                    //queryDocumentSnapshots.size kadar kişi beğenmiş
                    System.out.println("queryDocumentSnapshots sayisi : " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot likeSnapshot : queryDocumentSnapshots) {
                        String userId = likeSnapshot.getString("UserId");

                        DocumentReference userRef = usersRef.document(userId);

                        userRef.get()
                                .addOnSuccessListener(userSnapshot -> {
                                    User user = userSnapshot.toObject(User.class);
                                    System.out.println("begenen kullanici : " + user);
                                    if (user != null) {
                                        LikeView likeView = new LikeView(user.ProfileImgUrl, user.UserName);
                                        likeViews.add(likeView);
                                    }

                                    //System.out.println("Likeviews sayısı  : "+likeViews.size() );

                                    //kullanıcıların sayısı likeviewse eşitse
                                    if (likeViews.size() == queryDocumentSnapshots.size()) {
                                        // Tüm işlemler tamamlandığında yapılacak işlemler
                                        System.out.println("Fonksiyon dialog cagiriliyor");
                                        createLikesDialog(likeViews);
                                    }
                                })
                                .addOnFailureListener(e -> {

                                });
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println(e.getMessage());
                });
    }

    private void createLikesDialog(List<LikeView> likeViews) {
        System.out.println("Like Dialog oluşturuluyor");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Beğenen Kişiler");

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        LikesAdapter adapter = new LikesAdapter(likeViews);
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        builder.show();
        System.out.println("Like dialog oluşturuldu");
    }






    public void onLikeClicked(int position){

        Post post = posts.get(position);
        System.out.println(post + " like basildi");

        //şu anki kullanıcının idsi

        String currentUserId = currentUser.getUid();


        // Kullanıcının bu postu zaten beğenip beğenmediğini kontrol et
        db.collection("Likes")
                .whereEqualTo("PostId", post.id)
                .whereEqualTo("UserId", currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Kullanıcı bu postu henüz beğenmemiş

                                // Like sayısını artır
                                post.likesCount++;

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("likesCount", post.likesCount);
                                db.collection("Posts").document(post.id).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Likes tablosuna kaydet
                                            Map<String, Object> likeData = new HashMap<>();
                                            likeData.put("PostId", post.id);
                                            likeData.put("Timestamp", FieldValue.serverTimestamp());
                                            likeData.put("UserId", currentUserId);
                                            db.collection("Likes").add(likeData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if (task.isSuccessful()) {
                                                        System.out.println("begenme basariliiii");

                                                        System.out.println("new like count : " + post.likesCount);
                                                        notifyItemChanged(position);
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });


                            } else {
                                // Kullanıcı bu postu zaten beğenmiş
                                System.out.println("Bu postu zaten beğendiniz");
                            }
                        } else {
                            // Hata oluştu
                            System.out.println("Hata: " + task.getException());
                        }
                    }
                });

    }


    public void isPostLikedByCurrentUser(int position, PostViewHolder holder) {

        Post post = posts.get(position);


        String currentUserId = currentUser.getUid();
        // Kullanıcının bu postu beğendiğini kontrol et
        db.collection("Likes")
                .whereEqualTo("PostId", post.id)
                .whereEqualTo("UserId", currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Kullanıcı bu postu henüz beğenmemiş

                            } else {
                                // Kullanıcı bu postu beğenmiş
                                holder.imageViewLike.setColorFilter(Color.RED);
                            }
                        } else {
                            // Hata oluştu
                            System.out.println("Hata: " + task.getException());
                        }
                    }
                });


    }
    private void showCommentsDialog(int position) {
        // Seçilen gönderiye ait yorumları Firebase'den al ve CommentsDialog'u başlat
        Post post = posts.get(position);



        db.collection("comments")
                .whereEqualTo("postId", post.id).orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Comment comment = document.toObject(Comment.class);
                            System.out.println(comment);
                            comments.add(comment);


                        }



                        // CommentsDialog'u başlat

                        CommentsDialog commentsDialog = new CommentsDialog(context, comments,post.id,post,PostAdapter.this,position);
                        commentsDialog.show();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hata durumunda yapılacak işlemler
                        Toast.makeText(context, "Yorumları alırken hata oluştu", Toast.LENGTH_SHORT).show();
                        System.out.println(e.getMessage());
                    }
                });
    }}


