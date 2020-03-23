package com.example.congresstracker.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.congresstracker.models.Bill;
import com.example.congresstracker.other.BillTrackDatabaseHelper;
import com.example.congresstracker.other.NetworkUtils;

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
    public static final String ACTION_SEND_TRACKED_BILLS = "com.example.congresstracker.models.action.SEND_TRACKED_BILLS";

    public static final String ACTION_PULL_TRACKED = "com.example.congresstracker.services.action.PULL_TRACKED";


    public static final String EXTRA_UPDATED_BILLS = "EXTRA_UPDATED_BILLS";
    public static final String EXTRA_INTRODUCED_BILLS = "EXTRA_INTRODUCED_BILLS";
    public static final String EXTRA_PASSED_BILLS = "EXTRA_PASSED_BILLS";
    public static final String EXTRA_ALL_BILLS = "EXTRA_ALL_BILLS";
    public static final String EXTRA_ALL_ACTIVE_BILLS = "EXTRA_ALL_ACTIVE_BILLS";
    public static final String EXTRA_SEARCH_TERM = "EXTRA_SEARCH_TERM";
    public static final String EXTRA_SEARCH_RESULT = "EXTRA_SEARCH_RESULT";
    public static final String EXTRA_SELECTED_BILL = "EXTRA_SELECTED_BILL";
    public static final String EXTRA_SELECT_BILL = "EXTRA_SELECT_BILL";
    public static final String EXTRA_TRACKED_BILLS = "EXTRA_TRACKED_BILLS";
    public static final String EXTRA_TRACKED_RETURNED = "EXTRA_TRACKED_RETURNED";


    private ArrayList<Bill> introHouseBills;
    private ArrayList<Bill> introSenateBills;
    private ArrayList<Bill> allBills;
    private ArrayList<Bill> allActiveBills;
    private ArrayList<Bill> searchResults;
    private ArrayList<Bill> trackedBills;
    private ArrayList<String> trackedBillIDs;
    private ArrayList<Bill> recentlyIntroduced;
    private ArrayList<Bill> recentlyUpdated;
    private ArrayList<Bill> recentlyPassed;

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
                    trackedBillIDs = getMyTrackedBills();
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
                        Log.i(TAG, "pullSelectedBill: Summary: "+ selectedBill.getSummary());
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

                case ACTION_PULL_TRACKED:
                    ArrayList<String> billIDs = intent.getStringArrayListExtra(EXTRA_TRACKED_BILLS);

                    pullListOfBills(billIDs);

                    for (Bill b:trackedBills) {
                        Log.i(TAG, "onHandleIntent: Tracked Bill ID: " + b.getBillNum());
                        Log.i(TAG, "onHandleIntent: ----------------");
                        
                    }
                    if(trackedBills.size() > 0){
                        broadCastTrackedBills();
                    }
                    break;



            }

        }
    }
    public void pullListOfBills(ArrayList<String> bills){
        trackedBills = new ArrayList<>();

        for (String uri :bills) {
            pullSelectedBill(uri);
            if(selectedBill != null){
                trackedBills.add(selectedBill);
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
                    Log.i(TAG, "pullSelectedBill: Summary"+ summary);
                    Log.i(TAG, "pullSelectedBill: ------------");
                    Log.i(TAG, "pullSelectedBill: SummaryShort:"+ summaryShort);
                     Log.i(TAG, "pullSelectedBill: ---------------------------------");
                    String latestActionDate = obj.getString("latest_major_action_date");

                    String lastVote = "";
                    if(!obj.isNull("latest_major_action")){
                        lastVote  = obj.getString("latest_major_action");
                    }


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
                    selectedBill.setLastVote(lastVote);




            }catch (JSONException e){
                e.printStackTrace();

            }

        }

    }

    public void pullBills(){
        allBills = new ArrayList<>();
        allActiveBills = new ArrayList<>();
        recentlyIntroduced = new ArrayList<>();
        recentlyUpdated = new ArrayList<>();
        recentlyPassed = new ArrayList<>();

        String url = "";


        if(NetworkUtils.isConnected(getBaseContext())) {

            ArrayList<Bill> temp;
            ArrayList<Bill> shortTitles = new ArrayList<>();


            url = "https://api.propublica.org/congress/v1/116/both/bills/active.json";
            temp =  pullBillForURL(url);
            allActiveBills.addAll(temp);
//            for (Bill b: temp) {
//                pullSelectedBill(b.getBillUri());
//                shortTitles.add(selectedBill);
//            }
//            allActiveBills.addAll(shortTitles);




//            url = "https://api.propublica.org/congress/v1/115/both/bills/active.json";
//            temp =  pullBillForURL(url);
//            allActiveBills.addAll(temp);


            url = "https://api.propublica.org/congress/v1/116/both/bills/introduced.json";
            temp =  pullBillForURL(url);
            recentlyIntroduced.addAll(temp);
//            shortTitles = new ArrayList<>();
//            for (Bill b: temp) {
//                pullSelectedBill(b.getBillUri());
//                shortTitles.add(selectedBill);
//            }
//            recentlyIntroduced.addAll(shortTitles);



            url = "https://api.propublica.org/congress/v1/116/both/bills/updated.json";
            temp =  pullBillForURL(url);
            recentlyUpdated.addAll(temp);
//            shortTitles = new ArrayList<>();
//            for (Bill b: temp) {
//                pullSelectedBill(b.getBillUri());
//                shortTitles.add(selectedBill);
//            }
//            recentlyUpdated.addAll(shortTitles);

            url = "https://api.propublica.org/congress/v1/116/both/bills/passed.json";
            temp =  pullBillForURL(url);
            recentlyPassed.addAll(temp);


//            url = "https://api.propublica.org/congress/v1/113/both/bills/active.json";
//            temp =  pullBillForURL(url);
//            allActiveBills.addAll(temp);
//
//            url = "https://api.propublica.org/congress/v1/112/both/bills/active.json";
//            temp =  pullBillForURL(url);
//            allActiveBills.addAll(temp);
//
//            url = "https://api.propublica.org/congress/v1/111/both/bills/active.json";
//            temp =  pullBillForURL(url);
//            allActiveBills.addAll(temp);

            Collections.sort(allActiveBills);
            Collections.sort(recentlyUpdated);
            Collections.sort(recentlyIntroduced);
            Collections.sort(recentlyPassed);

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
                    String latestActionDate = nestedObj.getString("latest_major_action_date");
                    boolean active = nestedObj.getBoolean("active");
                    int cosponsors = nestedObj.getInt("cosponsors");
                    String billUrl = nestedObj.getString("congressdotgov_url");
                    String summary = nestedObj.getString("summary");

                    Bill b = new Bill(chamber,billNum,billUri,title,
                            shortTitle,sponsor,dateIntroduced,active,cosponsors,billUrl,summary);
                    b.setLatestActionDate(latestActionDate);

                    if(trackedBillIDs != null){
                        if(trackedBillIDs.contains(b.getBillNum())){
                            b.setTracking(true);
                        }
                    }
                    billsToReturn.add(b);

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

    public ArrayList<String> getMyTrackedBills(){

        ArrayList<String> trackedBillIds;
        BillTrackDatabaseHelper dbh;
        Cursor cursor;
        dbh = BillTrackDatabaseHelper.getInstance(this);
        cursor = dbh.getAllBills();

        if(cursor.getCount() > 0) {
            trackedBillIds = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    String uri = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_URI));
                    String id = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_ID));
