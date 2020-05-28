package com.jeffDev.congresstracker.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jeffDev.congresstracker.models.Bill;
import com.jeffDev.congresstracker.models.BillVote;
import com.jeffDev.congresstracker.models.CongressMember;
import com.jeffDev.congresstracker.models.States;
import com.jeffDev.congresstracker.other.NetworkUtils;
import com.jeffDev.congresstracker.models.Term;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MemberDataPull extends IntentService {

    private static final String SENATE_API_URL = "https://api.propublica.org/congress/v1/116/senate/members.json";
    private static final String HOUSE_API_URL = "https://api.propublica.org/congress/v1/116/house/members.json";
    public static final String ACTION_RECEIVE_MSG = "com.example.congresstracker.models.action.RECEIVE_MSG";
    public static final String ACTION_SEND_MEM_DETAIL = "com.example.congresstracker.models.action.SEND_MEM_DETAIL";
    public static final String ACTION_SEND_MEM_VOTES = "com.example.congresstracker.models.action.SEND_MEM_VOTES";
    public static final String ACTION_SEND_STATE_REPS = "com.example.congresstracker.models.action.SEND_STATE_REPS";


    public static final String ACTION_PULL_ALL = "com.example.congresstracker.models.action.PULL_ALL";
    public static final String ACTION_PULL_SELECTED = "com.example.congresstracker.models.action.PULL_SELECTED";
    public static final String ACTION_PULL_VOTES = "com.example.congresstracker.models.action.PULL_VOTES";
    public static final String ACTION_PULL_STATE = "com.example.congresstracker.models.action.PULL_STATE";

    public static final String EXTRA_MEMBERS = "EXTRA_MEMBERS";
    public static final String EXTRA_SENATE = "EXTRA_SENATE";
    public static final String EXTRA_HOUSE = "EXTRA_HOUSE";
    public static final String EXTRA_PAST_MEMBERS = "EXTRA_PAST_MEMBERS";
    public static final String EXTRA_ALL_MEMBERS = "EXTRA_ALL_MEMBERS";
    public static final String EXTRA_SELECTED_MEMBER = "EXTRA_SELECTED_MEMBER";
    public static final String EXTRA_MEMBER_VOTES = "EXTRA_MEMBER_VOTES";
    public static final String EXTRA_MEMBER_IMAGE = "EXTRA_MEMBER_IMAGE";
    public static final String EXTRA_USER_STATE = "EXTRA_USER_STATE";
    public static final String EXTRA_STATE_REPS = "EXTRA_STATE_REPS";

    private final String TAG = "MemberDataPull";

    ArrayList<CongressMember> currentMembers;
    ArrayList<CongressMember> senate;
    ArrayList<CongressMember> house;
    ArrayList<CongressMember> pastMembers;
    ArrayList<CongressMember> allMembers;
    ArrayList<CongressMember> myState;
    ArrayList<BillVote> memberVotes;
    CongressMember selectedMember;

    Bitmap memberImage;

    private static final int BROADCAST_ALL_MEM = 0;
    private static final int BROADCAST_SELECTED_MEM = 1;
    private static final int BROADCAST_MEM_VOTES = 2;
    private static final int BROADCAST_STATE_REPS = 3;

    private int broadCastCode;



    public MemberDataPull(){
        super("MemberDataPull");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null){
            String passedId = "";
            switch (intent.getAction()){

                // this is the action used to get all congress members
                case ACTION_PULL_ALL:
                    Log.i(TAG, "onHandleIntent: action Pull All");
                    requestMemberData();
                    broadCastCode = BROADCAST_ALL_MEM;
                    broadCastResults(broadCastCode);
                    break;

                // this is the action used to get a specific member
                case ACTION_PULL_SELECTED:
                    passedId = intent.getStringExtra(EXTRA_SELECTED_MEMBER);
                    Log.i(TAG, "selectedMember: action Pull Member: " + passedId);
                    requestSingleMemberData(passedId);
                    requestMemberImage(passedId);

                    if(selectedMember != null){
                        Log.i(TAG, "selectedMember: name: " + selectedMember.getName());
                        Log.i(TAG, "selectedMember: party: " + selectedMember.getParty());
                        Log.i(TAG, "selectedMember: chamber: " + selectedMember.getChamber());
                        Log.i(TAG, "selectedMember: gender: " + selectedMember.getGender());
                        Log.i(TAG, "selectedMember: next Election: " + selectedMember.getNextElection());
                        Log.i(TAG, "selectedMember: Seniority: " + selectedMember.getSeniority());
                        if(selectedMember.getTerms() != null){
                            Log.i(TAG, "selectedMember: terms: " + selectedMember.getTerms().size());
                            if(selectedMember.getTerms().get(0).getCommittees() != null){
                                Log.i(TAG, "selectedMember: Committees last term: " + selectedMember.getTerms().get(0).getCommittees().size());
                            }

                        }
                        broadCastCode = BROADCAST_SELECTED_MEM;
                        broadCastResults(broadCastCode);

                    }else{
                        Log.i(TAG, "selectedMember: ERROR Member null ");
                    }
                    break;

                // this is the action used to get a member's vote positions
                case ACTION_PULL_VOTES:
                    passedId = intent.getStringExtra(EXTRA_SELECTED_MEMBER);
                    Log.i(TAG, "selectedMember: action Pull Member: " + passedId);
                    requestMemberVoteData(passedId);

                    if(memberVotes != null){

                        for (BillVote vote: memberVotes) {
                            Log.i(TAG, "Vote History: BillID: " + vote.getId());
                            Log.i(TAG, "Vote History: Bill Title: " + vote.getTitle());
                            Log.i(TAG, "Vote History: Bill Title D: " + vote.getDescription());
                            Log.i(TAG, "Vote History: Bill Result: " + vote.getResult());
                            Log.i(TAG, "Vote History: Member Position: " + vote.getPosition());


                        }
                        broadCastCode = BROADCAST_MEM_VOTES;
                        broadCastResults(broadCastCode);
                    }
                    break;

                case ACTION_PULL_STATE:
                    String stateAbr = intent.getStringExtra(EXTRA_USER_STATE);
                    requestStateMembers(stateAbr);

                    if(myState != null){
                        for(CongressMember m : myState){
                            Log.i(TAG, "stateMember: name: " + m.getName());
                            Log.i(TAG, "stateMember: party: " + m.getParty());
                            Log.i(TAG, "stateMember: chamber: " + m.getChamber());
                        }
                        broadCastCode = BROADCAST_STATE_REPS;
                        broadCastResults(broadCastCode);
                    }
                    break;

            }

        }



    }
    public void requestSingleMemberData(String id){

        String url = "https://api.propublica.org/congress/v1/members/" + id+ ".json";
        String data = NetworkUtils.getNetworkData(url);

        if(NetworkUtils.isConnected(getBaseContext())){

            try{
                JSONObject response = new JSONObject(data);
                JSONArray resultsJson = response.getJSONArray("results");

                for (int i = 0; i < resultsJson.length(); i++) {

                    JSONObject obj = resultsJson.getJSONObject(0);
                    String firstName = obj.getString("first_name");
                    String lastName = obj.getString("last_name");
                    String party = obj.getString("current_party");
                    String gender = obj.getString("gender");
                    String personalURL = obj.getString("url");

                    if(party.equals("R")){
                        party = "Republican";
                    }else if(party.equals("D")){
                        party = "Democrat";
                    }else{
                        party = "Independent";
                    }
                    JSONArray nestedArray = obj.getJSONArray("roles");
                    ArrayList<Term> terms = new ArrayList<>();
                    String nextElection = "";
                    String _chamber = "";
                    String _state = "";

                    String name = firstName + " "+ lastName;
                    selectedMember = new CongressMember(id,name,gender, personalURL);
                    selectedMember.setParty(party);
                    for (int x = 0; x < nestedArray.length(); x++) {

                        JSONObject nestedObj = nestedArray.getJSONObject(x);
                        String termId = nestedObj.getString("congress");
                        String chamber = nestedObj.getString("chamber");
                        String state = nestedObj.getString("state");
                        String seniority = nestedObj.getString("seniority");
                        String startDate = nestedObj.getString("start_date");
                        String endDate = nestedObj.getString("end_date");
                        String termEnd = nestedObj.getString("next_election");

                        int totalVotes = 0;
                        if(!nestedObj.isNull("total_votes")){
                            totalVotes = nestedObj.getInt("total_votes");
                        }

                        int billsSponsored = 0;
                        if(!nestedObj.isNull("bills_sponsored")){
                            billsSponsored = nestedObj.getInt("bills_sponsored");
                        }

                        int billsCosponsored = 0;
                        if(!nestedObj.isNull("bills_cosponsored")){
                            billsCosponsored = nestedObj.getInt("bills_cosponsored");
                        }


                        double mvp = 0;
                        if(!nestedObj.isNull("missed_votes_pct")){
                            mvp = nestedObj.getDouble("missed_votes_pct");
                        }

                        double vwpp = 0;
                        if(!nestedObj.isNull("votes_with_party_pct")){
                            vwpp = nestedObj.getDouble("votes_with_party_pct");
                        }

                        double vapp = 0;
                        if(!nestedObj.isNull("votes_against_party_pct")){
                            vapp = nestedObj.getDouble("votes_against_party_pct");
                        }

                        if(x == 0){
                            nextElection = termEnd;
                            _chamber = chamber;
                            _state = state;
                        }



                        ArrayList<String> committees = new ArrayList<>();
                        ArrayList<String> comCodes = new ArrayList<>();
                        JSONArray committeeJArray = nestedObj.getJSONArray("committees");

                        for (int y = 0; y < committeeJArray.length(); y++) {
                            JSONObject comNestedObj = committeeJArray.getJSONObject(y);
                            committees.add(comNestedObj.getString("name"));
                            comCodes.add(comNestedObj.getString("code"));
                        }

                        Term thisTerm = new Term(chamber,state,startDate,
                                endDate,seniority,totalVotes,billsSponsored
                                ,billsCosponsored,mvp,vwpp,vapp,termId);
                        thisTerm.setCommittees(committees);
                        thisTerm.setComCodes(comCodes);
                        terms.add(thisTerm);


                    }

                    selectedMember.setNextElection(nextElection);
                    selectedMember.setChamber(_chamber);
                    selectedMember.setState(_state);
                    selectedMember.setTerms(terms);

                }



            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(selectedMember.getTerms().size() > 0){
                if(selectedMember.getNumSponsoredFromTerms() > 0){

                     ArrayList<Bill> billsToReturn = new ArrayList<>();
                     url = "https://api.propublica.org/congress/v1/members/"+ selectedMember.getId() +"/bills/introduced.json";
                     data = NetworkUtils.getNetworkData(url);
                    try {
                            JSONObject response = new JSONObject(data);
                            JSONArray resultsJson = response.getJSONArray("results");


                            for (int i = 0; i < resultsJson.length(); i++) {
                                JSONObject obj = resultsJson.getJSONObject(i);
                                JSONArray billsJson = obj.getJSONArray("bills");

                                for (int x = 0; x < billsJson.length(); x++) {
                                    JSONObject nestedObj = billsJson.getJSONObject(x);

                                    String chamber = "";
                                    if(!obj.isNull("chamber")){
                                        chamber = obj.getString("chamber");
                                    }

                                    String billNum = nestedObj.getString("number");
                                    String billUri = nestedObj.getString("bill_uri");
                                    String title = nestedObj.getString("title");
                                    String shortTitle  = nestedObj.getString("short_title");
                                    String sponsor = nestedObj.getString("sponsor_name");
                                    String dateIntroduced = nestedObj.getString("introduced_date");
                                    boolean active = nestedObj.getBoolean("active");
                                    int cosponsors = nestedObj.getInt("cosponsors");
                                    String billUrl = nestedObj.getString("congressdotgov_url");
                                    String summary = nestedObj.getString("summary");


                                    billsToReturn.add(new Bill(chamber,billNum,billUri,title,
                                            shortTitle,sponsor,dateIntroduced,active,cosponsors,billUrl,summary));

                                }

                            }

                            selectedMember.setSponsoredBills(billsToReturn);

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }



        }

    }

    public void requestMemberVoteData(String id){
        String url = "https://api.propublica.org/congress/v1/members/" + id+ "/votes.json";
        String data = NetworkUtils.getNetworkData(url);

        if(NetworkUtils.isConnected(getBaseContext())) {
            memberVotes = new ArrayList<>();

            try {
                JSONObject response = new JSONObject(data);
                JSONArray resultsJson = response.getJSONArray("results");


                for (int i = 0; i < resultsJson.length(); i++) {

                    JSONObject obj = resultsJson.getJSONObject(i);
                    JSONArray votesJson = obj.getJSONArray("votes");

                    for (int x = 0; x < votesJson.length(); x++) {
                        JSONObject nestedObj = votesJson.getJSONObject(x);
                        JSONObject billObj = nestedObj.getJSONObject("bill");
                        String billId = billObj.getString("number");
                        String billUri = "";
                        if(!billObj.isNull("bill_uri")){
                            billUri = billObj.getString("bill_uri");
                        }

                        String billTitle = billObj.getString("title");
                        String billLastAction = billObj.getString("latest_action");

                        String description = nestedObj.getString("description");
                        String result = nestedObj.getString("result");
                        String date = nestedObj.getString("date");
                        String position = nestedObj.getString("position");

                        memberVotes.add(new BillVote(billId,billTitle,billUri,billLastAction,description,result,position,date));
                    }
                    Log.i(TAG, "MemberVoteData: Num Results: " + i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void requestMemberData(){
        Log.i(TAG, "onHandleIntent: request started");
        String data = NetworkUtils.getNetworkData(SENATE_API_URL);

        if(NetworkUtils.isConnected(getBaseContext())) {
            currentMembers = new ArrayList<>();
            senate = new ArrayList<>();
            house = new ArrayList<>();
            pastMembers = new ArrayList<>();
            allMembers = new ArrayList<>();
            try {

                JSONObject response = new JSONObject(data);

                JSONArray resultsJson = response.getJSONArray("results");



                for (int i = 0; i < resultsJson.length(); i++) {
                    JSONObject obj = resultsJson.getJSONObject(i);
                    JSONArray membersJson = obj.getJSONArray("members");

                    for (int x = 0; x < membersJson.length(); x++) {
                        JSONObject nestedObj = membersJson.getJSONObject(x);
                        boolean inOffice = nestedObj.getBoolean("in_office");
                        String id = nestedObj.getString("id");
                        String firstName = nestedObj.getString("first_name");
                        String lastName = nestedObj.getString("last_name");
                        String party = nestedObj.getString("party");
                        String state = nestedObj.getString("state");

                        String seniority = "0";
                        if(!nestedObj.isNull("seniority")){
                            seniority = nestedObj.getString("seniority");
                        }
                        String nextElection = nestedObj.getString("next_election");
                        String name = firstName + " " + lastName;

                        if(party.equals("R")){
                            party = "Republican";
                        }else if(party.equals("D")){
                            party = "Democrat";
                        }else{
                            party = "Independent";
                        }
                        String chamber = "senate";

                        if(inOffice){
                            currentMembers.add(new CongressMember(id, name, party, state,chamber,seniority, nextElection));
                            senate.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));

                        }else{
                            pastMembers.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));
                        }

                        allMembers.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));

                    }

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Log.i(TAG, "requestMemberData: Not Connected");
        }

        data = NetworkUtils.getNetworkData(HOUSE_API_URL);
        try {

            JSONObject response = new JSONObject(data);

            JSONArray resultsJson = response.getJSONArray("results");


            for(int i = 0; i < resultsJson.length(); i++) {
                JSONObject obj = resultsJson.getJSONObject(i);
                JSONArray membersJson = obj.getJSONArray("members");

                for(int x = 0; x < membersJson.length(); x++){
                    JSONObject nestedObj = membersJson.getJSONObject(x);
                    boolean inOffice = nestedObj.getBoolean("in_office");
                    String id = nestedObj.getString("id");
                    String firstName = nestedObj.getString("first_name");
                    String lastName = nestedObj.getString("last_name");
                    String party = nestedObj.getString("party");
                    String state = nestedObj.getString("state");

                    String seniority = "0";
                    if(!nestedObj.isNull("seniority")){
                        seniority = nestedObj.getString("seniority");
                    }

                    String nextElection = nestedObj.getString("next_election");
                    String name = firstName + " "+lastName;
                    if(party.equals("R")){
                        party = "Republican";
                    }else if(party.equals("D")){
                        party = "Democrat";
                    }else{
                        party = "Independent";
                    }

                    String chamber = "house";

                    if(inOffice){
                        currentMembers.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));
                        house.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));
                    }else{
                        pastMembers.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));
                    }
                    allMembers.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));

                }

            }


        } catch(JSONException e) {
            e.printStackTrace();
        }



    }
    public void requestStateMembers(String state){

        String url = "https://api.propublica.org/congress/v1/members/senate/"+state+"/current.json";
        String data = NetworkUtils.getNetworkData(url);

        if(NetworkUtils.isConnected(getBaseContext())){
            myState = new ArrayList<>();

            try {

                JSONObject response = new JSONObject(data);
                JSONArray resultsJson = response.getJSONArray("results");

                for (int i = 0; i < resultsJson.length(); i++) {
                    JSONObject obj = resultsJson.getJSONObject(i);
                    String id = obj.getString("id");
                    String firstName = obj.getString("first_name");
                    String lastName = obj.getString("last_name");
                    String party = obj.getString("party");
                    String seniority = "0";
                    if(!obj.isNull("seniority")){
                        seniority = obj.getString("seniority");
                    }
                    String nextElection = obj.getString("next_election");

                    String chamber = "Senate";
                    String name = firstName + " " + lastName;

                    myState.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            int numDistricts = States.getDistricts(state);

            for (int x = 1; x <= numDistricts; x++) {


                url = "https://api.propublica.org/congress/v1/members/house/" + state + "/"+ x +"/current.json";
                data = NetworkUtils.getNetworkData(url);

                try {

                    JSONObject response = new JSONObject(data);
                    JSONArray resultsJson = response.getJSONArray("results");

                    for (int i = 0; i < resultsJson.length(); i++) {
                        JSONObject obj = resultsJson.getJSONObject(i);
                        String id = obj.getString("id");
                        String firstName = obj.getString("first_name");
                        String lastName = obj.getString("last_name");
                        String party = obj.getString("party");
                        String seniority = "0";
                        if (!obj.isNull("seniority")) {
                            seniority = obj.getString("seniority");
                        }
                        String nextElection = obj.getString("next_election");

                        String chamber = "House";
                        String name = firstName + " " + lastName;

                        myState.add(new CongressMember(id, name, party, state, chamber, seniority, nextElection));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public void requestMemberImage(String id){

        String url = "https://theunitedstates.io/images/congress/225x275/" + id + ".jpg";

        try{
            memberImage = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void broadCastResults(int code){
        Intent broadcastIntent;
        switch (code){
            case BROADCAST_ALL_MEM:
                broadcastIntent = new Intent(ACTION_RECEIVE_MSG);
                broadcastIntent.putExtra(EXTRA_MEMBERS, currentMembers);
                broadcastIntent.putExtra(EXTRA_SENATE,senate);
                broadcastIntent.putExtra(EXTRA_HOUSE,house);
                broadcastIntent.putExtra(EXTRA_PAST_MEMBERS, pastMembers);
                broadcastIntent.putExtra(EXTRA_ALL_MEMBERS, allMembers);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                break;
            case BROADCAST_SELECTED_MEM:
                broadcastIntent = new Intent(ACTION_SEND_MEM_DETAIL);
                broadcastIntent.putExtra(EXTRA_SELECTED_MEMBER, selectedMember);
                broadcastIntent.putExtra(EXTRA_MEMBER_IMAGE, memberImage);

                sendBroadcast(broadcastIntent);
                //LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                break;
            case BROADCAST_MEM_VOTES:
                broadcastIntent = new Intent(ACTION_SEND_MEM_VOTES);
                broadcastIntent.putExtra(EXTRA_MEMBER_VOTES, memberVotes);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                break;

            case BROADCAST_STATE_REPS:
                broadcastIntent = new Intent(ACTION_SEND_STATE_REPS);
                broadcastIntent.putExtra(EXTRA_STATE_REPS, myState);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                break;
        }

    }

}
