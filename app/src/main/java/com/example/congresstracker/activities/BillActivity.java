package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

import com.example.congresstracker.R;
import com.example.congresstracker.fragments.BillDetailFragment;
import com.example.congresstracker.fragments.BillFragment;
import com.example.congresstracker.services.BillDataPull;

public class BillActivity extends AppCompatActivity implements BillFragment.BillClickListener, BillDetailFragment.BillDetailListener {

    private BillFragment billFragment;
    private BillDetailFragment billDetailFragment;
    public static final String EXTRA_SELECT_BILL = "EXTRA_SELECT_BILL";
    public static final String EXTRA_SELECTED_BILL = "EXTRA_SELECTED_BILL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Bills" + "</font>"));
        }

        if(getIntent().hasExtra(EXTRA_SELECT_BILL)){
            String billUri = getIntent().getStringExtra(EXTRA_SELECT_BILL);

            billDetailFragment = BillDetailFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.bill_fragment_container,billDetailFragment)
                    .commit();

            Intent pullDataIntent = new Intent(this, BillDataPull.class);
            pullDataIntent.setAction(BillDataPull.ACTION_PULL_ONE_BILL);
            pullDataIntent.putExtra(EXTRA_SELECTED_BILL, billUri);
            startService(pullDataIntent);


        }else {
            billFragment = BillFragment.newInstance();

            getSupportFragmentManager().beginTransaction().add(R.id.bill_fragment_container,billFragment).commit();


            Intent pullDataIntent = new Intent(this, BillDataPull.class);
            pullDataIntent.setAction(BillDataPull.ACTION_PULL_BILLS);
            startService(pullDataIntent);
        }

    }

    @Override
    public void BillClicked(String id) {

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + id+ "</font>"));
        }

        billDetailFragment = BillDetailFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bill_fragment_container,billDetailFragment)
                .addToBackStack(billFragment.TAG)
                .commit();
    }

    @Override
    public void FilterClicked(String filter) {
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + filter+ "</font>"));
        }
    }

    @Override
    public void updateTitle() {
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Bills" + "</font>"));

        }
    }
}
