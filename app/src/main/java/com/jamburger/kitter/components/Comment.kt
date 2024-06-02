package com.jamburger.kitter.components

class Comment {
    lateinit var publisherId: String
    lateinit var text: String
    lateinit var commentId: String

    constructor()
    constructor(publisherId: String, text: String, commentId: String) {
        this.publisherId = publisherId
        this.text = text
        this.commentId = commentId
    }
}
