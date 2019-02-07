package com.example.insta;

public class GlobalPhotoHolder extends RecyclerViewItem{

    private String uID;
    private String pID;
    private String url;
    private String location;
    private String timestamp;
    private String description;

    public GlobalPhotoHolder(String uID, String pID, String url, String location, String timestamp, String description) {
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

    public String getUrl() {
        return url;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
