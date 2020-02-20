package com.example.congresstracker.fragments;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.congresstracker.R;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.models.MemberAdapter;
import com.example.congresstracker.models.MemberDataPull;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CongressFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

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

    public int currentTopFilter = 1;
    public int currentMidFilter = 0;
    public int currentBottomFilter = 0;

    ProgressBar loadingPB;
    TextInputEditText searchInputField;
    MaterialButton searchBtn;
    Spinner topFilter;
    Spinner midFilter;
    Spinner bottomFilter;

    ListView membersLV;

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
            loadingPB.setVisibility(View.VISIBLE);
            searchInputField = getView().findViewById(R.id.search_textInput);
            searchBtn = getView().findViewById(R.id.search_btn);
            searchBtn.setOnClickListener(this);
            searchBtn.setCheckable(false);

            topFilter = getView().findViewById(R.id.top_filter_spinner);
            topFilter.setOnItemSelectedListener(this);
            topFilter.setSelection(1);
            midFilter = getView().findViewById(R.id.mid_filter_spinner);
            midFilter.setOnItemSelectedListener(this);
            bottomFilter = getView().findViewById(R.id.bottom_filter_spinner);
            bottomFilter.setOnItemSelectedListener(this);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MemberDataPull.ACTION_RECEIVE_MSG);
        getContext().registerReceiver(receiver,filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
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
            if(m.getName().equals(_searchTxt)){
                searchResults.add(m);
            }else if(m.getName().contains(_searchTxt)){
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
                    topFilter.setSelection(1);
                    midFilter.setSelection(0);
                    bottomFilter.setSelection(0);
                    dialog.cancel();
                }
            });

            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
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
        }
    }

    // Gets the selected item for each filter
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        loadingPB.setVisibility(View.VISIBLE);

        switch (parent.getId()){
            case R.id.top_filter_spinner:
                Log.i(TAG, "onItemSelected: Spinner 1");
                currentTopFilter = position;
                currentMidFilter = midFilter.getSelectedItemPosition();
                currentBottomFilter = bottomFilter.getSelectedItemPosition();
                break;

            case R.id.mid_filter_spinner:
                currentMidFilter = position;
                currentTopFilter = topFilter.getSelectedItemPosition();
                currentBottomFilter = bottomFilter.getSelectedItemPosition();
                break;

            case R.id.bottom_filter_spinner:
                currentBottomFilter = position;
                currentTopFilter = topFilter.getSelectedItemPosition();
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
        switch (currentTopFilter){
            case ALL_MEMBERS_FILTER:
                filteredList = allMembers;
                break;
            case CURRENT_MEMBERS_FILTER:
                filteredList = members;
                break;
            case PAST_MEMBERS_FILTER:
                filteredList = pastMembers;
                break;
        }

        switch (currentMidFilter){
            case ALL_MEMBERS_FILTER:
                break;
            case SENATE_MEMBERS_FILTER:
                if(currentTopFilter == CURRENT_MEMBERS_FILTER){
                    filteredList = senate;
                }else{
                    ArrayList<CongressMember> tempFilter =  new ArrayList<>();
                    for (CongressMember m: filteredList) {
                        if(m.getChamber().equals("senate")){
                            tempFilter.add(m);
                        }
                    }
                    filteredList = tempFilter;
                }
                break;
            case HOUSE_MEMBERS_FILTER:
                if(currentTopFilter == CURRENT_MEMBERS_FILTER){
                    filteredList = house;
                }else{
                    ArrayList<CongressMember> tempFilter =  new ArrayList<>();
                    for (CongressMember m: filteredList) {
                        if(m.getChamber().equals("house")){
                            tempFilter.add(m);
                        }
                    }
                    filteredList = tempFilter;
                }
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
            loadingPB.setVisibility(View.GONE);
            MemberAdapter adapter = new MemberAdapter(getContext(), filteredList);
            membersLV.setAdapter(adapter);
            searchBtn.setCheckable(true);
        }
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