//                    String title = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_TITLE));
//                    String sponsor = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_SPONSOR));
//                    String dateIntro = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_DATE_INTRODUCED));
//                    String lastDate = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_LAST_DATE));
//                    int active = cursor.getInt(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_ID));
                    trackedBillIds.add(id);

                }
            } finally {
                cursor.close();
            }
            return trackedBillIds;

        }
        return null;
    }

    public void broadCastBills(){
        Intent broadcastIntent;
        broadcastIntent = new Intent(ACTION_SEND_BILLS);
        broadcastIntent.putExtra(EXTRA_ALL_BILLS,allBills);
        broadcastIntent.putExtra(EXTRA_ALL_ACTIVE_BILLS, allActiveBills);
        broadcastIntent.putExtra(EXTRA_UPDATED_BILLS, recentlyUpdated);
        broadcastIntent.putExtra(EXTRA_INTRODUCED_BILLS, recentlyIntroduced);
        broadcastIntent.putExtra(EXTRA_PASSED_BILLS, recentlyPassed);
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
    public void broadCastTrackedBills(){
        Intent broadcastIntent;
        broadcastIntent = new Intent(ACTION_SEND_TRACKED_BILLS);
        broadcastIntent.putExtra(EXTRA_TRACKED_RETURNED, trackedBills);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }


}
