package com.jamburger.kitter.components;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Post {
    String creator, postid, imageUrl, caption;
    List<DocumentReference> likes;

    public Post(String creator, String postid, String imageUrl, String caption, List<DocumentReference> likes) {
        this.creator = creator;
        this.postid = postid;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likes = likes;
    }

    public List<DocumentReference> getLikes() {
        return likes;
    }

    public void setLikes(List<DocumentReference> likes) {
        this.likes = likes;
    }

    public Post() {
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
