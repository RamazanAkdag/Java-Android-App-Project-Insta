package com.example.chatinstablog.Adapters;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        // Kullanıcı bilgilerini Firestore'dan alın
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(post.userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot userSnapshot) {
                String username = userSnapshot.getString("UserName");
                String profileImageUrl = userSnapshot.getString("ProfileImgUrl");

                // Verileri görünüme bağlayın
                try {

                    holder.textViewUsername.setText(username);

                    holder.textViewUsername2.setText(username);
                    Glide.with(holder.itemView.getContext())
                            .load(profileImageUrl)
                            .into(holder.imageViewProfile);

                    Glide.with(holder.itemView.getContext())
                            .load(profileImageUrl)
                            .into(holder.imageViewProfile2);

                    // Gönderi görselini yükleyin
                    Glide.with(holder.itemView.getContext())
                            .load(post.imageUri)
                            .into(holder.imageViewPost);



                    holder.textViewDescription.setText(post.description);
                    holder.textViewComments.setText(post.commentsCount);
                    holder.textViewLikes.setText(post.likesCount);

                }
                catch (Exception e){
                    System.out.println(e.getLocalizedMessage());
                }



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

        }
    }
}