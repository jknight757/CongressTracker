package com.example.congresstracker.fragments;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MyAreaActivity;
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
public class CongressFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "CongressFragment";
    private ArrayList<CongressMember> members;
    private ArrayList<CongressMember> senate;
    private ArrayList<CongressMember> house;
    private ArrayList<CongressMember> pastMembers;
    private ArrayList<CongressMember> allMembers;

    private ArrayList<CongressMember> filteredList;

    private static final String EXTRA_MEMBERS = "EXTRA_MEMBERS";
    private static final String EXTRA_SENATE = "EXTRA_SENATE";
    private static final String EXTRA_HOUSE = "EXTRA_HOUSE";
    public static final String EXTRA_PAST_MEMBERS = "EXTRA_PAST_MEMBERS";
    public static final String EXTRA_ALL_MEMBERS = "EXTRA_ALL_MEMBERS";

    public static final int ALL_MEMBERS_FILTER = 0;
    public static final int CURRENT_MEMBERS_FILTER = 1;
    public static final int PAST_MEMBERS_FILTER = 2;

    public static final int SENATE_MEMBERS_FILTER = 1;
    public static final int HOUSE_MEMBERS_FILTER = 2;


    public static final int D_MEMBERS_FILTER = 1;
    public static final int R_MEMBERS_FILTER = 2;
    public static final int I_MEMBERS_FILTER = 3;


    public int currentMidFilter = 0;
    public int currentBottomFilter = 0;

    ProgressBar loadingPB;
    TextInputEditText searchInputField;
    MaterialButton searchBtn;
    Spinner midFilter;
    Spinner bottomFilter;
    BottomNavigationView bottomNav;

    ListView membersLV;

    CongressClickListener listener;

    private final MembersDataReceiver receiver = new MembersDataReceiver();

    public CongressFragment() {
        // Required empty public constructor
    }

    public static CongressFragment newInstance() {
        
        Bundle args = new Bundle();
        
        CongressFragment fragment = new CongressFragment();
        fragment.setArguments(args);
        return fragment;
    }



    public interface CongressClickListener{
        void MemberClicked(String id);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof CongressClickListener){
            listener = (CongressClickListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_congress, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){
            membersLV = getView().findViewById(android.R.id.list);
            loadingPB = getView().findViewById(R.id.loadin_pb);

            if(members == null){
                loadingPB.setVisibility(View.VISIBLE);
            }else {
                loadingPB.setVisibility(View.GONE);
            }



            searchInputField = getView().findViewById(R.id.search_textInput);
            searchBtn = getView().findViewById(R.id.search_btn);
            searchBtn.setOnClickListener(this);
            searchBtn.setCheckable(false);

            midFilter = getView().findViewById(R.id.mid_filter_spinner);
            midFilter.setOnItemSelectedListener(this);
            bottomFilter = getView().findViewById(R.id.bottom_filter_spinner);
            bottomFilter.setOnItemSelectedListener(this);

            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setOnNavigationItemSelectedListener(this);

            membersLV.setOnItemClickListener(this);


        }

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MemberDataPull.ACTION_RECEIVE_MSG);
        //getContext().registerReceiver(receiver,filter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        //getContext().unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.congress_tab_item:
                break;
            case R.id.bill_tab_item:
                Intent billIntent = new Intent(getContext(), BillActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(billIntent);
                break;
            case R.id.local_tab_item:
                Intent congressIntent = new Intent(getContext(), MyAreaActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(congressIntent);

                break;
        }

        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            // search button is clicked, get text from search input
            // validate input, if input is valid then check search text against list
            // if list contains results display to listview
            case R.id.search_btn:

                String searchTxt = searchInputField.getText().toString();
                if(validateInput(searchTxt)){
                    loadingPB.setVisibility(View.VISIBLE);
                    searchMembersList(searchTxt);
                }
                break;

        }
    }

    public boolean validateInput(String _searchTxt){

        if(_searchTxt.isEmpty()){
            return false;
        }

        return true;
    }

    public void searchMembersList(String _searchTxt){
        ArrayList<CongressMember> searchResults = new ArrayList<>();
        for (CongressMember m:filteredList) {
            if(m.getName().toUpperCase().equals(_searchTxt.toUpperCase())){
                searchResults.add(m);
            }else if(m.getName().toUpperCase().contains(_searchTxt.toUpperCase())){
                searchResults.add(m);
            }
        }

        if(searchResults.size() > 0){
            showSearchResults(searchResults);
        }else{
            loadingPB.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("No Results Found");
            builder.setMessage("There were no results found matching "+ _searchTxt +
                    ", try changing filters to adjust where you are searching.");
            builder.setNegativeButton("Reset Filters", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    topFilter.setSelection(1);
                    midFilter.setSelection(0);
                    bottomFilter.setSelection(0);
                    dialog.cancel();
                    filteredList = members;
                    showFilteredList();
                }
            });

            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    //filteredList = members;
                    showFilteredList();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        }
    }

    public void showSearchResults(ArrayList<CongressMember> _searchResults){
        if (membersLV != null) {
            loadingPB.setVisibility(View.GONE);
            MemberAdapter adapter = new MemberAdapter(getContext(), _searchResults);
            membersLV.setAdapter(adapter);
            searchBtn.setCheckable(true);
            filteredList = _searchResults;
        }
    }

    // Gets the selected item for each filter
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()){


            case R.id.mid_filter_spinner:
                currentMidFilter = position;
                currentBottomFilter = bottomFilter.getSelectedItemPosition();
                break;

            case R.id.bottom_filter_spinner:
                currentBottomFilter = position;
                currentMidFilter = midFilter.getSelectedItemPosition();
                break;
        }

        updateFilteredLists();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "onNothingSelected: No Selection");
    }


    // filters based on selected spinner items
    public void updateFilteredLists(){

        filteredList = members;

        switch (currentMidFilter){
            case ALL_MEMBERS_FILTER:
                break;
            case SENATE_MEMBERS_FILTER:
                    filteredList = senate;
                break;
            case HOUSE_MEMBERS_FILTER:
                    filteredList = house;
                break;
        }

        ArrayList<CongressMember> tempFilter;
        switch (currentBottomFilter){
            case ALL_MEMBERS_FILTER:
                break;
            case D_MEMBERS_FILTER:
                tempFilter =  new ArrayList<>();
                for (CongressMember m: filteredList) {
                    if(m.getParty().equals("Democrat")){
                        tempFilter.add(m);
                    }
                }
                filteredList = tempFilter;
                break;
            case R_MEMBERS_FILTER:
                tempFilter =  new ArrayList<>();
                for (CongressMember m: filteredList) {
                    if(m.getParty().equals("Republican")){
                        tempFilter.add(m);
                    }
                }
                filteredList = tempFilter;
                break;
            case I_MEMBERS_FILTER:
                tempFilter =  new ArrayList<>();
                for (CongressMember m: filteredList) {
                    if(m.getParty().equals("Independent")){
                        tempFilter.add(m);
                    }
                }
                filteredList = tempFilter;
                break;
        }
        showFilteredList();
    }
    public void showFilteredList(){
        if (membersLV != null) {
            MemberAdapter adapter = new MemberAdapter(getContext(), filteredList);
            membersLV.setAdapter(adapter);
            searchBtn.setCheckable(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick: Position: "+ position);

        CongressMember selectedMember = filteredList.get(position);
        String memberID = selectedMember.getId();
        listener.MemberClicked(memberID);


    }

    // Receiver gets filtered congress member lists
    // Current members list, Past Members list, Senate list, House List,

    class MembersDataReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: ");
            if(intent.hasExtra(EXTRA_MEMBERS) && intent.hasExtra(EXTRA_SENATE) && intent.hasExtra(EXTRA_HOUSE)){

                members = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_MEMBERS);
                senate = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_SENATE);
                house = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_HOUSE);
                pastMembers = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_PAST_MEMBERS);
                allMembers = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_ALL_MEMBERS);
                filteredList = members;
                updateList();

            }
        }

        public void updateList(){
            if(filteredList != null) {
                if (membersLV != null) {
                    loadingPB.setVisibility(View.GONE);
                    MemberAdapter adapter = new MemberAdapter(getContext(), filteredList);
                    membersLV.setAdapter(adapter);
                    searchBtn.setCheckable(true);
                }
            }else{
                Log.i(TAG, "updateList: members list null");
            }
        }
    }

}
