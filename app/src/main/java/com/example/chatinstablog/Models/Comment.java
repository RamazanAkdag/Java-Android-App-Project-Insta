package com.example.chatinstablog.Models;

import android.net.Uri;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Comment {

    public String postId;
    public String userId;
    public String userName;
    public String profileImageUrl;
    public String text;
    public Timestamp timestamp;

    @Override
    public String toString() {
        return "Comment{" +
                "postId='" + postId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public Comment(String postId, String userId, String userName, String profileImageUrl, String text, Timestamp timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Comment(){

    }

}
