package com.example.insta;

public class CommentHolder extends RecyclerViewItem{
    String cID;
    String uID;
    String pID;
    String author;
    String content;
    String timestamp;
    String avatar;
    String username;

    public CommentHolder(String cID, String uID, String pID, String author, String content, String timestamp, String avatar, String username) {
        this.cID = cID;
        this.uID = uID;
        this.pID = pID;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        this.avatar = avatar;
        this.username = username;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getcID() {
        return cID;
    }

    public void setcID(String cID) {
        this.cID = cID;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
