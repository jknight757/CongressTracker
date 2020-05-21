package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.fragments.CongressFragment;
import com.example.congresstracker.fragments.MemberDetailFragment;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.services.MemberDataPull;

public class CongressActivity extends AppCompatActivity implements CongressFragment.CongressClickListener, MemberDetailFragment.MemberDetailListener {

    public static final String TAG = "CongressActivity.TAG";
    public static final String EXTRA_SELECTED_MEMBER = "EXTRA_SELECTED_MEMBER";
    public static final String EXTRA_MEMBER_IMAGE = "EXTRA_MEMBER_IMAGE";
    public static final String EXTRA_MEMBER_ID = "EXTRA_MEMBER_ID";
    public static final String EXTRA_FROM_BILL = "EXTRA_FROM_BILL";


    private final MemberDataReceiver receiver = new MemberDataReceiver();
    private CongressFragment congressFragment;
    private ProgressBar progressBar;
    private CongressMember selectedMember;
    private Bitmap memImage;
    private String seniority;
    private View congressContainer;
    private boolean fromBill = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congress);


        congressContainer = findViewById(R.id.congress_fragment_container);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Congress" + "</font>"));
            //getSupportActionBar().setTitle(" Congress");
        }

        if(getIntent().hasExtra(EXTRA_MEMBER_ID)){

            String id = getIntent().getStringExtra(EXTRA_MEMBER_ID);
            progressBar = findViewById(R.id.mem_select_pb);
            progressBar.setVisibility(View.VISIBLE);

            Intent pullDataIntent = new Intent(this, MemberDataPull.class);
            pullDataIntent.setAction(MemberDataPull.ACTION_PULL_SELECTED);
            pullDataIntent.putExtra(MemberDataPull.EXTRA_SELECTED_MEMBER,id);
            startService(pullDataIntent);


        }
        else {

            congressFragment = CongressFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.congress_fragment_container, congressFragment).commit();

            Intent pullDataIntent = new Intent(this, MemberDataPull.class);
            pullDataIntent.setAction(MemberDataPull.ACTION_PULL_ALL);
            startService(pullDataIntent);
        }

    }




    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MemberDataPull.ACTION_SEND_MEM_DETAIL);
        registerReceiver(receiver,filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void MemberClicked(String id, String _seniority) {
        //findViewById(R.id.congress_fragment_container).setVisibility(View.GONE);
        progressBar = findViewById(R.id.mem_select_pb);
        progressBar.setVisibility(View.VISIBLE);
        seniority = _seniority;

        Intent pullDataIntent = new Intent(this, MemberDataPull.class);
        pullDataIntent.setAction(MemberDataPull.ACTION_PULL_SELECTED);
        pullDataIntent.putExtra(MemberDataPull.EXTRA_SELECTED_MEMBER,id);
        startService(pullDataIntent);
    }

    @Override
    public void DataReceived(boolean received) {

    }

    @Override
    public void updateTitle() {
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Congress" + "</font>"));
            //getSupportActionBar().setTitle(" Congress");
        }
    }


    class MemberDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            findViewById(R.id.congress_fragment_container).setVisibility(View.VISIBLE);
            Log.i(TAG, "onReceive: ");
            if(intent != null){

                if(intent.getAction().equals( MemberDataPull.ACTION_SEND_MEM_DETAIL)){

                    if(intent.hasExtra(EXTRA_SELECTED_MEMBER) && intent.hasExtra(EXTRA_MEMBER_IMAGE)){

                        selectedMember = (CongressMember) intent.getSerializableExtra(EXTRA_SELECTED_MEMBER);
                        selectedMember.setSeniority(seniority);
                        memImage = intent.getParcelableExtra(EXTRA_MEMBER_IMAGE);

                        if(getSupportActionBar() != null){
                            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#EFEFEF\">" + " Member Detail" + "</font>"));
                            //getSupportActionBar().setTitle(" Member Detail");
                        }
                        progressBar.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.congress_fragment_container, MemberDetailFragment.newInstance(selectedMember, memImage))
                                .addToBackStack(congressFragment.TAG)
                                .commit();

                    }
                }
            }

        }

    }
}
