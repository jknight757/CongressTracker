package com.example.congresstracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.congresstracker.services.NotificationService;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = "BootReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: ");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest
                .Builder(NotificationService.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueue(request);


    }
}
