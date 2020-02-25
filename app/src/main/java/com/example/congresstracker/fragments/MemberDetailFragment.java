package com.example.congresstracker.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.congresstracker.R;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.models.MemberDataPull;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberDetailFragment extends Fragment {

    private static final String EXTRA_SELECTED_MEMBER = "EXTRA_SELECTED_MEMBER";

    private CongressMember selectedMember;
    private TextView nameTV;
    private TextView partyTV;
    private TextView stateTV;
    private TextView totalVotesTV;
    private TextView missedVotePctTV;
    private TextView voteWPartyTV;
    private TextView voteAPartyTV;

    private ProgressBar progressBar;

    MemberDetailListener listener;


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
    public void onDestroy() {
        super.onDestroy();
        listener.updateTitle();
    }
}
