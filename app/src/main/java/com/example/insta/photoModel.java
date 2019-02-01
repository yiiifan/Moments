package com.example.insta;

import android.net.Uri;

public class photoModel extends RecyclerViewItem{
    private String pID;
    private String url;
    private String location;
    private String timestamp;

    public photoModel() {

    }

    public photoModel(String pID, String url, String location, String timestamp) {
        this.pID = pID;
        this.url = url;
        this.location = location;
        this.timestamp = timestamp;
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
