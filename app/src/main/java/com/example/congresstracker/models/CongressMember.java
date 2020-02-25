package com.example.congresstracker.models;

import java.io.Serializable;
import java.util.ArrayList;

public class CongressMember implements Serializable {
    private String id;
    private String name;
    private String party;
    private String state;
    private String chamber;
    private String gender;
    private String url;
    private String nextElection;
    private ArrayList<Term> terms;

//    public CongressMember(String id, String name, String party, String state) {
//        this.id = id;
//        this.name = name;
//        this.party = party;
//        this.state = state;
//    }
    public CongressMember(String id, String name, String party, String state, String chamber) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.state = state;
        this.chamber = chamber;
    }
    public CongressMember(String id, String name,String gender, String url) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.url = url;
    }
    public CongressMember(String id, String name, String party) {
        this.id = id;
        this.name = name;
        this.party = party;
    }

    public String getChamber() {
        return chamber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParty() {
        return party;
    }

    public String getState() {
        return state;
    }

    public String getGender() {
        return gender;
    }

    public String getUrl() {
        return url;
    }

    public String getNextElection() {
        return nextElection;
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    public void setNextElection(String nextElection) {
        this.nextElection = nextElection;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setChamber(String chamber) {
        this.chamber = chamber;
    }

    public void setTerms(ArrayList<Term> terms) {
        this.terms = terms;
    }


}
