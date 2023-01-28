package com.jamburger.kitter.components;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class User {
    String id;
    String name;
    String username;
    String email;
    String bio;
    String profileImageUrl;
    String backgroundImageUrl;
    String password;
    List<DocumentReference> posts;
    List<DocumentReference> saved;


    public User() {

    }

    public User(String id, String name, String username, String email, String password, String profileImageUrl, String backgroundImageUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.bio = "";
        this.posts = new ArrayList<>();
        this.saved = new ArrayList<>();
    }

    public String getPassword() {
        return password;
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
