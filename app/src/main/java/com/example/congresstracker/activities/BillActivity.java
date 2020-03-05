package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

import com.example.congresstracker.R;
import com.example.congresstracker.fragments.BillDetailFragment;
import com.example.congresstracker.fragments.BillFragment;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.models.BillDataPull;
import com.example.congresstracker.models.MemberDataPull;

public class BillActivity extends AppCompatActivity implements BillFragment.BillClickListener, BillDetailFragment.BillDetailListener {

    private BillFragment billFragment;
    private BillDetailFragment billDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Bills" + "</font>"));
            //getSupportActionBar().setTitle(" Bills");
        }

        billFragment = BillFragment.newInstance();

        getSupportFragmentManager().beginTransaction().add(R.id.bill_fragment_container,billFragment).commit();


        Intent pullDataIntent = new Intent(this, BillDataPull.class);
        pullDataIntent.setAction(BillDataPull.ACTION_PULL_BILLS);
        startService(pullDataIntent);
    }

    @Override
    public void BillClicked(Bill bill) {

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Bill Detail" + "</font>"));
            //getSupportActionBar().setTitle(" Bill Detail");
        }

        billDetailFragment = BillDetailFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bill_fragment_container,billDetailFragment)
                .addToBackStack(billFragment.TAG)
                .commit();
    }

    @Override
    public void updateTitle() {
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Bills" + "</font>"));
            //getSupportActionBar().setTitle(" Bills");
        }
    }
}
