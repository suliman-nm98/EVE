package com.eve.tp040685.eve.UserClasses;

public class Notification {

    private String from;
    private String message;

    public String getFrom() {
        return from;
    }
    public void setFrom(String user_name) {
        this.from = user_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String user_image) {
        this.message = user_image;
    }

    public Notification(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public Notification() {
    }



}
