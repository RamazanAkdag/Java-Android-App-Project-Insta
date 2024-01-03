package com.example.chatinstablog.Models;

import com.google.firebase.Timestamp;

public class Post {
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


}
