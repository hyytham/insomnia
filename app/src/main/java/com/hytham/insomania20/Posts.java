package com.hytham.insomania20;

public class Posts {

    public String profileimage;
    public String time;
    public String uid;
    public String username;
    public String date;
    public String post;
    public String image;



    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public Posts (){

    }
    public Posts(String date, String post, String profileimage, String time, String uid, String username, String image) {
        this.date = date;
        this.post = post;
        this.profileimage = profileimage;
        this.time = time;
        this.uid = uid;
        this.username = username;
        this.image = image;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }




}
