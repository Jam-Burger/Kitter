package com.jamburger.kitter.components;

import com.google.firebase.firestore.DocumentReference;

public class Comment {
    DocumentReference publisher;
    String text;

    String commentId;


    public Comment() {

    }

    public Comment(DocumentReference publisher, String text, String commentId) {
        this.publisher = publisher;
        this.text = text;
        this.commentId = commentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DocumentReference getPublisher() {
        return publisher;
    }

    public void setPublisher(DocumentReference publisher) {
        this.publisher = publisher;
    }

    public String getCommentId() {
        return commentId;
    }
}
