package com.jamburger.kitter.components;

public class Post {
    String publisher, postid, imageUrl, description;

    public Post() {

    }

    public Post(String publisher, String postid, String imageUrl, String description) {
        this.publisher = publisher;
        this.postid = postid;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
