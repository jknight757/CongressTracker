package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

import com.example.congresstracker.R;
import com.example.congresstracker.fragments.MyAreaFragment;
import com.example.congresstracker.services.UserDataPull;

public class MyAreaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_area);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Profile" + "</font>"));
            //getSupportActionBar().setTitle(" Profile");

        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.profile_fragment_container, MyAreaFragment.newInstance()).commit();


        Intent pullDataIntent = new Intent(this, UserDataPull.class);
        pullDataIntent.setAction(UserDataPull.ACTION_PULL_PROFILE);
        startService(pullDataIntent);

    }
}
