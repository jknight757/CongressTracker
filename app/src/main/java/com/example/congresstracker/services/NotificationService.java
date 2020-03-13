package com.example.congresstracker.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.other.BillTrackDatabaseHelper;
import com.example.congresstracker.other.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationService extends IntentService {

    private static final int NOTIFICATION_ID = 0x0011;
    private static final String CHANNEL_ID = "BILL_CHANNEL";
    private static final String CHANNEL_NAME = "Bill Channel";

    private int mCounterID = 0;
    private ArrayList<Bill> trackedBills;
    private Bill selectedBill;

    ArrayList<String> trackedBillIds;
    ArrayList<String> trackedBillDates;
    ArrayList<String> trackedBillActive;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.i("TAG", "onHandleIntent: Notification");


        getLocalTracked();

        if(trackedBills != null){
            if(trackedBills.size() > 0){
                for (int i = 0; i < trackedBills.size(); i++) {
                    if(trackedBills.get(i).getLatestActionDate() == trackedBillDates.get(i)){
                        buildNotification();
                    }
                }
            }

        }


    }
    public void getLocalTracked(){
        BillTrackDatabaseHelper dbh;
        Cursor cursor;
        dbh = BillTrackDatabaseHelper.getInstance(this);


        cursor = dbh.getAllBills();


        if(cursor.getCount() > 0) {
            ArrayList<String> trackedBillUris = new ArrayList<>();
            trackedBillIds = new ArrayList<>();
            trackedBillActive = new ArrayList<>();
            trackedBillDates = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    String uri = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_URI));
                    String id = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_ID));
//                    String title = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_TITLE));
//                    String sponsor = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_SPONSOR));
//                    String dateIntro = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_DATE_INTRODUCED));
                    String lastDate = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_LAST_DATE));
                    int active = cursor.getInt(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_IS_ACTIVE));


                    trackedBillUris.add(uri);
                    trackedBillIds.add(id);
                    trackedBillDates.add(lastDate);
                    if(active == 1){
                        trackedBillActive.add("Active");
                    }else {
                        trackedBillActive.add("Inactive");
                    }

                }
            } finally {
                cursor.close();
            }

            if(trackedBillUris.size() > 0){
                pullListOfBills(trackedBillUris);
            }

        }else {

        }
    }
    public void pullListOfBills(ArrayList<String> bills){
        trackedBills = new ArrayList<>();

        for (String id :bills) {
            pullSelectedBill(id);
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


    private void buildNotification(){
        // check build version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //create a channel for a notification
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Main Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel Description");

            NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            if(mgr != null){
                mgr.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_account_balance_black_24dp);
        builder.setContentTitle("Bill Passed");
        builder.setContentText("This notification was triggered by an alarm");

        // create a pending intent to be attached to the notification, this tells the notification
        // what to do when teh notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);

        NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);


        if(mgr != null){
            mgr.notify(NOTIFICATION_ID + mCounterID++ , builder.build());

        }

    }
}
