package com.example.chatinstablog.Models;

public class User {



    public String Email;
    public String FirstName;
    public int FollowedCount;
    public int FollowerCount;

    public String LastName;

    public int Posts;
    public String ProfileImgUrl;

    public String UserName;





    public User(){

    }

    public User(String email, String firstName, int followedCount, int followerCount, String lastName, int posts, String profileImgUrl, String userName) {
        Email = email;
        FirstName = firstName;
        FollowedCount = followedCount;
        FollowerCount = followerCount;
        LastName = lastName;
        Posts = posts;
        ProfileImgUrl = profileImgUrl;
        UserName = userName;
    }

    @Override
    public String toString() {
        return "User{" +
                "Email='" + Email + '\'' +
                ", FirstName='" + FirstName + '\'' +
                ", FollowedCount=" + FollowedCount +
                ", FollowerCount=" + FollowerCount +
                ", LastName='" + LastName + '\'' +
                ", Posts=" + Posts +
                ", ProfileImgUrl='" + ProfileImgUrl + '\'' +
                ", UserName='" + UserName + '\'' +
                '}';
    }
}
