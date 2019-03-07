package com.eve.tp040685.eve.UserClasses;

import android.media.Image;

public class Manager extends User {

    public Manager() {

    }

    public Manager(String id, String name, String username,String role, String password) {
        super(id, name, username,role,password);
    }

    public Manager(String profile_image){
        super(profile_image);
    }

}
