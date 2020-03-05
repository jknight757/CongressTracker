package com.example.congresstracker.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.activities.MyAreaActivity;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.models.BillDataPull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillDetailFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String TAG = "BillDetail.TAG";
    public static final String EXTRA_BILL = "EXTRA_BILL";
    public static final String EXTRA_SELECT_BILL = "EXTRA_SELECT_BILL";

    private TextView billNameTV;
    private TextView sponsorTV;
    private TextView statusTV;
    private TextView dateTV;
    private TextView summaryTV;
    private TextView actionDateTV;
    private TextView urlTxtBtn;
    private TextView republicanCoTV;
    private TextView democratCoTV;
    private Button fullSummaryBtn;
    private ProgressBar loadingPb;

    private Bill selectedBill;

    private BottomNavigationView bottomNav;
    private BillDetailListener listener;

    private SelectBillReceiver receiver = new SelectBillReceiver();

    public BillDetailFragment() {
        // Required empty public constructor
    }


    public static BillDetailFragment newInstance() {

        Bundle args = new Bundle();

        BillDetailFragment fragment = new BillDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public interface BillDetailListener{
        void updateTitle();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof BillDetailListener){
            listener = (BillDetailListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bill_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView()!= null){
            billNameTV = getView().findViewById(R.id.bill_txt_lbl);
            sponsorTV = getView().findViewById(R.id.sponsor_txt_lbl);
            statusTV = getView().findViewById(R.id.status_txt_lbl);
            dateTV = getView().findViewById(R.id.date_intro_txt);
            summaryTV = getView().findViewById(R.id.summary_txt);
            actionDateTV = getView().findViewById(R.id.latest_action_date_txt);
            urlTxtBtn = getView().findViewById(R.id.visit_url_txt);
            urlTxtBtn.setOnClickListener(this);
            republicanCoTV = getView().findViewById(R.id.repub_cosponsor_txt);
            democratCoTV = getView().findViewById(R.id.demo_cosponsor_txt);
            fullSummaryBtn = getView().findViewById(R.id.view_full_sum_btn);
            fullSummaryBtn.setOnClickListener(this);
            loadingPb = getView().findViewById(R.id.loading_selectbill_pb);
            loadingPb.setVisibility(View.VISIBLE);

            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setSelectedItemId(R.id.bill_tab_item);
            bottomNav.setOnNavigationItemSelectedListener(this);



            if(selectedBill != null){
                billNameTV.setText(selectedBill.getShortTitle());

                String sponsor = "Sponsor: " + selectedBill.getSponsor();
                sponsorTV.setText(sponsor);

                String status = "Status: ";
                if(selectedBill.isActive()){
                    status += "Active";
                }else {
                    status+= "Inactive";
                }
                statusTV.setText(status);

                String date = "Date Introduced: ";
                date += selectedBill.getDateIntroduced();
                dateTV.setText(date);

                String sum = "Summary: ";

                sum += selectedBill.getSummary();
                summaryTV.setText(sum);
            }


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BillDataPull.ACTION_SEND_SELECT_BILL);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);



    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.updateTitle();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.visit_url_txt:
                break;
            case R.id.view_full_sum_btn:
                break;
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.congress_tab_item:
                Intent congressIntent = new Intent(getContext(), CongressActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(congressIntent);
                break;
            case R.id.bill_tab_item:
                break;
            case R.id.local_tab_item:
                if(MainActivity.validUser){
                    Intent localIntent = new Intent(getContext(), MyAreaActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(localIntent);
                }else {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Not Signed In")
                            .setMessage("You must be signed in to use this feature. Would you like to go back to the login screen?")
                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNegativeButton("Back to Login", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent signoutIntent = new Intent(getContext(), MainActivity.class);
                                    startActivity(signoutIntent);

                                }
                            })
                            .show();
                }
                break;
        }

        return false;
    }


    class SelectBillReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(EXTRA_SELECT_BILL)){
                selectedBill = (Bill) intent.getSerializableExtra(EXTRA_SELECT_BILL);

                if(selectedBill != null){
                    updateUI();
                }
            }
        }

        public void updateUI(){
            loadingPb.setVisibility(View.GONE);
            billNameTV.setText(selectedBill.getShortTitle());

            String sponsor = "Sponsor: " + selectedBill.getSponsor();
            sponsorTV.setText(sponsor);

            String status = "Status: ";
            if(selectedBill.isActive()){
                status += "Active";
            }else {
                status+= "Inactive";
            }
            statusTV.setText(status);

            String date = "Date Introduced: ";
            date += selectedBill.getDateIntroduced();
            dateTV.setText(date);

//            String sum = "Summary: ";
//            sum += selectedBill.getSummaryShort();
//            summaryTV.setText(sum);

            String latestDate = "Latest Action Date: ";
            latestDate += selectedBill.getLatestActionDate();
            actionDateTV.setText(latestDate);


            String repCo = "Republican Cosponsors: ";
            repCo += selectedBill.getRepublicanCosponsors();

            republicanCoTV.setText(repCo);

            String demCo = "Democrat Cosponsors: ";
            demCo += selectedBill.getDemocratCosponsors();
            democratCoTV.setText(demCo);
        }
    }
}
