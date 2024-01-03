package com.example.chatinstablog.Models;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

public class Post {

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", commentsCount=" + commentsCount +
                ", likesCount=" + likesCount +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String id;
    public String description;
    public String imageUri;
    public int commentsCount;
    public int likesCount;
    public Timestamp timestamp;
    public String userId;

    public Post(){

    }

    public Post(String description, String imageUri, int commentsCount, int likesCount, Timestamp timestamp, String userId) {
        this.description = description;
        this.imageUri = imageUri;
        this.commentsCount = commentsCount;
        this.likesCount = likesCount;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public Post(String id,String description, String imageUri, int commentsCount, int likesCount, Timestamp timestamp, String userId) {
        this.id = id;
        this.description = description;
        this.imageUri = imageUri;
        this.commentsCount = commentsCount;
        this.likesCount = likesCount;
        this.timestamp = timestamp;
        this.userId = userId;
    }


}
