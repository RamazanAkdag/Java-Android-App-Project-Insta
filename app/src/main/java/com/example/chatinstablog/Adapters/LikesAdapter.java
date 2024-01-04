package com.example.chatinstablog.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatinstablog.Models.LikeView;
import com.example.chatinstablog.R;

import java.util.List;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.LikesViewHolder> {

    private List<LikeView> likes;

    public LikesAdapter(List<LikeView> likes) {
        this.likes = likes;
    }

    @NonNull
    @Override
    public LikesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_like_item, parent, false);
        return new LikesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LikesViewHolder holder, int position) {
        LikeView like = likes.get(position);

        holder.textViewLikesUsername.setText(like.likedUserUsername);
        Glide.with(holder.itemView.getContext())
                .load(like.likedUserProfileImage)
                .into(holder.imageViewLikesProfile);

    }

    @Override
    public int getItemCount() {
        return likes.size();
    }

    public class LikesViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageViewLikesProfile;
        public TextView textViewLikesUsername;




        public LikesViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewLikesProfile = itemView.findViewById(R.id.imageViewLikesProfile);
            textViewLikesUsername = itemView.findViewById(R.id.textViewLikesUsername);


        }
    }
}