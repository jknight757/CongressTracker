package com.example.congresstracker.models;

import java.util.ArrayList;

public class Term {
    String chamber;
    String state;
    String startDate;
    String endDate;
    String seniority;
    int totalVotes;
    int billsSponsored;
    int billsCosponsored;
    double missVotePct;
    double voteWPartyPct;
    double voteAPartyPct;
    ArrayList<String> committees;
    ArrayList<String> comCodes;

    public Term(String chamber, String state, String startDate, String endDate, String seniority, int totalVotes, int billsSponsored, int billsCosponsored, double missVotePct, double voteWPartyPct, double voteAPartyPct) {
        this.chamber = chamber;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
        this.seniority = seniority;
        this.totalVotes = totalVotes;
        this.billsSponsored = billsSponsored;
        this.billsCosponsored = billsCosponsored;
        this.missVotePct = missVotePct;
        this.voteWPartyPct = voteWPartyPct;
        this.voteAPartyPct = voteAPartyPct;
    }

    public String getChamber() {
        return chamber;
    }

    public String getState() {
        return state;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getSeniority() {
        return seniority;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public int getBillsSponsored() {
        return billsSponsored;
    }

    public int getBillsCosponsored() {
        return billsCosponsored;
    }

    public double getMissVotePct() {
        return missVotePct;
    }

    public double getVoteWPartyPct() {
        return voteWPartyPct;
    }

    public double getVoteAPartyPct() {
        return voteAPartyPct;
    }

    public ArrayList<String> getCommittees() {
        return committees;
    }

    public ArrayList<String> getComCodes() {
        return comCodes;
    }

    public void setCommittees(ArrayList<String> committees) {
        this.committees = committees;
    }

    public void setComCodes(ArrayList<String> comCodes) {
        this.comCodes = comCodes;
    }
}
