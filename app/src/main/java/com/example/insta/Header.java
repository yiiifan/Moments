package com.example.insta;

public class Header extends RecyclerViewItem{

    private String profile_username;
    private String profile_bio;
    private String profile_avatar;

    public Header(String profile_username, String profile_bio, String profile_avatar) {
        this.profile_username = profile_username;
        this.profile_bio = profile_bio;
        this.profile_avatar = profile_avatar;
    }

    public String getProfile_username() {
        return profile_username;
    }

    public void setProfile_username(String profile_username) {
        this.profile_username = profile_username;
    }

    public String getProfile_bio() {
        return profile_bio;
    }

    public void setProfile_bio(String profile_bio) {
        this.profile_bio = profile_bio;
    }

    public String getProfile_avatar() {
        return profile_avatar;
    }

    public void setProfile_avatar(String profile_avatar) {
        this.profile_avatar = profile_avatar;
    }
}
