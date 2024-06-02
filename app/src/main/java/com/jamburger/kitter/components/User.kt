package com.jamburger.kitter.components

import com.google.firebase.firestore.DocumentReference

class User {
    lateinit var id: String
    lateinit var name: String
    lateinit var username: String
    lateinit var email: String
    lateinit var bio: String
    lateinit var profileImageUrl: String
    lateinit var backgroundImageUrl: String
    var isPrivate = false
    lateinit var posts: List<DocumentReference>
    lateinit var saved: List<DocumentReference>
    lateinit var following: List<DocumentReference>
    lateinit var followers: List<DocumentReference>
    lateinit var blockedAccounts: List<DocumentReference>

    constructor()
    constructor(
        id: String,
        name: String,
        username: String,
        email: String,
        profileImageUrl: String,
        backgroundImageUrl: String
    ) {
        this.id = id
        this.name = name
        this.username = username
        this.email = email
        this.profileImageUrl = profileImageUrl
        this.backgroundImageUrl = backgroundImageUrl
        this.bio = ""
        this.posts = ArrayList()
        this.saved = ArrayList()
        this.following = ArrayList()
        this.followers = ArrayList()
        this.blockedAccounts = ArrayList()
        this.isPrivate = false
    }
}
