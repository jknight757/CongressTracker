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
import java.util.Collections;

public class BillDataPull extends IntentService {

    public static final String TAG = "BillDataPull.TAG";

    public static final String ACTION_PULL_BILLS = "com.example.congresstracker.models.action.PULL_BILLS";
    public static final String ACTION_PULL_ONE_BILL = "com.example.congresstracker.models.action.PULL_ONE_BILL";
    public static final String ACTION_SEARCH_BILL = "com.example.congresstracker.models.action.SEARCH_BILL";
    public static final String ACTION_SEND_BILLS = "com.example.congresstracker.models.action.PULL_SEND_BILLS";
    public static final String ACTION_SEND_RESULTS = "com.example.congresstracker.models.action.PULL_SEND_RESULTS";
    public static final String ACTION_SEND_SELECT_BILL = "com.example.congresstracker.models.action.SEND_SELECT_BILL";


    public static final String EXTRA_HOUSE_BILLS = "EXTRA_HOUSE_BILLS";
    public static final String EXTRA_SENATE_BILLS = "EXTRA_SENATE_BILLS";
    public static final String EXTRA_ALL_BILLS = "EXTRA_ALL_BILLS";
    public static final String EXTRA_ALL_ACTIVE_BILLS = "EXTRA_ALL_ACTIVE_BILLS";
    public static final String EXTRA_SEARCH_TERM = "EXTRA_SEARCH_TERM";
    public static final String EXTRA_SEARCH_RESULT = "EXTRA_SEARCH_RESULT";
    public static final String EXTRA_SELECTED_BILL = "EXTRA_SELECTED_BILL";
    public static final String EXTRA_SELECT_BILL = "EXTRA_SELECT_BILL";

    private ArrayList<Bill> introHouseBills;
    private ArrayList<Bill> introSenateBills;
    private ArrayList<Bill> allBills;
    private ArrayList<Bill> allActiveBills;
    private ArrayList<Bill> searchResults;

    Bill selectedBill;

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

                    if(allBills != null){
                       broadCastBills();
                    }

                    break;
                case ACTION_PULL_ONE_BILL:
                    String id = intent.getStringExtra(EXTRA_SELECTED_BILL);
                    pullSelectedBill(id);

                    if(selectedBill != null){
                        Log.i(TAG, "One Bill Pulled: BillID: "+ selectedBill.getBillNum());
                        Log.i(TAG, "One Bill Pulled: Republican Cosponsors: "+ selectedBill.getRepublicanCosponsors());
                        Log.i(TAG, "One Bill Pulled: Democrat Cosponsors: "+ selectedBill.getDemocratCosponsors());
                        Log.i(TAG, "One Bill Pulled: Last Date: "+ selectedBill.getLatestActionDate());
                        broadCastSelectedBill();

                    }

                    break;
                case ACTION_SEARCH_BILL:
                    String keyword = intent.getStringExtra(EXTRA_SEARCH_TERM);
                    searchBill(keyword);

