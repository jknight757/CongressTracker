package com.example.congresstracker.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.MyAreaActivity;
import com.example.congresstracker.models.BillVote;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.models.MemberAdapter;
import com.example.congresstracker.models.MemberDataPull;
import com.example.congresstracker.models.VoteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberDetailFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MemberDetail.TAG";

    private static final String EXTRA_SELECTED_MEMBER = "EXTRA_SELECTED_MEMBER";
    public static final String EXTRA_MEMBER_VOTES = "EXTRA_MEMBER_VOTES";

    private ArrayList<BillVote> memberVotes;

    private CongressMember selectedMember;
    private TextView nameTV;
    private TextView partyTV;
    private TextView stateTV;
    private TextView totalVotesTV;
    private TextView missedVotePctTV;
    private TextView voteWPartyTV;
    private TextView voteAPartyTV;
    private ListView voteListView;

    private ProgressBar progressBar;
    BottomNavigationView bottomNav;

    MemberDetailListener listener;

    private final MemberVotesReceiver receiver = new MemberVotesReceiver();


    public MemberDetailFragment() {
        // Required empty public constructor
    }

    public static MemberDetailFragment newInstance(CongressMember member) {

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SELECTED_MEMBER,member);
        MemberDetailFragment fragment = new MemberDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }



    public interface MemberDetailListener{
        void updateTitle();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_member_detail, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MemberDetailListener){
            listener = (MemberDetailListener) context;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null && getArguments() != null){

            selectedMember = (CongressMember) getArguments().getSerializable(EXTRA_SELECTED_MEMBER);
            nameTV = getView().findViewById(R.id.name_txt_lbl);
            partyTV = getView().findViewById(R.id.party_txt_lbl);
            stateTV = getView().findViewById(R.id.state_txt_lbl);
            totalVotesTV = getView().findViewById(R.id.total_votes);
            missedVotePctTV = getView().findViewById(R.id.missed_votes_pct);
            voteWPartyTV = getView().findViewById(R.id.vote_wparty_pct);
            voteAPartyTV = getView().findViewById(R.id.vote_aparty_pct);
            progressBar = getView().findViewById(R.id.loadin_votes_pb);
            voteListView = getView().findViewById(android.R.id.list);
            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setOnNavigationItemSelectedListener(this);

            if(selectedMember != null){
                updateUI();

                String id = selectedMember.getId();
                progressBar.setVisibility(View.VISIBLE);
                Intent pullDataIntent = new Intent(getContext(), MemberDataPull.class);
                pullDataIntent.setAction(MemberDataPull.ACTION_PULL_VOTES);
                pullDataIntent.putExtra(MemberDataPull.EXTRA_SELECTED_MEMBER,id);
                getContext().startService(pullDataIntent);
            }




        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.congress_tab_item:

                break;
            case R.id.bill_tab_item:
                break;
            case R.id.local_tab_item:
                Intent congressIntent = new Intent(getContext(), MyAreaActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(congressIntent);
                break;
        }
        return false;
    }

    public void updateUI()
    {
        nameTV.setText(selectedMember.getName());
        partyTV.setText(selectedMember.getParty());
        stateTV.setText(selectedMember.getUnabreviated());
        String totalVStr = selectedMember.getTotalVotes()+ "";
        totalVotesTV.setText(totalVStr);
        String missVoteStr = selectedMember.getMissedVotePctAverage() + "";
        missedVotePctTV.setText(missVoteStr);
        String voteWPStr = selectedMember.getVoteWPPctAverage() + "";
        voteWPartyTV.setText(voteWPStr);
        String voteAPStr = selectedMember.getVoteAPPctAverage() + "";
        voteAPartyTV.setText(voteAPStr);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MemberDataPull.ACTION_SEND_MEM_VOTES);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        //getContext().unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.updateTitle();
    }

    class MemberVotesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: ");
            if (intent.hasExtra(EXTRA_MEMBER_VOTES)) {
                memberVotes = (ArrayList<BillVote>) intent.getSerializableExtra(EXTRA_MEMBER_VOTES);
                progressBar.setVisibility(View.GONE);
                updateUI();

            }
        }

        public void updateUI(){
            if(memberVotes != null) {
                if (voteListView != null) {
                    VoteAdapter adapter = new VoteAdapter(getContext(), memberVotes);
                    voteListView.setAdapter(adapter);
                }
            }else{
                Log.i(TAG, "updateList: members list null");
            }
        }
    }
}
