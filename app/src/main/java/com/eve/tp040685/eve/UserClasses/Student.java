package com.eve.tp040685.eve.UserClasses;

import android.media.Image;

public class Student extends User{

    public Student() {

    }
    public Student (String id, String username, String password){
        super(id, username, password);
    }

    public Student(String id, String name, String username, String role, String password) {
        super(id,name, username,role,password);
    }
    public  Student(String profile_image){
        super(profile_image);
    }
}
