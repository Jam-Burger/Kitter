package com.jamburger.kitter.components;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class Post {
    String creator, postid, imageUrl, caption;
    List<DocumentReference> likes;
    List<Comment> comments;

    public Post() {
    }

    public Post(String creator, String postid, String imageUrl, String caption) {
        this.creator = creator;
        this.postid = postid;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likes = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<DocumentReference> getLikes() {
        return likes;
    }

    public void setLikes(List<DocumentReference> likes) {
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
