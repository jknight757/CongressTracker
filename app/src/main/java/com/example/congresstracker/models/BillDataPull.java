package com.example.congresstracker.models;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BillDataPull extends IntentService {

    public static final String TAG = "BillDataPull.TAG";

    public static final String ACTION_PULL_BILLS = "com.example.congresstracker.models.action.PULL_BILLS";
    public static final String ACTION_PULL_ONE_BILL = "com.example.congresstracker.models.action.PULL_ONE_BILL";

    private ArrayList<Bill> introHouseBills;
    private ArrayList<Bill> introSenateBills;

    public BillDataPull() {
        super("BillDataPull");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null){
            switch (intent.getAction()){
                case ACTION_PULL_BILLS:
                    Log.i(TAG, "onHandleIntent: action Pull All");
                    pullBills();
                    if(introHouseBills != null){
                        for (Bill b:introHouseBills) {
                            Log.i(TAG, "onBillsPulled: id: " + b.getBillNum());
                            Log.i(TAG, "onBillsPulled: title: " + b.getTitle());
                            Log.i(TAG, "onBillsPulled: chamber: " + b.getChamber());
                            Log.i(TAG, "onBillsPulled: sponsor: " + b.getSponsor());
                            Log.i(TAG, "onBillsPulled: Active: " + b.isActive());
                            Log.i(TAG, "________________________________" );
                        }
                    }
                    if(introSenateBills != null){
                        for (Bill b:introSenateBills) {
                            Log.i(TAG, "onBillsPulled: id: " + b.getBillNum());
                            Log.i(TAG, "onBillsPulled: title: " + b.getTitle());
                            Log.i(TAG, "onBillsPulled: chamber: " + b.getChamber());
                            Log.i(TAG, "onBillsPulled: sponsor: " + b.getSponsor());
                            Log.i(TAG, "onBillsPulled: Active: " + b.isActive());
                            Log.i(TAG, "________________________________" );
                        }
                    }

                    break;
                case ACTION_PULL_ONE_BILL:
                    break;
            }

        }
    }

    public void pullBills(){

        String url = "https://api.propublica.org/congress/v1/115/house/bills/introduced.json";


        if(NetworkUtils.isConnected(getBaseContext())) {
            String data = NetworkUtils.getNetworkData(url);
            try {
                JSONObject response = new JSONObject(data);
                JSONArray resultsJson = response.getJSONArray("results");
                introHouseBills = new ArrayList<>();

                for (int i = 0; i < resultsJson.length(); i++) {
                    JSONObject obj = resultsJson.getJSONObject(i);
                    JSONArray billsJson = obj.getJSONArray("bills");

                    for (int x = 0; x < billsJson.length(); x++) {
                        JSONObject nestedObj = billsJson.getJSONObject(x);

                        String chamber = obj.getString("chamber");
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

                        introHouseBills.add(new Bill(chamber,billNum,billUri,title,
                                shortTitle,sponsor,dateIntroduced,active,cosponsors,billUrl,summary));

                    }

                }



            }catch (JSONException e){
                e.printStackTrace();
            }

            url = "https://api.propublica.org/congress/v1/115/senate/bills/introduced.json";

            data = NetworkUtils.getNetworkData(url);
            try {
                JSONObject response = new JSONObject(data);
                JSONArray resultsJson = response.getJSONArray("results");
                introSenateBills = new ArrayList<>();

                for (int i = 0; i < resultsJson.length(); i++) {
                    JSONObject obj = resultsJson.getJSONObject(i);
                    JSONArray billsJson = obj.getJSONArray("bills");

                    for (int x = 0; x < billsJson.length(); x++) {
                        JSONObject nestedObj = billsJson.getJSONObject(x);

                        String chamber = obj.getString("chamber");
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

                        introSenateBills.add(new Bill(chamber,billNum,billUri,title,
                                shortTitle,sponsor,dateIntroduced,active,cosponsors,billUrl,summary));

                    }

                }



            }catch (JSONException e){
                e.printStackTrace();
            }


//            url = "https://api.propublica.org/congress/v1/115/both/bills/passed.json";
//            data = NetworkUtils.getNetworkData(url);
//            try {
//                JSONObject response = new JSONObject(data);
//                JSONArray resultsJson = response.getJSONArray("results");
//
//
//
//            }catch (JSONException e){
//                e.printStackTrace();
//            }
        }
    }


}
