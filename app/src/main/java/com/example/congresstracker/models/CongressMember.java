package com.example.congresstracker.models;

import java.io.Serializable;

public class CongressMember implements Serializable {
    private String id;
    private String name;
    private String party;
    private String state;
    private String chamber;

    public CongressMember(String id, String name, String party, String state) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.state = state;
    }
    public CongressMember(String id, String name, String party, String state, String chamber) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.state = state;
        this.chamber = chamber;
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
}
