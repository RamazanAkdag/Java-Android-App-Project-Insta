package com.example.chatinstablog.Models;


import com.google.firebase.Timestamp;

public class Following {
    public String followerId;
    public String followingId;
    public Timestamp timestamp;

    public Following(String followerId, String followingId, Timestamp timestamp) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.timestamp = timestamp;
    }

    public Following() {

    }

    @Override
    public String toString() {
        return "Followings{" +
                "followerId='" + followerId + '\'' +
                ", followingId='" + followingId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
