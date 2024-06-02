package com.jamburger.kitter.components

import com.google.firebase.firestore.DocumentReference

class Post {
    lateinit var creator: String
    lateinit var postid: String
    lateinit var imageUrl: String
    lateinit var caption: String
    lateinit var kitt: String
    lateinit var likes: MutableList<DocumentReference>

    constructor()
    constructor(creator: String, postid: String, imageUrl: String, caption: String) {
        this.creator = creator
        this.postid = postid
        this.imageUrl = imageUrl
        this.caption = caption
        this.likes = ArrayList()
        this.kitt = ""
    }
}
