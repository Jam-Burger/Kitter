package com.jamburger.kitter.components;

public class Comment {
    String publisherId;
    String text;

    String commentId;


    public Comment() {

    }

    public Comment(String publisherId, String text, String commentId) {
        this.publisherId = publisherId;
        this.text = text;
        this.commentId = commentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getCommentId() {
        return commentId;
    }
}
