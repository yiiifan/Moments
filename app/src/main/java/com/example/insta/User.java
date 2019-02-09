package com.example.insta;

public class User {
    private String uID;
    private String username;
    private String avatar;
    private String bio;

    public User(String uID, String username, String avatar, String bio) {
        this.uID = uID;
        this.username = username;
        this.avatar = avatar;
        this.bio = bio;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
