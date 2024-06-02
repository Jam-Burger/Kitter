package com.jamburger.kitter.components;

public class Message {
    String messageId, text, senderId;

    public Message() {
    }

    public Message(String messageId, String text, String senderId) {
        this.messageId = messageId;
        this.text = text;
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessageId() {
        return messageId;
    }
}
