package com.example.insta;

public class DetailHolder extends RecyclerViewItem{
    private String uID;
    private String pID;
    private String photoURL;
    private String caption;
    private String timestamp;
    private String location;

    public DetailHolder(String uID, String pID, String photoURL, String caption, String timestamp, String location) {
        this.uID = uID;
        this.pID = pID;
        this.photoURL = photoURL;
        this.caption = caption;
        this.timestamp = timestamp;
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getpID() {
        return pID;
    }

    public void setpID(String pID) {
        this.pID = pID;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
