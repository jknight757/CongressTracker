package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.example.congresstracker.R;
import com.example.congresstracker.fragments.CongressFragment;
import com.example.congresstracker.models.MemberDataPull;

public class CongressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congress);


        getSupportFragmentManager().beginTransaction()
                .add(R.id.congress_fragment_container, CongressFragment.newInstance()).commit();

        Intent pullDataIntent = new Intent(this, MemberDataPull.class);
        pullDataIntent.setAction(MemberDataPull.ACTION_PULL_ALL);
        startService(pullDataIntent);
    }
}
