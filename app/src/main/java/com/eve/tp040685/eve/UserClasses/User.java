package com.eve.tp040685.eve.UserClasses;

import android.media.Image;

public class User {
    private String id;
    private String name;
    private String username;
    private String password;
    private String profile_image;
    private String role;

    public User() {

    }
    public User(String id, String username, String password){
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(String profile_image){
        this.profile_image = profile_image;
    }

    public User(String id, String name, String username,String role, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
        this.password = password;
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

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

}
