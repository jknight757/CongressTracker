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
    private String seniority;
    private ArrayList<Term> terms;

    private String[] stateAbList = new String[] {"AK","AL","AR","AZ","CA","CO","CT","DC","DE","FL","GA","HI","IA","ID",
            "IL","IN","KS","KY","LA","MA","MD","ME","MI","MN","MO","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY",
            "OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VA","VT","WA","WI","WV","WY"};

    private String[] stateList = new String[] {"Alaska","Alabama","Arkansas","Arizona","California","Colorado","Connecticut",
            "District of Columbia","Delaware","Florida","Georgia","Hawaii","Iowa","Idaho", "Illinois","Indiana","Kansas",
            "Kentucky","Louisiana","Massachusetts","Maryland","Maine","Michigan", "Minnesota","Missouri","Mississippi",
            "Montana","North Carolina","North Dakota","Nebraska","New Hampshire", "New Jersey","New Mexico","Nevada",
            "New York", "Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota",
            "Tennessee","Texas","Utah", "Virginia","Vermont","Washington","Wisconsin","West Virginia","Wyoming"};


    public CongressMember(String id, String name, String party, String state, String chamber, String seniority, String nextElection) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.state = state;
        this.chamber = chamber;
        this.seniority = seniority;
        this.nextElection = nextElection;
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

    public String[] getStateAbList() {
        return stateAbList;
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

    public String getSeniority() {
        return seniority;
    }

    public void setSeniority(String seniority) {
        this.seniority = seniority;
    }

    public int getTotalVotes(){
        if(terms != null){
            int total = 0;
            for (int i = 0; i < terms.size(); i++) {
                total += terms.get(i).totalVotes;
            }
            return total;
        }else{
            return 0;
        }

    }
    public int getMissedVotePctAverage(){
        if(terms != null){
            double total = 0;
            for (int i = 0; i < terms.size(); i++) {
                total += terms.get(i).getMissVotePct();
            }
            total = total/terms.size();
            int roundedT = (int) Math.rint(total);
            return roundedT;
        }else {
            return 0;
        }
    }

    public int getVoteWPPctAverage(){
        if(terms != null){
            double total = 0;
            for (int i = 0; i < terms.size(); i++) {
                total += terms.get(i).getVoteWPartyPct();
            }
            total = total/terms.size();
            int roundedT = (int) Math.rint(total);
            return roundedT;
        }else {
            return 0;
        }
    }

    public int getVoteAPPctAverage(){
        if(terms != null){
            double total = 0;
            for (int i = 0; i < terms.size(); i++) {
                total += terms.get(i).getVoteAPartyPct();
            }
            total = total/terms.size();
            int roundedT = (int) Math.rint(total);
            return roundedT;
        }else {
            return 0;
        }
    }

    public String getUnabreviated(){
        String state = "";
        for (int i = 0; i < stateAbList.length; i++) {
            if(stateAbList[i].equals(getState())){
                state = stateList[i];
            }

        }
       return state;
    }


}
