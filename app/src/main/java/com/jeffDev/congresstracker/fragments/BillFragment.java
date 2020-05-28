package com.jeffDev.congresstracker.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jeffDev.congresstracker.R;
import com.jeffDev.congresstracker.activities.CongressActivity;
import com.jeffDev.congresstracker.models.Bill;
import com.jeffDev.congresstracker.other.BillAdapter;
import com.jeffDev.congresstracker.other.BillTrackDatabaseHelper;
import com.jeffDev.congresstracker.services.BillDataPull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "BillFragment.TAG";

    public static final String EXTRA_UPDATED_BILLS = "EXTRA_UPDATED_BILLS";
    public static final String EXTRA_INTRODUCED_BILLS = "EXTRA_INTRODUCED_BILLS";
    public static final String EXTRA_ALL_BILLS = "EXTRA_ALL_BILLS";
    public static final String EXTRA_ALL_ACTIVE_BILLS = "EXTRA_ALL_ACTIVE_BILLS";
    public static final String EXTRA_SEARCH_TERM = "EXTRA_SEARCH_TERM";
    public static final String EXTRA_SEARCH_RESULT = "EXTRA_SEARCH_RESULT";
    public static final String EXTRA_SELECTED_BILL = "EXTRA_SELECTED_BILL";
    public static final String EXTRA_TRACKED_BILLS = "EXTRA_TRACKED_BILLS";
    public static final String EXTRA_TRACKED_RETURNED = "EXTRA_TRACKED_RETURNED";
    public static final String EXTRA_PASSED_BILLS = "EXTRA_PASSED_BILLS";
    public static final String EXTRA_FROM_BILL = "EXTRA_FROM_BILL";

    private int selectedSubTab = 0;

    public static final int ALL_BILL_TAB = 0;
    public static final int TRACKED_BILL_TAB = 1;

    BillTrackDatabaseHelper dbh;
    Cursor cursor;


    private ArrayList<Bill> recentlyUpdated;
    private ArrayList<Bill> recentlyIntroduced;
    private ArrayList<Bill> recentlyPassed;
    private ArrayList<Bill> allBills;
    private ArrayList<Bill> allActiveBills;
    private ArrayList<Bill> filteredList;
    private ArrayList<Bill> lastShownList;
    private ArrayList<Bill> searchResults;
    private ArrayList<Bill> trackedBills;

    private ListView billsListV;
    private ProgressBar loadingPB;
    private TextInputEditText searchInputField;
    private MaterialButton searchBtn;
    private BottomNavigationView bottomNav;
    private TextView allBillsBtn;
    private TextView trackedBillsBtn;
    private View allTabSelect;
    private View trackTabSelect;
    private View emptyView;
    private TextView emptyText;

    private final BillsDataReceiver receiver = new BillsDataReceiver();
    private final SearchResultReceiver searchResultReceiver = new SearchResultReceiver();
    private final TrackedBillsReceiver trackedBillsReceiver = new TrackedBillsReceiver();

    private BillClickListener listener;

    public BillFragment() {
        // Required empty public constructor
    }

    public static BillFragment newInstance() {

        Bundle args = new Bundle();

        BillFragment fragment = new BillFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface BillClickListener {
        void BillClicked(String id, String uri);
        void FilterClicked(String filter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof BillClickListener){
            listener = (BillClickListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bill, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BillDataPull.ACTION_SEND_BILLS);
        //getContext().registerReceiver(receiver,filter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BillDataPull.ACTION_SEND_RESULTS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchResultReceiver,intentFilter);

        IntentFilter trackedFilter = new IntentFilter();
        trackedFilter.addAction(BillDataPull.ACTION_SEND_TRACKED_BILLS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(trackedBillsReceiver,trackedFilter);


    }

    @Override
    public void onPause() {
        super.onPause();
        //getContext().unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(searchResultReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(trackedBillsReceiver);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bill_filter,menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){
            billsListV = getView().findViewById(android.R.id.list);
            billsListV.setOnItemClickListener(this);
            loadingPB = getView().findViewById(R.id.loadin_pb);

            if(allActiveBills == null){
                loadingPB.setVisibility(View.VISIBLE);
            }else {
                loadingPB.setVisibility(View.GONE);
            }
            selectedSubTab = ALL_BILL_TAB;

            searchInputField = getView().findViewById(R.id.search_textInput);
            searchBtn = getView().findViewById(R.id.search_btn);
            searchBtn.setOnClickListener(this);

            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setSelectedItemId(R.id.bill_tab_item);
            bottomNav.setOnNavigationItemSelectedListener(this);

            allBillsBtn = getView().findViewById(R.id.all_bill_btn);
            allBillsBtn.setOnClickListener(this);

            trackedBillsBtn = getView().findViewById(R.id.tracked_bill_btn);
            trackedBillsBtn.setOnClickListener(this);

            allTabSelect = getView().findViewById(R.id.all_bill_select);
            trackTabSelect = getView().findViewById(R.id.track_bill_select);

            emptyView = getView().findViewById(R.id.empty_view);
            emptyText = getView().findViewById(R.id.empty_text);

            if(filteredList != null){
                if(lastShownList != null){
                    filteredList = lastShownList;
                }
                loadingPB.setVisibility(View.GONE);
                BillAdapter adapter = new BillAdapter(getContext(), filteredList);
                billsListV.setAdapter(adapter);
                searchBtn.setCheckable(true);
            }



        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search_btn:
                String searchTxt = searchInputField.getText().toString();
                if(searchTxt.isEmpty()){
                    Toast.makeText(getContext(), "Search Field Empty", Toast.LENGTH_SHORT).show();
                    filteredList = allBills;
                    showSearchResults();
                }else {
                    searchAPI(searchTxt);
                    //searchBillList(searchTxt);
                }
                break;

            case R.id.all_bill_btn:
                if(selectedSubTab == TRACKED_BILL_TAB){
                    billsListV.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.GONE);
                    filteredList = lastShownList;

                    if(filteredList != null) {
                        if (billsListV != null) {
                            BillAdapter adapter = new BillAdapter(getContext(), filteredList);
                            billsListV.setAdapter(adapter);
                        }
                    }

                    allTabSelect.setVisibility(View.INVISIBLE);
                    trackTabSelect.setVisibility(View.VISIBLE);

                    selectedSubTab = ALL_BILL_TAB;

                }
                break;
            case R.id.tracked_bill_btn:
                if(selectedSubTab == ALL_BILL_TAB){
                    dbh = BillTrackDatabaseHelper.getInstance(getContext());
                    cursor = dbh.getAllBills();
                    lastShownList = filteredList;


                    if(trackedBills == null){
                        getTrackedBills();
                    }else if(trackedBills.size() != cursor.getCount()){
                        getTrackedBills();
                    } else {
                        filteredList = trackedBills;
                        if (billsListV != null) {
                            billsListV.setVisibility(View.VISIBLE);
                            BillAdapter adapter = new BillAdapter(getContext(), filteredList);
                            billsListV.setAdapter(adapter);

                        }
                    }

                    allTabSelect.setVisibility(View.VISIBLE);
                    trackTabSelect.setVisibility(View.INVISIBLE);

                    selectedSubTab = TRACKED_BILL_TAB;

                }
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_dropdown1:
                filteredList = allActiveBills;
                showSearchResults();
                listener.FilterClicked("All Active Bills");
                break;
            case R.id.action_dropdown2:
                filteredList = recentlyUpdated;
                showSearchResults();
                listener.FilterClicked("Recently Updated Bills");
                break;
            case R.id.action_dropdown3:
                filteredList = recentlyIntroduced;
                showSearchResults();
                listener.FilterClicked("Recently Introduced Bills");
                break;
            case R.id.action_dropdown4:
                filteredList = recentlyPassed;
                showSearchResults();
                listener.FilterClicked("Recently Passed Bills");
                break;

        }


        return super.onOptionsItemSelected(item);
    }


    public void getTrackedBills(){


        dbh = BillTrackDatabaseHelper.getInstance(getContext());


        cursor = dbh.getAllBills();


        if(cursor.getCount() > 0) {
            ArrayList<String> trackedBillIds = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    String uri = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_URI));
//                    String title = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_TITLE));
//                    String sponsor = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_SPONSOR));
//                    String dateIntro = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_DATE_INTRODUCED));
//                    String lastDate = cursor.getString(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_LAST_DATE));
//                    int active = cursor.getInt(cursor.getColumnIndex(BillTrackDatabaseHelper.COLUMN_BILL_ID));
                    trackedBillIds.add(uri);

                }
            } finally {
                cursor.close();
            }

            if(trackedBillIds.size() > 0){

                Intent pullDataIntent = new Intent(getContext(), BillDataPull.class);
                pullDataIntent.setAction(BillDataPull.ACTION_PULL_TRACKED);
                pullDataIntent.putStringArrayListExtra(EXTRA_TRACKED_BILLS,trackedBillIds);
                getContext().startService(pullDataIntent);

            }

        }else {
            billsListV.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(), "No Saved Bills", Toast.LENGTH_SHORT).show();
            emptyView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bill selectedBill = filteredList.get(position);
        String billUri = selectedBill.getBillUri();
        listener.BillClicked(selectedBill.getBillNum(), billUri);





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
                filteredList = recentlyUpdated;
                showSearchResults();
                break;

        }

        return false;
    }


    public void searchAPI(String searchTxt){
        Intent pullDataIntent = new Intent(getContext(), BillDataPull.class);
        pullDataIntent.setAction(BillDataPull.ACTION_SEARCH_BILL);
        pullDataIntent.putExtra(EXTRA_SEARCH_TERM, searchTxt);
        getContext().startService(pullDataIntent);
    }

    public void showSearchResults(){
        if (billsListV != null) {
            BillAdapter adapter = new BillAdapter(getContext(), filteredList);
            billsListV.setAdapter(adapter);
            searchBtn.setCheckable(true);
        }


    }

    class BillsDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: ");
            if(intent.hasExtra(EXTRA_ALL_BILLS)){
                allBills = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_ALL_BILLS);
                allActiveBills = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_ALL_ACTIVE_BILLS);
                recentlyIntroduced = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_INTRODUCED_BILLS);
                recentlyUpdated = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_UPDATED_BILLS);
                recentlyPassed = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_PASSED_BILLS);
                updateList();

            }
        }

        public void updateList(){
            if(recentlyPassed != null) {
                filteredList = recentlyPassed;
                if (billsListV != null) {
                    loadingPB.setVisibility(View.GONE);
                    BillAdapter adapter = new BillAdapter(getContext(), filteredList);
                    billsListV.setAdapter(adapter);
                    searchBtn.setCheckable(true);
                }
            }else{
                Log.i(TAG, "updateList: members list null");
            }
        }
    }

    class SearchResultReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Search Returned: ----------");
            if(intent.hasExtra(EXTRA_SEARCH_RESULT)){
                searchResults = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_SEARCH_RESULT);
                updateList();
            }
        }
        public void updateList(){
            if(searchResults != null) {
                filteredList = searchResults;
                if (billsListV != null) {
                    loadingPB.setVisibility(View.GONE);
                    BillAdapter adapter = new BillAdapter(getContext(), searchResults);
                    billsListV.setAdapter(adapter);
                    searchBtn.setCheckable(true);
                }
            }else{
                Log.i(TAG, "updateList: members list null");
            }
        }

    }

    class TrackedBillsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: Tracked Bills Received");
            if(intent.hasExtra(EXTRA_TRACKED_RETURNED)){
                trackedBills = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_TRACKED_RETURNED);
                updateUI();
            }
        }
        public void updateUI(){
            if(trackedBills != null) {
                filteredList = trackedBills;
                if (billsListV != null) {
                    billsListV.setVisibility(View.VISIBLE);
                    loadingPB.setVisibility(View.GONE);
                    BillAdapter adapter = new BillAdapter(getContext(), trackedBills);
                    billsListV.setAdapter(adapter);
                    searchBtn.setCheckable(true);
                }
            }else{
                Log.i(TAG, "updateList: members list null");
            }
        }
    }
}
