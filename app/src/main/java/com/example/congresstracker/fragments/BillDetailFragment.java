package com.example.congresstracker.fragments;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.activities.MyAreaActivity;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.other.BillTrackDatabaseHelper;
import com.example.congresstracker.receivers.AlarmReceiver;
import com.example.congresstracker.services.BillDataPull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillDetailFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String TAG = "BillDetail.TAG";
    public static final String EXTRA_BILL = "EXTRA_BILL";
    public static final String EXTRA_SELECT_BILL = "EXTRA_SELECT_BILL";
    public static final String EXTRA_MEMBER_ID = "EXTRA_MEMBER_ID";

    private TextView billNameTV;
    private TextView sponsorTV;
    private TextView statusTV;
    private TextView dateTV;
    private TextView summaryTV;
    private TextView actionDateTV;
    private TextView republicanCoTV;
    private TextView democratCoTV;
    private Button fullSummaryBtn;
    private ProgressBar loadingPb;

    private Bill selectedBill;

    private BottomNavigationView bottomNav;
    private BillDetailListener listener;

    BillTrackDatabaseHelper dbh;
    Cursor cursor;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.track_menu, menu);
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
            sponsorTV.setOnClickListener(this);
            statusTV = getView().findViewById(R.id.status_txt_lbl);
            dateTV = getView().findViewById(R.id.date_intro_txt);
            summaryTV = getView().findViewById(R.id.summary_txt);
            actionDateTV = getView().findViewById(R.id.latest_action_date_txt);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        dbh = BillTrackDatabaseHelper.getInstance(getContext());

        if(selectedBill != null){

            cursor = dbh.getAllBills();

            if(!(cursor.getCount() > 0)){

                dbh.trackBill(selectedBill);
                Cursor c = dbh.getAllBills();
                c.moveToLast();
                String id = c.getString(c.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_ID));
                Toast.makeText(getContext(), "Stored : id: "+ id, Toast.LENGTH_SHORT).show();

                Calendar calendar = Calendar.getInstance();
                long currentDateTime=calendar.getTimeInMillis();
                calendar.setTime(new Date(currentDateTime+(60*1000)));// 1 minutes timeout
                Intent myIntent = new Intent(getContext(), AlarmReceiver.class);
                PendingIntent mAlarmSender = PendingIntent.getBroadcast(getContext(), 0, myIntent, 0);
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), mAlarmSender);

            }else {
                cursor = dbh.getBillById(selectedBill.getBillNum());

                if(cursor.getCount() > 0){

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                    builder.setTitle("Untrack Bill");
                    builder.setMessage("You are already tracking this bill, would you like to stop tracking it?");

                    builder.setPositiveButton("Untrack", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cursor.moveToFirst();
                            String id = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_ID));
                            //// remove bill from database
                            dbh.mDatabase.delete(BillTrackDatabaseHelper.TABLE_NAME,"_id="+ id,null);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                }else {
                    /// bill is not already stored we can go ahead and insert it into the database
                    dbh.trackBill(selectedBill);

                    Cursor c = dbh.getAllBills();
                    c.moveToLast();
                    String id = c.getString(c.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_ID));

                    Toast.makeText(getContext(), "Stored : id: "+ id, Toast.LENGTH_SHORT).show();

                    Calendar calendar = Calendar.getInstance();
                    long currentDateTime=calendar.getTimeInMillis();
                    calendar.setTime(new Date(currentDateTime+(2*60*1000)));// 1 minutes timeout
                    Intent myIntent = new Intent(getContext(), AlarmReceiver.class);
                    PendingIntent mAlarmSender = PendingIntent.getBroadcast(getContext(), 0, myIntent, 0);
                    AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), mAlarmSender);
                }
            }

        }


        // for updating tracked bill
//            ContentValues cv = new ContentValues();
//            cv.put(BillTrackDatabaseHelper.COLUMN_BILL_ID, selectedBill.getBillNum());
//            cv.put(BillTrackDatabaseHelper.COLUMN_BILL_TITLE, selectedBill.getTitle());
//            cv.put(BillTrackDatabaseHelper.COLUMN_LAST_DATE, selectedBill.getLatestActionDate());
//            cv.put(BillTrackDatabaseHelper.COLUMN_LAST_VOTE, selectedBill.getLastVote());
//


        //Toast.makeText(getContext(), "Bill Tracked!", Toast.LENGTH_SHORT).show();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.view_full_sum_btn:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(selectedBill.getUrl()));
                startActivity(intent);
                break;

                // When the sponsors name is clicked
            case R.id.sponsor_txt_lbl:
                if(selectedBill != null) {

                    String memberId = selectedBill.getSponsorID();

                    Intent memberDetailIntent = new Intent(getContext(), CongressActivity.class);
                    memberDetailIntent.putExtra(EXTRA_MEMBER_ID,memberId);
                    startActivity(memberDetailIntent);
                }
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


    //Receiver handles a single bill request being returned
    //Updates UI with data pulled

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