                    if(searchResults != null){
                        ArrayList<Bill> sortedResult = new ArrayList<>();
                        ArrayList<Bill> sortedResult2 = new ArrayList<>();
                        for (Bill b: searchResults) {


                            if(b.getTitle().contains(keyword)){
                                sortedResult.add(b);
                            }else {
                                sortedResult2.add(b);
                            }

                        }
                        sortedResult.addAll(sortedResult2);

                        for (Bill b: sortedResult) {

                            Log.i(TAG, "Search found: Bill name: " + b.getTitle());
                            Log.i(TAG, "------------------------------");


                        }

                        searchResults = sortedResult;
                        broadCastSearchResults();
                    }
                    break;

            }

        }
    }
    public void pullSelectedBill(String _billUri){


        if(NetworkUtils.isConnected(getBaseContext())){
            String url = _billUri;
            String data = NetworkUtils.getNetworkData(url);

            try{
                JSONObject response = new JSONObject(data);
                JSONArray resultsJson = response.getJSONArray("results");

                    JSONObject obj = resultsJson.getJSONObject(0);

                    String chamber = "";
                    if(!obj.isNull("chamber")){
                        chamber = obj.getString("chamber");
                    }

                    String billNum = obj.getString("number");
                    String billUri = obj.getString("bill_uri");
                    String title = obj.getString("title");
                    String shortTitle  = obj.getString("short_title");
                    String sponsor = obj.getString("sponsor");
                    String sponsorId = obj.getString("sponsor_id");
                    String dateIntroduced = obj.getString("introduced_date");
                    boolean active = obj.getBoolean("active");
                    int cosponsors = obj.getInt("cosponsors");
                    String billUrl = obj.getString("congressdotgov_url");
                    String summary = obj.getString("summary");
                    String summaryShort = obj.getString("summary_short");
                    String latestActionDate = obj.getString("latest_major_action_date");

                    JSONObject nestedObj = obj.getJSONObject("cosponsors_by_party");

                    int repCosponsors = 0;
                    if(!nestedObj.isNull("R")){
                        repCosponsors = nestedObj.getInt("R");
                    }

                    int demCosponsors = 0;
                    if(!nestedObj.isNull("D")){
                        demCosponsors = nestedObj.getInt("D");
                    }

                    selectedBill = new Bill(chamber,billNum,billUri,title,
                            shortTitle,sponsor,dateIntroduced,active,cosponsors,billUrl,summary);

                    selectedBill.setSponsorID(sponsorId);
                    selectedBill.setLatestActionDate(latestActionDate);
                    selectedBill.setRepublicanCosponsors(repCosponsors);
                    selectedBill.setDemocratCosponsors(demCosponsors);
                    selectedBill.setSummaryShort(summaryShort);




            }catch (JSONException e){
                e.printStackTrace();

            }

        }

    }

    public void pullBills(){
        allBills = new ArrayList<>();
        allActiveBills = new ArrayList<>();

        String url = "";


        if(NetworkUtils.isConnected(getBaseContext())) {

            ArrayList<Bill> temp;


            url = "https://api.propublica.org/congress/v1/116/both/bills/active.json";
            temp =  pullBillForURL(url);
            allActiveBills.addAll(temp);

            url = "https://api.propublica.org/congress/v1/115/both/bills/active.json";
            temp =  pullBillForURL(url);
            allActiveBills.addAll(temp);

            url = "https://api.propublica.org/congress/v1/114/both/bills/active.json";
            temp =  pullBillForURL(url);
            allActiveBills.addAll(temp);

            url = "https://api.propublica.org/congress/v1/113/both/bills/active.json";
            temp =  pullBillForURL(url);
            allActiveBills.addAll(temp);

            url = "https://api.propublica.org/congress/v1/112/both/bills/active.json";
            temp =  pullBillForURL(url);
            allActiveBills.addAll(temp);

            url = "https://api.propublica.org/congress/v1/111/both/bills/active.json";
            temp =  pullBillForURL(url);
            allActiveBills.addAll(temp);

            Collections.sort(allActiveBills);

        }
    }

    public void searchBill(String keyword){

        String url = "https://api.propublica.org/congress/v1/bills/search.json?query=" + "\""+ keyword + "\"";
        searchResults = pullBillForURL(url);

    }

    public ArrayList<Bill> pullBillForURL(String url){
        ArrayList<Bill> billsToReturn = new ArrayList<>();

        String data = NetworkUtils.getNetworkData(url);
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


        }catch (JSONException e){
            e.printStackTrace();
        }
        ArrayList<Bill> reverse = new ArrayList<>();
        for (int i = billsToReturn.size() -1 ; i >= 0 ; i--) {
            reverse.add(billsToReturn.get(i));

        }
        billsToReturn = reverse;

        return billsToReturn;

    }

    public void broadCastBills(){
        Intent broadcastIntent;
        broadcastIntent = new Intent(ACTION_SEND_BILLS);
        broadcastIntent.putExtra(EXTRA_HOUSE_BILLS, introHouseBills);
        broadcastIntent.putExtra(EXTRA_SENATE_BILLS, introSenateBills);
        broadcastIntent.putExtra(EXTRA_ALL_BILLS,allBills);
        broadcastIntent.putExtra(EXTRA_ALL_ACTIVE_BILLS, allActiveBills);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    public void broadCastSearchResults(){
        Intent broadcastIntent;
        broadcastIntent = new Intent(ACTION_SEND_RESULTS);
        broadcastIntent.putExtra(EXTRA_SEARCH_RESULT, searchResults);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    public void broadCastSelectedBill(){
        Intent broadcastIntent;
        broadcastIntent = new Intent(ACTION_SEND_SELECT_BILL);
        broadcastIntent.putExtra(EXTRA_SELECT_BILL, selectedBill);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }


}
