package com.example.congresstracker.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String email;
    public String password;
    public String party;
    public String zip;
    public boolean hasProfImg;
    public String state;


    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getState() {
        return state;
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

    public void setState(String state) {
        this.state = state;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
