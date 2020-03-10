package com.example.congresstracker.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.MainActivity;

public class NotificationService extends IntentService {

    private static final int NOTIFICATION_ID = 0x0011;
    private static final String CHANNEL_ID = "BILL_CHANNEL";
    private static final String CHANNEL_NAME = "Bill Channel";

    private int mCounterID = 0;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.i("TAG", "onHandleIntent: Notification");

        PullRemoteTrackedBills();


        buildNotification();


    }
    public void PullRemoteTrackedBills(){

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
