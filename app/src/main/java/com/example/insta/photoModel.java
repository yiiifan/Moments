package com.example.insta;

import android.net.Uri;

public class photoModel extends RecyclerViewItem{
    private String uID;
    private String pID;
    private String url;
    private String location;
    private String timestamp;
    private String description;

    public photoModel() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public photoModel(String uID, String pID, String url, String location, String timestamp, String description) {
        this.uID = uID;
        this.pID = pID;
        this.url = url;
        this.location = location;
        this.timestamp = timestamp;
        this.description = description;
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


    public Uri getUrl() {
        return Uri.parse(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
