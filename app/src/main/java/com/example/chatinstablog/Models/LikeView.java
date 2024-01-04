package com.example.chatinstablog.Models;

public class LikeView {

    @Override
    public String toString() {
        return "LikeView{" +
                "likedUserProfileImage='" + likedUserProfileImage + '\'' +
                ", likedUserUsername='" + likedUserUsername + '\'' +
                '}';
    }

    public String likedUserProfileImage;
    public String likedUserUsername;

    public LikeView(String likedUserProfileImage, String likedUserUsername) {
        this.likedUserProfileImage = likedUserProfileImage;
        this.likedUserUsername = likedUserUsername;
    }

    public LikeView() {
    }
}
