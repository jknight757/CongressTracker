package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.congresstracker.R;
import com.example.congresstracker.models.BillDataPull;
import com.example.congresstracker.models.MemberDataPull;

public class BillActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);


        Intent pullDataIntent = new Intent(this, BillDataPull.class);
        pullDataIntent.setAction(BillDataPull.ACTION_PULL_BILLS);
        startService(pullDataIntent);
    }
}
