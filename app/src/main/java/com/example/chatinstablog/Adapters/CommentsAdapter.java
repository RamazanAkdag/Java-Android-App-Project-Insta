package com.example.chatinstablog.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatinstablog.Models.Comment;
import com.example.chatinstablog.R;

import java.util.List;
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        Glide.with(holder.itemView.getContext())
                .load(comment.profileImageUrl)
                .into(holder.imageViewProfile);

        // Kullanıcı adını ayarla
        holder.textViewUsername.setText(comment.userName);

        // Yorum metnini ayarla
        holder.textViewComment.setText(comment.text);

        // TODO: ViewHolder içerisinde comment nesnesini görüntüleme işlemleri

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewProfile;
        public TextView textViewUsername;
        public TextView textViewComment;

        // TODO: Comment öğelerini tutan view elemanlarını tanımla

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewCommentsProfile);
            textViewUsername = itemView.findViewById(R.id.textViewCommentsUsername);
            textViewComment = itemView.findViewById(R.id.textViewCommentsComment);
            // TODO: View elemanlarını burada initialize et
        }
    }
}



