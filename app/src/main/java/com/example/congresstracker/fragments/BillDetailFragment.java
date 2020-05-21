package com.example.congresstracker.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.other.BillDetailAdapter;
import com.example.congresstracker.other.BillTrackDatabaseHelper;
import com.example.congresstracker.services.BillDataPull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillDetailFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = "BillDetail.TAG";
    public static final String EXTRA_BILL = "EXTRA_BILL";
    public static final String EXTRA_SELECT_BILL = "EXTRA_SELECT_BILL";
    public static final String EXTRA_MEMBER_ID = "EXTRA_MEMBER_ID";
    public static final String EXTRA_FROM_BILL = "EXTRA_FROM_BILL";

    private ArrayList<String> mDetails;

    private TextView billNameTV;
    private TextView billSummaryTV;
    private Button fullSummaryBtn;
    private ProgressBar loadingPb;

    private ListView detailsLV;

    private Bill selectedBill;

    // Bill Status textviews

    private TextView passed0;
    private TextView passed1;
    private TextView passed2;
    private TextView passed3;
    private TextView passed4;
    private TextView passed5;
    private TextView passed6;
    private TextView passed7;
    private View linLay0;
    private View linLay1;
    private View linLay2;
    private View linLay3;


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
        IntentFilter filter = new IntentFilter();
        filter.addAction(BillDataPull.ACTION_SEND_SELECT_BILL);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        if(getView()!= null){
            billNameTV = getView().findViewById(R.id.bill_title_lbl);
            billSummaryTV = getView().findViewById(R.id.bill_txt_lbl);
            billSummaryTV.setMovementMethod(new ScrollingMovementMethod());
            fullSummaryBtn = getView().findViewById(R.id.view_full_sum_btn);
            fullSummaryBtn.setOnClickListener(this);
            loadingPb = getView().findViewById(R.id.loading_selectbill_pb);
            loadingPb.setVisibility(View.VISIBLE);

            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setSelectedItemId(R.id.bill_tab_item);
            bottomNav.setOnNavigationItemSelectedListener(this);

            detailsLV = getView().findViewById(android.R.id.list);
            detailsLV.setOnItemClickListener(this);

            passed0 = getView().findViewById(R.id.txt_passed1_0);
            passed1 = getView().findViewById(R.id.txt_passed1_1);
            passed2 = getView().findViewById(R.id.txt_passed2_0);
            passed3 = getView().findViewById(R.id.txt_passed2_1);
            passed4 = getView().findViewById(R.id.txt_passed3_0);
            passed5 = getView().findViewById(R.id.txt_passed3_1);
            passed6 = getView().findViewById(R.id.txt_passed4_0);
            passed7 = getView().findViewById(R.id.txt_passed4_1);

            linLay0 = getView().findViewById(R.id.lay_passed0);
            linLay1 = getView().findViewById(R.id.lay_passed1);
            linLay2 = getView().findViewById(R.id.lay_passed2);
            linLay3 = getView().findViewById(R.id.lay_passed3);

            linLay0.setVisibility(View.GONE);
            linLay1.setVisibility(View.GONE);
            linLay2.setVisibility(View.GONE);
            linLay3.setVisibility(View.GONE);






            if(selectedBill != null){

                mDetails = new ArrayList<>();


                billSummaryTV.setText(selectedBill.getShortTitle());

                String sponsor = "Sponsor: " + selectedBill.getSponsor();

                String status = "Status: ";
                if(selectedBill.isActive()){
                    status += "Active";
                }else {
                    status+= "Inactive";
                }

                String date = "Date Introduced: ";
                date += selectedBill.getDateIntroduced();

                String sum = "Summary: ";

                sum += selectedBill.getSummary();

                mDetails.add(sponsor);
                mDetails.add(status);
                mDetails.add(date);
            }


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BillDataPull.ACTION_SEND_SELECT_BILL);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);
        //getContext().registerReceiver(receiver,filter);



    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        //getContext().unregisterReceiver(receiver);
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
                Toast.makeText(getContext(),  id + " Saved", Toast.LENGTH_SHORT).show();


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

                    Toast.makeText(getContext(),  id + " Saved", Toast.LENGTH_SHORT).show();

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (selectedBill != null && position == 0) {

            String memberId = selectedBill.getSponsorID();

            Intent memberDetailIntent = new Intent(getContext(), CongressActivity.class);
            memberDetailIntent.putExtra(EXTRA_MEMBER_ID,memberId);
            startActivity(memberDetailIntent);


        }
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
//            case R.id.sponsor_txt_lbl:
//
//                }
//                break;
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.congress_tab_item:
                Intent congressIntent = new Intent(getContext(), CongressActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                congressIntent.putExtra(EXTRA_FROM_BILL,true);
                startActivity(congressIntent);
                break;
            case R.id.bill_tab_item:
                break;

        }

        return false;
    }


    //Receiver handles a single bill request being returned
    //Updates UI with data pulled

    class SelectBillReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: Select Bill Received");
            if(intent.hasExtra(EXTRA_SELECT_BILL)){
                selectedBill = (Bill) intent.getSerializableExtra(EXTRA_SELECT_BILL);

                if(selectedBill != null){
                    updateUI();
                }
            }
        }

        public void updateUI(){
            String billNum = selectedBill.getBillNum();


                linLay0.setBackgroundColor(getContext().getResources().getColor(R.color.lightGreySolid));
                linLay1.setBackgroundColor(getContext().getResources().getColor(R.color.lightGreySolid));
                linLay2.setBackgroundColor(getContext().getResources().getColor(R.color.lightGreySolid));
                linLay3.setBackgroundColor(getContext().getResources().getColor(R.color.lightGreySolid));
                passed0.setTextColor(getContext().getResources().getColor(R.color.white));
                passed1.setTextColor(getContext().getResources().getColor(R.color.white));
                passed2.setTextColor(getContext().getResources().getColor(R.color.white));
                passed3.setTextColor(getContext().getResources().getColor(R.color.white));
                passed4.setTextColor(getContext().getResources().getColor(R.color.white));
                passed5.setTextColor(getContext().getResources().getColor(R.color.white));
                passed6.setTextColor(getContext().getResources().getColor(R.color.white));
                passed7.setTextColor(getContext().getResources().getColor(R.color.white));
                passed6.setTypeface(Typeface.DEFAULT_BOLD);
                passed7.setTypeface(Typeface.DEFAULT_BOLD);



            if(billNum.substring(0,1).toUpperCase().equals("H")){
                passed1.setText("House");
                passed3.setText("Senate");

                if(!selectedBill.isActive()){
                    linLay3.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                    passed6.setTypeface(Typeface.DEFAULT);
                    passed7.setTypeface(Typeface.DEFAULT);
                    passed6.setTextColor(getContext().getResources().getColor(R.color.textColor));
                    passed7.setTextColor(getContext().getResources().getColor(R.color.textColor));

                    if(selectedBill.isSenatePassage()){
                        passed4.setTypeface(Typeface.DEFAULT_BOLD);
                        passed5.setTypeface(Typeface.DEFAULT_BOLD);

                    }else if(selectedBill.isHousePassage()){
                        linLay2.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                        passed2.setTypeface(Typeface.DEFAULT_BOLD);
                        passed3.setTypeface(Typeface.DEFAULT_BOLD);
                        passed2.setTextColor(getContext().getResources().getColor(R.color.textColor));
                        passed3.setTextColor(getContext().getResources().getColor(R.color.textColor));

                    }else{
                        linLay1.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                        passed1.setTypeface(Typeface.DEFAULT_BOLD);
                        passed2.setTypeface(Typeface.DEFAULT_BOLD);
                        passed1.setTextColor(getContext().getResources().getColor(R.color.textColor));
                        passed2.setTextColor(getContext().getResources().getColor(R.color.textColor));
                    }

                }

            }else if(billNum.substring(0,1).toUpperCase().equals("S")){
                if(billNum.substring(0,3).toUpperCase().equals("S.R")){
                    passed0.setText("Introduced");
                    passed1.setVisibility(View.INVISIBLE);
                    passed3.setText("Senate");
                    linLay2.setVisibility(View.INVISIBLE);
                    linLay3.setVisibility(View.INVISIBLE);


                }else{
                    passed1.setText("Senate");
                    passed3.setText("House");

                    if(!selectedBill.isActive()){
                        linLay3.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                        passed6.setTypeface(Typeface.DEFAULT);
                        passed7.setTypeface(Typeface.DEFAULT);
                        passed6.setTextColor(getContext().getResources().getColor(R.color.textColor));
                        passed7.setTextColor(getContext().getResources().getColor(R.color.textColor));

                        if(selectedBill.isHousePassage()){
                            passed4.setTypeface(Typeface.DEFAULT_BOLD);
                            passed5.setTypeface(Typeface.DEFAULT_BOLD);

                        }else if(selectedBill.isSenatePassage()){
                            linLay1.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                            linLay2.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                            passed0.setTypeface(Typeface.DEFAULT_BOLD);
                            passed1.setTypeface(Typeface.DEFAULT_BOLD);
                            passed2.setTextColor(getContext().getResources().getColor(R.color.textColor));
                            passed3.setTextColor(getContext().getResources().getColor(R.color.textColor));

                        }else{
                            linLay0.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                            linLay1.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                            linLay2.setBackgroundColor(getContext().getResources().getColor(R.color.offWhite));
                            passed1.setTextColor(getContext().getResources().getColor(R.color.textColor));
                            passed0.setTextColor(getContext().getResources().getColor(R.color.textColor));
                            passed2.setTextColor(getContext().getResources().getColor(R.color.textColor));
                            passed3.setTextColor(getContext().getResources().getColor(R.color.textColor));
                            passed4.setTextColor(getContext().getResources().getColor(R.color.textColor));
                            passed5.setTextColor(getContext().getResources().getColor(R.color.textColor));

                        }

                    }
                }

            }



            String[] sumTitle;

            mDetails = new ArrayList<>();

            loadingPb.setVisibility(View.GONE);
            String title = selectedBill.getTitle();
            billSummaryTV.setText(title);

            String sponsor = "Sponsor: " + selectedBill.getSponsor();
            //sponsorTV.setTextColor(ContextCompat.getColor(getContext(),R.color.hyperLinkBlue));
            String status = "Status: ";
            if(selectedBill.isActive()){
                status += "Active";
            }else {
                status+= "Inactive";
            }


            String date = "Introduced: ";
            date += selectedBill.getDateIntroduced();

            if(!selectedBill.getSummary().isEmpty()){
                //billSummaryTV.setText(selectedBill.getSummary());
                sumTitle = selectedBill.getSummary().split("This");
                billNameTV.setText(sumTitle[0]);
                title += "\n" +  selectedBill.getSummary().substring(sumTitle[0].length());
                billSummaryTV.setText(title);
            }


            Log.i(TAG, "updateUI: Summary: "+ selectedBill.getSummary());



            String latestDate = "Last Action: ";
            latestDate += selectedBill.getLatestActionDate();


            String repCo = "Republican Cosponsors: ";
            repCo += selectedBill.getRepublicanCosponsors();


            String demCo = "Democrat Cosponsors: ";
            demCo += selectedBill.getDemocratCosponsors();



            mDetails.add(sponsor);
            mDetails.add(status);
            mDetails.add(date);
            mDetails.add(latestDate);
            mDetails.add(repCo);
            mDetails.add(demCo);

            if(detailsLV != null && mDetails != null){
                BillDetailAdapter adapter =  new BillDetailAdapter(getContext(), mDetails);
                detailsLV.setAdapter(adapter);
            }
        }
    }
}
