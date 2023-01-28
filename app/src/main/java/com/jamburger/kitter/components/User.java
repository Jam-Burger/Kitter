package com.jamburger.kitter.components;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class User {
    String id, name, username, email, bio, profileImageUrl, backgroundImageUrl;
    List<DocumentReference> posts;
    List<DocumentReference> saved;

    public User() {

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public User(String id, String name, String username, String email, String profileImageUrl, String backgroundImageUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.bio = "";
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.posts = new ArrayList<>();
        this.saved = new ArrayList<>();
    }

    public List<DocumentReference> getSaved() {
        return saved;
    }

    public String getEmail() {
        return email;
    }

    public List<DocumentReference> getPosts() {
        return posts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }


    public String getBio() {
        return bio;
    }


    public String getProfileImageUrl() {
        return profileImageUrl;
    }


    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }
}
