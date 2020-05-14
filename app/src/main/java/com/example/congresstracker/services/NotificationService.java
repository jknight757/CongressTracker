package com.example.congresstracker.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.other.BillTrackDatabaseHelper;
import com.example.congresstracker.other.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationService extends Worker {

    public static final String TAG = "NotificationService.TAG";

    private static final int NOTIFICATION_ID = 0x0011;
    private static final String CHANNEL_ID = "BILL_CHANNEL";
    private static final String CHANNEL_NAME = "Bill Channel";

    private int mCounterID = 0;
    private ArrayList<Bill> trackedBills;
    private ArrayList<Bill> trackedBillsLocal;
    private ArrayList<Bill> updatedBillsR;
    private ArrayList<Bill> updatedBillsL;
    private Bill selectedBill;

    ArrayList<String> trackedBillIds;
    ArrayList<String> trackedBillDates;
    ArrayList<String> trackedBillActive;
    private Context mContext = getApplicationContext();

    public NotificationService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "onHandleIntent: Notification");


        getLocalTracked();
        checkUpdated();

        if(updatedBillsR != null){
            if(updatedBillsR.size() > 0){
                buildUpdateMSG();
            }
        }
        return Result.success();
    }
    public void checkUpdated(){

        if(trackedBills != null){
            updatedBillsR = new ArrayList<>();
            updatedBillsL = new ArrayList<>();
            if(trackedBills.size() > 0){
                Bill localBill = null;
                for (int i = 0; i < trackedBills.size(); i++) {
                    Bill b = trackedBills.get(i);
                    for (int j = 0; j < trackedBillsLocal.size(); j++) {
                        Bill temp = trackedBillsLocal.get(i);
                        if(b.getBillNum().equals(temp.getBillNum())){
                            localBill = temp;
                        }

                    }

                    if(b.getLatestActionDate() != null && localBill != null){
                        if(!b.getLatestActionDate().equals(localBill.getLatestActionDate())){
                            Log.i(TAG, "billsUpdated: "+ localBill.getBillNum() + "|| "+ b.getBillNum());
                            Log.i(TAG, "billsUpdated: Previous Date:"+ localBill.getLatestActionDate());
                            Log.i(TAG, "billsUpdated: New Date:"+ b.getLatestActionDate());
                            Log.i(TAG, "--------------------------------");
                            updatedBillsR.add(b);
                            updatedBillsL.add(localBill);
                        }

                    }


//                    if(trackedBillDates.contains(trackedBills.get(i).getLatestActionDate())){
//                        buildNotification();
//                    }

                }
            }

        }

    }
    public void buildUpdateMSG(){
        Bill b = updatedBillsR.get(0);
        String title = "Update to Bill "+ updatedBillsR.get(0).getBillNum();
        String msg ="";

        if(updatedBillsR.get(0).isActive() && !updatedBillsL.get(0).isActive()){
            msg = b.getShortTitle() + " has been Passed";
            buildNotification(title, msg);
        }

    }

    public void getLocalTracked(){
        BillTrackDatabaseHelper dbh;
        Cursor cursor;
        dbh = BillTrackDatabaseHelper.getInstance(mContext);


        cursor = dbh.getAllBills();


        if(cursor.getCount() > 0) {
            ArrayList<String> trackedBillUris = new ArrayList<>();
            trackedBillIds = new ArrayList<>();
            trackedBillActive = new ArrayList<>();
            trackedBillDates = new ArrayList<>();
            trackedBillsLocal = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    String uri = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_URI));
                    String id = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_ID));
                    String title = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_TITLE));
                    String sponsor = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_SPONSOR));
                    String dateIntro = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_DATE_INTRODUCED));
                    String lastDate = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_LAST_DATE));
                    int active = cursor.getInt(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_IS_ACTIVE));




                    trackedBillUris.add(uri);
                    trackedBillIds.add(id);
                    trackedBillDates.add(lastDate);
                    if(active == 1){
                        trackedBillsLocal.add(new Bill(id,title,uri,sponsor,dateIntro,lastDate,true));
                        trackedBillActive.add("Active");
                    }else {
                        trackedBillsLocal.add(new Bill(id,title,uri,sponsor,dateIntro,lastDate,false));
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


        if(NetworkUtils.isConnected(getApplicationContext())){
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


    private void buildNotification(String title, String msg){
        // check build version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //create a channel for a notification
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Main Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel Description");

            NotificationManager mgr = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            if(mgr != null){
                mgr.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_account_balance_black_24dp);
        builder.setContentTitle(title);
        builder.setContentText(msg);

        // create a pending intent to be attached to the notification, this tells the notification
        // what to do when teh notification is clicked
        Intent intent = new Intent(mContext, CongressActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);

        NotificationManager mgr = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        if(mgr != null){
            mgr.notify(NOTIFICATION_ID + mCounterID++ , builder.build());

        }

    }


}
