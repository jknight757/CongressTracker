package com.example.congresstracker.models;

import java.io.Serializable;

public class CongressMember implements Serializable {
    private String id;
    private String name;
    private String party;
    private String state;

    public CongressMember(String id, String name, String party, String state) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.state = state;
    }
    public CongressMember(String id, String name, String party) {
        this.id = id;
        this.name = name;
        this.party = party;
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
}
