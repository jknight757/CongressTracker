package com.example.congresstracker.models;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MemberDataPull extends IntentService {

    private static final String SENATE_API_URL = "https://api.propublica.org/congress/v1/116/senate/members.json";
    private static final String HOUSE_API_URL = "https://api.propublica.org/congress/v1/116/house/members.json";
    public static final String ACTION_RECEIVE_MSG = "com.example.congresstracker.models.action.RECEIVE_MSG";
    public static final String ACTION_SEND_MEM_DETAIL = "com.example.congresstracker.models.action.SEND_MEM_DETAIL";
    public static final String ACTION_PULL_ALL = "com.example.congresstracker.models.action.PULL_ALL";
    public static final String ACTION_PULL_SELECTED = "com.example.congresstracker.models.action.PULL_SELECTED";
    public static final String EXTRA_MEMBERS = "EXTRA_MEMBERS";
    public static final String EXTRA_SENATE = "EXTRA_SENATE";
    public static final String EXTRA_HOUSE = "EXTRA_HOUSE";
    public static final String EXTRA_PAST_MEMBERS = "EXTRA_PAST_MEMBERS";
    public static final String EXTRA_ALL_MEMBERS = "EXTRA_ALL_MEMBERS";
    public static final String EXTRA_SELECTED_MEMBER = "EXTRA_SELECTED_MEMBER";
    private final String TAG = "MemberDataPull";

    ArrayList<CongressMember> currentMembers;
    ArrayList<CongressMember> senate;
    ArrayList<CongressMember> house;
    ArrayList<CongressMember> pastMembers;
    ArrayList<CongressMember> allMembers;
    CongressMember selectedMember;

    private static final int BROADCAST_ALL_MEM = 0;
    private static final int BROADCAST_SELECTED_MEM = 1;

    private int broadCastCode;



    public MemberDataPull(){
        super("MemberDataPull");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null){
            if(intent.getAction().equals(ACTION_PULL_ALL)){
                Log.i(TAG, "onHandleIntent: action Pull All");
                requestMemberData();
                broadCastCode = BROADCAST_ALL_MEM;
                broadCastResults(broadCastCode);
            }
            else if(intent.getAction().equals(ACTION_PULL_SELECTED)){
                String passedId = intent.getStringExtra(EXTRA_SELECTED_MEMBER);
                Log.i(TAG, "selectedMember: action Pull Member: " + passedId);
                requestSingleMemberData(passedId);

                if(selectedMember != null){
                    Log.i(TAG, "selectedMember: name: " + selectedMember.getName());
                    Log.i(TAG, "selectedMember: party: " + selectedMember.getParty());
                    Log.i(TAG, "selectedMember: chamber: " + selectedMember.getChamber());
                    Log.i(TAG, "selectedMember: gender: " + selectedMember.getGender());
                    Log.i(TAG, "selectedMember: next Election: " + selectedMember.getNextElection());
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
                        String chamber = nestedObj.getString("chamber");
                        String state = nestedObj.getString("state");
                        String seniority = nestedObj.getString("seniority");
                        String startDate = nestedObj.getString("start_date");
                        String endDate = nestedObj.getString("end_date");
                        String termEnd = nestedObj.getString("next_election");
                        int totalVotes = nestedObj.getInt("total_votes");

                        int billsSponsored = 0;
                        int billsCosponsored = 0;
                        if(!nestedObj.isNull("bills_sponsored")){
                            billsSponsored = nestedObj.getInt("bills_sponsored");
                        }

                        if(!nestedObj.isNull("bills_cosponsored")){
                            billsCosponsored = nestedObj.getInt("bills_cosponsored");
                        }


                        double mvp = nestedObj.getDouble("missed_votes_pct");
                        double vwpp = nestedObj.getDouble("votes_with_party_pct");
                        double vapp = nestedObj.getDouble("votes_against_party_pct");

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
                                ,billsCosponsored,mvp,vwpp,vapp);
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
                            currentMembers.add(new CongressMember(id, name, party, state,chamber));
                            senate.add(new CongressMember(id, name, party, state, chamber));

                        }else{
                            pastMembers.add(new CongressMember(id, name, party, state, chamber));
                        }

                        allMembers.add(new CongressMember(id, name, party, state, chamber));

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
                        currentMembers.add(new CongressMember(id, name, party, state, chamber));
                        house.add(new CongressMember(id, name, party, state, chamber));
                    }else{
                        pastMembers.add(new CongressMember(id, name, party, state, chamber));
                    }
                    allMembers.add(new CongressMember(id, name, party, state, chamber));

                }

            }


        } catch(JSONException e) {
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
                sendBroadcast(broadcastIntent);
                //LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                break;
        }

    }

}
