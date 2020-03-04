package com.example.congresstracker.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bill implements Serializable, Comparable<Bill> {
    String chamber;
    String billNum;
    String billUri;
    String title;
    String shortTitle;
    String sponsor;
    String dateIntroduced;
    boolean active;
    int cosponsors;
    String url;
    String summary;

    public Bill(String chamber, String billNum, String billUri, String title, String shortTitle, String sponsor, String dateIntroduced, boolean active, int cosponsors, String url, String summary) {
        this.chamber = chamber;
        this.billNum = billNum;
        this.billUri = billUri;
        this.title = title;
        this.shortTitle = shortTitle;
        this.sponsor = sponsor;
        this.dateIntroduced = dateIntroduced;
        this.active = active;
        this.cosponsors = cosponsors;
        this.url = url;
        this.summary = summary;
    }

    public String getChamber() {
        return chamber;
    }

    public String getBillNum() {
        return billNum;
    }

    public String getBillUri() {
        return billUri;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public String getSponsor() {
        return sponsor;
    }

    public String getDateIntroduced() {
        return dateIntroduced;
    }

    public boolean isActive() {
        return active;
    }

    public int getCosponsors() {
        return cosponsors;
    }

    public String getUrl() {
        return url;
    }

    public String getSummary() {
        return summary;
    }
    public Date getDateAsO(){

        SimpleDateFormat format = new SimpleDateFormat("yyyy-dd-mm");
        try{
            Date date = format.parse(dateIntroduced);
            return date;
        }catch (ParseException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int compareTo(Bill o) {
        return o.getDateAsO().compareTo(getDateAsO());
    }
}
