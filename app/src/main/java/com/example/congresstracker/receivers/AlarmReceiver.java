package com.example.congresstracker.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.congresstracker.services.BillDataPull;
import com.example.congresstracker.services.NotificationService;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String REMINDER_BUNDLE = "REMINDER_BUNDLE";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TAG", "onReceive: Alarm received starting service");
        Intent pullDataIntent = new Intent(context, NotificationService.class);
        context.startService(pullDataIntent);

    }
}
