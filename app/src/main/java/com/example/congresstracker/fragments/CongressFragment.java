package com.example.congresstracker.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.congresstracker.R;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.models.MemberAdapter;
import com.example.congresstracker.models.MemberDataPull;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CongressFragment extends Fragment {

    public static final String TAG = "CongressFragment";

    ArrayList<CongressMember> members;
    ArrayList<CongressMember> senate;
    ArrayList<CongressMember> house;
    public static final String EXTRA_MEMBERS = "EXTRA_MEMBERS";
    public static final String EXTRA_SENATE = "EXTRA_SENATE";
    public static final String EXTRA_HOUSE = "EXTRA_HOUSE";

    ProgressBar loadingPB;

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

    class MembersDataReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: ");
            if(intent.hasExtra(EXTRA_MEMBERS) && intent.hasExtra(EXTRA_SENATE) && intent.hasExtra(EXTRA_HOUSE)){

                members = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_MEMBERS);
                senate = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_SENATE);
                house = (ArrayList<CongressMember>) intent.getSerializableExtra(EXTRA_HOUSE);
                updateList();

            }
        }

        public void updateList(){
            if(members != null) {
                if (membersLV != null) {
                    loadingPB.setVisibility(View.GONE);
                    MemberAdapter adapter = new MemberAdapter(getContext(), members);
                    membersLV.setAdapter(adapter);
                }
            }else{
                Log.i(TAG, "updateList: members list null");
            }
        }
    }

}
