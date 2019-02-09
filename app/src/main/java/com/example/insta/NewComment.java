package com.example.insta;

public class NewComment extends RecyclerViewItem{

    String uID;
    String pID;
    String content;
    String timestamp;

    public NewComment(String uID, String pID, String content, String timestamp) {
        this.uID = uID;
        this.pID = pID;
        this.content = content;
        this.timestamp = timestamp;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
