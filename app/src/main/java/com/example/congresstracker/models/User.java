package com.example.congresstracker.models;

import java.io.Serializable;

public class User implements Serializable {
    String name;
    String email;
    String password;
    String party;
    String zip;
    boolean hasProfImg;


    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getParty() {
        return party;
    }

    public String getZip() {
        return zip;
    }

    public boolean getHasProfImg() {
        return hasProfImg;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setHasProfImg(boolean hasProfImg) {
        this.hasProfImg = hasProfImg;
    }

}
