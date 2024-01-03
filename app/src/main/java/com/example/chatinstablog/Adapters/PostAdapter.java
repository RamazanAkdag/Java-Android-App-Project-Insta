package com.example.chatinstablog.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatinstablog.Models.Post;
import com.example.chatinstablog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {


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
                    isPostLikedByCurrentUser(holder.getAdapterPosition(),holder);

                    holder.textViewDescription.setText(post.description);

                    holder.textViewComments.setText(String.valueOf(post.commentsCount) );
                    holder.textViewLikes.setText(String.valueOf(post.likesCount));

                }
                catch (Exception e){
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
                                        if (task.isSuccessful()){
                                            // Likes tablosuna kaydet
                                            Map<String, Object> likeData = new HashMap<>();
                                            likeData.put("PostId", post.id);
                                            likeData.put("Timestamp", FieldValue.serverTimestamp());
                                            likeData.put("UserId", currentUserId);
                                            db.collection("Likes").add(likeData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if (task.isSuccessful()){
                                                        System.out.println("begenme basariliiii");

                                                        System.out.println("new like count : "+post.likesCount);
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



    public void isPostLikedByCurrentUser(int position,PostViewHolder holder){

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


}
