package com.jamburger.kitter.components;

public class Post {
    String creator, postid, imageUrl, caption;
    int likes;

    public Post(String creator, String postid, String imageUrl, String caption, int likes) {
        this.creator = creator;
        this.postid = postid;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likes = likes;
    }

    public Post() {
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getPostid() {
        return postid;
    }

    public String getCreator() {
        return creator;
    }


    public String getImageUrl() {
        return imageUrl;
    }


    public String getCaption() {
        return caption;
    }

}
