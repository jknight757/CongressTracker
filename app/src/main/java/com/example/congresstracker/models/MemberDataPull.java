package com.example.congresstracker.models;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MemberDataPull extends IntentService {

    private static final String SENATE_API_URL = "https://api.propublica.org/congress/v1/116/senate/members.json";
    private static final String HOUSE_API_URL = "https://api.propublica.org/congress/v1/116/house/members.json";
    public static final String ACTION_RECEIVE_MSG = "com.example.congresstracker.models.action.RECEIVE_MSG";
    public static final String ACTION_PULL_ALL = "com.example.congresstracker.models.action.PULL_ALL";
    public static final String EXTRA_MEMBERS = "EXTRA_MEMBERS";
    public static final String EXTRA_SENATE = "EXTRA_SENATE";
    public static final String EXTRA_HOUSE = "EXTRA_HOUSE";
    public static final String EXTRA_PAST_MEMBERS = "EXTRA_PAST_MEMBERS";
    public static final String EXTRA_ALL_MEMBERS = "EXTRA_ALL_MEMBERS";
    private final String TAG = "MemberDataPull";

    ArrayList<CongressMember> currentMembers;
    ArrayList<CongressMember> senate;
    ArrayList<CongressMember> house;
    ArrayList<CongressMember> senateRepublican;
    ArrayList<CongressMember> houseRepublican;
    ArrayList<CongressMember> senateDemocrat;
    ArrayList<CongressMember> houseDemocrat;

    ArrayList<CongressMember> pastMembers;
    ArrayList<CongressMember> pastSenate;
    ArrayList<CongressMember> pastHouse;
    ArrayList<CongressMember> pastSenateRepublican;
    ArrayList<CongressMember> pastHouseRepublican;
    ArrayList<CongressMember> pastSenateDemocrat;
    ArrayList<CongressMember> pastHouseDemocrat;

    ArrayList<CongressMember> allMembers;


    public MemberDataPull(){
        super("MemberDataPull");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null){
            if(intent.getAction().equals(ACTION_PULL_ALL)){
                requestMemberData();
                broadCastResults();
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
                        Boolean inOffice = nestedObj.getBoolean("in_office");
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

    public void broadCastResults(){
        Intent broadcastIntent = new Intent(ACTION_RECEIVE_MSG);
        broadcastIntent.putExtra(EXTRA_MEMBERS, currentMembers);
        broadcastIntent.putExtra(EXTRA_SENATE,senate);
        broadcastIntent.putExtra(EXTRA_HOUSE,house);
        broadcastIntent.putExtra(EXTRA_PAST_MEMBERS, pastMembers);
        broadcastIntent.putExtra(EXTRA_ALL_MEMBERS, allMembers);
        sendBroadcast(broadcastIntent);
    }

    private int getIdFromLink(String _link) {
        int index = _link.lastIndexOf('/');
        if(index > -1 && (index+1) < _link.length()) {
            try {
                return Integer.parseInt(_link.substring(index+1));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("ERROR", "Unable to find ID in string \"" + _link + "\".");
        return 0;
    }
}
