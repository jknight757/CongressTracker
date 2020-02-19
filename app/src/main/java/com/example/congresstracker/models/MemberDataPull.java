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
    public static final String EXTRA_MEMBERS = "EXTRA_MEMBERS";
    public static final String EXTRA_SENATE = "EXTRA_SENATE";
    public static final String EXTRA_HOUSE = "EXTRA_HOUSE";
    private final String TAG = "MemberDataPull";

    ArrayList<CongressMember> members;
    ArrayList<CongressMember> pastMembers;
    ArrayList<CongressMember> senate;
    ArrayList<CongressMember> house;


    public MemberDataPull(){
        super("MemberDataPull");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null){
            Log.i(TAG, "onHandleIntent: Service Started");

            requestMemberData();
            if(members != null){
                for (CongressMember m: members) {
                    Log.i(TAG, "onHandleIntent: "+ m.getName());
                    Log.i(TAG, "onHandleIntent: "+ m.getParty());
                    Log.i(TAG, "onHandleIntent: "+ m.getState());
                    Log.i(TAG, "onHandleIntent:______________________ ");
                }

            }else{
                Log.i(TAG, "onHandleIntent: memberlist null");
            }
            broadCastResults();
        }

    }

    public void requestMemberData(){
        Log.i(TAG, "onHandleIntent: request started");
        String data = NetworkUtils.getNetworkData(SENATE_API_URL);

        if(NetworkUtils.isConnected(getBaseContext())) {
            members = new ArrayList<>();
            senate = new ArrayList<>();
            house = new ArrayList<>();
            pastMembers = new ArrayList<>();
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


                        if(inOffice){
                            members.add(new CongressMember(id, name, party, state));
                            senate.add(new CongressMember(id, name, party, state));
                        }else{
                            pastMembers.add(new CongressMember(id, name, party, state));
                        }


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
                    Boolean inOffice = nestedObj.getBoolean("in_office");
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

                    if(inOffice){
                        members.add(new CongressMember(id, name, party, state));
                        senate.add(new CongressMember(id, name, party, state));
                    }else{
                        pastMembers.add(new CongressMember(id, name, party, state));
                    }
                }

            }


        } catch(JSONException e) {
            e.printStackTrace();
        }



    }

    public void broadCastResults(){
        Intent broadcastIntent = new Intent(ACTION_RECEIVE_MSG);
        broadcastIntent.putExtra(EXTRA_MEMBERS,members);
        broadcastIntent.putExtra(EXTRA_SENATE,senate);
        broadcastIntent.putExtra(EXTRA_HOUSE,house);
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
