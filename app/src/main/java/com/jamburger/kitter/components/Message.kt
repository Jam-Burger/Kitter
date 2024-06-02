package com.jamburger.kitter.components

class Message {
    lateinit var messageId: String
    lateinit var text: String
    lateinit var senderId: String

    constructor()
    constructor(messageId: String, text: String, senderId: String) {
        this.messageId = messageId
        this.text = text
        this.senderId = senderId
    }
}
