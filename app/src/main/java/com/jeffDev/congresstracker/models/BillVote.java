package com.jeffDev.congresstracker.models;

import java.io.Serializable;

public class BillVote implements Serializable {
    String id;
    String title;
    String uri;
    String lastAction;
    String description;
    String result;
    String position;
    String date;

    public BillVote(String id, String title, String uri, String lastAction) {
        this.id = id;
        this.title = title;
        this.uri = uri;
        this.lastAction = lastAction;
    }

    public BillVote(String id, String title, String uri, String lastAction, String description, String result, String position) {
        this.id = id;
        this.title = title;
        this.uri = uri;
        this.lastAction = lastAction;
        this.description = description;
        this.result = result;
        this.position = position;
    }

    public BillVote(String id, String title, String uri, String lastAction, String description, String result, String position, String date) {
        this.id = id;
        this.title = title;
        this.uri = uri;
        this.lastAction = lastAction;
        this.description = description;
        this.result = result;
        this.position = position;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public String getLastAction() {
        return lastAction;
    }

    public String getDescription() {
        return description;
    }

    public String getResult() {
        return result;
    }

    public String getPosition() {
        return position;
    }

    public String getDate() {
        return date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
