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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.activities.MyAreaActivity;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.models.BillAdapter;
import com.example.congresstracker.models.BillDataPull;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.models.MemberAdapter;
import com.example.congresstracker.models.MemberDataPull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "BillFragment.TAG";

    public static final String EXTRA_HOUSE_BILLS = "EXTRA_HOUSE_BILLS";
    public static final String EXTRA_SENATE_BILLS = "EXTRA_SENATE_BILLS";
    public static final String EXTRA_ALL_BILLS = "EXTRA_ALL_BILLS";
    public static final String EXTRA_ALL_ACTIVE_BILLS = "EXTRA_ALL_ACTIVE_BILLS";
    public static final String EXTRA_SEARCH_TERM = "EXTRA_SEARCH_TERM";
    public static final String EXTRA_SEARCH_RESULT = "EXTRA_SEARCH_RESULT";

    private ArrayList<Bill> introHouseBills;
    private ArrayList<Bill> introSenateBills;
    private ArrayList<Bill> allBills;
    private ArrayList<Bill> allActiveBills;
    private ArrayList<Bill> filteredList;
    private ArrayList<Bill> searchResults;

    ListView billsListV;
    ProgressBar loadingPB;
    TextInputEditText searchInputField;
    MaterialButton searchBtn;
    BottomNavigationView bottomNav;

    private final BillsDataReceiver receiver = new BillsDataReceiver();
    private final SearchResultReceiver searchResultReceiver = new SearchResultReceiver();

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
        void BillClicked(Bill bill);
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
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BillDataPull.ACTION_SEND_BILLS);
        //getContext().registerReceiver(receiver,filter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BillDataPull.ACTION_SEND_RESULTS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(searchResultReceiver,intentFilter);


    }

    @Override
    public void onPause() {
        super.onPause();
        //getContext().unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(searchResultReceiver);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){
            billsListV = getView().findViewById(android.R.id.list);
            billsListV.setOnItemClickListener(this);
            loadingPB = getView().findViewById(R.id.loadin_pb);

            if(introHouseBills == null){
                loadingPB.setVisibility(View.VISIBLE);
            }else {
                loadingPB.setVisibility(View.GONE);
            }

            searchInputField = getView().findViewById(R.id.search_textInput);
            searchBtn = getView().findViewById(R.id.search_btn);
            searchBtn.setOnClickListener(this);

            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setSelectedItemId(R.id.bill_tab_item);
            bottomNav.setOnNavigationItemSelectedListener(this);

            if(allBills != null){
                loadingPB.setVisibility(View.GONE);
                BillAdapter adapter = new BillAdapter(getContext(), allBills);
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
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bill selectedBill = allBills.get(position);
        listener.BillClicked(selectedBill);
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
                filteredList = allBills;
                showSearchResults();
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

    public void searchBillList(String searchTxt){

        filteredList = new ArrayList<>();

        for (Bill b: allBills) {
            if(b.getShortTitle().toUpperCase().equals(searchTxt.toUpperCase())){
                filteredList.add(b);

            }

            if(b.getShortTitle().toUpperCase().contains(searchTxt.toUpperCase())){
                filteredList.add(b);
            }

        }

        if(filteredList.size() > 0){
            Toast.makeText(getContext(), "Search Matched", Toast.LENGTH_SHORT).show();
            showSearchResults();

        }

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
                introHouseBills = (ArrayList<Bill>)intent.getSerializableExtra(EXTRA_HOUSE_BILLS);
                introSenateBills = (ArrayList<Bill>)intent.getSerializableExtra(EXTRA_SENATE_BILLS);
                allBills = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_ALL_BILLS);
                allActiveBills = (ArrayList<Bill>) intent.getSerializableExtra(EXTRA_ALL_ACTIVE_BILLS);
                updateList();

            }
        }

        public void updateList(){
            if(allActiveBills != null) {
                if (billsListV != null) {
                    loadingPB.setVisibility(View.GONE);
                    BillAdapter adapter = new BillAdapter(getContext(), allActiveBills);
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
}
