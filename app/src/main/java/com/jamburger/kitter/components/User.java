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
    String onesignalPlayerId;
    List<DocumentReference> pictures;
    List<DocumentReference> kitts;
    List<DocumentReference> saved;
    List<DocumentReference> following;
    List<DocumentReference> followers;


    public User() {

    }


    public User(String id, String name, String username, String email, String profileImageUrl, String backgroundImageUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.onesignalPlayerId = "";
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.bio = "";
        this.pictures = new ArrayList<>();
        this.kitts = new ArrayList<>();
        this.saved = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
    }

    public String getOnesignalPlayerId() {
        return onesignalPlayerId;
    }

    public void setOnesignalPlayerId(String onesignalPlayerId) {
        this.onesignalPlayerId = onesignalPlayerId;
    }

    public List<DocumentReference> getFollowing() {
        return following;
    }

    public List<DocumentReference> getFollowers() {
        return followers;
    }

    public List<DocumentReference> getSaved() {
        return saved;
    }

    public List<DocumentReference> getPictures() {
        return pictures;
    }

    public List<DocumentReference> getKitts() {
        return kitts;
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

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }


    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }
}
