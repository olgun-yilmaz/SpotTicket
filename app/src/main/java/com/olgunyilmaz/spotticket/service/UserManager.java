package com.olgunyilmaz.spotticket.service;


public class UserManager {
    private static UserManager instance;
    public String ppUrl;

    private UserManager(){
        ppUrl = "";
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

}
