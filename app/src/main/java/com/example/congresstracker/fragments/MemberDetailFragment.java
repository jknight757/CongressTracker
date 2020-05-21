package com.example.congresstracker.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.models.Bill;
import com.example.congresstracker.models.BillVote;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.other.BillAdapter;
import com.example.congresstracker.services.MemberDataPull;
import com.example.congresstracker.models.Term;
import com.example.congresstracker.other.VoteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemberDetailFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = "MemberDetail.TAG";

    private static final String EXTRA_SELECTED_MEMBER = "EXTRA_SELECTED_MEMBER";
    public static final String EXTRA_MEMBER_VOTES = "EXTRA_MEMBER_VOTES";
    public static final String EXTRA_MEMBER_IMAGE = "EXTRA_MEMBER_IMAGE";
    public static final String EXTRA_SELECT_BILL = "EXTRA_SELECT_BILL";

    private int selectedSubTab = 0;

    public static final int VOTE_HISTORY_TAB = 0;
    public static final int OTHER_INFO_TAB = 1;

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
    private ImageView profImage;
    private TextView voteHistoryBtn;
    private TextView otherInfoBtn;
    private View voteBtnSelect;
    private View otherBtnSelect;
    private View otherInfoView;
    private TextView seniorityTV;
    private TextView committeesTV;
    private TextView nextElectionTV;
    private ListView committeeLV;
    private TextView sponsoredBillsBtn;

    Bitmap memImage;

    private ProgressBar progressBar;
    BottomNavigationView bottomNav;

    MemberDetailListener listener;


    private final MemberVotesReceiver receiver = new MemberVotesReceiver();


    public MemberDetailFragment() {
        // Required empty public constructor
    }

    public static MemberDetailFragment newInstance(CongressMember member, Bitmap img) {

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SELECTED_MEMBER,member);
        args.putParcelable(EXTRA_MEMBER_IMAGE, img);
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MemberDetailListener){
            listener = (MemberDetailListener) context;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.info_menu, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null && getArguments() != null){

            selectedMember = (CongressMember) getArguments().getSerializable(EXTRA_SELECTED_MEMBER);
            memImage = getArguments().getParcelable(EXTRA_MEMBER_IMAGE);
            nameTV = getView().findViewById(R.id.name_txt_lbl);
            partyTV = getView().findViewById(R.id.party_txt_lbl);
            stateTV = getView().findViewById(R.id.state_txt_lbl);
            totalVotesTV = getView().findViewById(R.id.total_votes);
            missedVotePctTV = getView().findViewById(R.id.missed_votes_pct);
            voteWPartyTV = getView().findViewById(R.id.vote_wparty_pct);
            voteAPartyTV = getView().findViewById(R.id.vote_aparty_pct);
            progressBar = getView().findViewById(R.id.loadin_votes_pb);
            voteListView = getView().findViewById(android.R.id.list);
            voteListView.setOnItemClickListener(this);
            profImage = getView().findViewById(R.id.prof_img);
            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setOnNavigationItemSelectedListener(this);

            voteHistoryBtn = getView().findViewById(R.id.vote_history_btn);
            voteHistoryBtn.setOnClickListener(this);
            otherInfoBtn = getView().findViewById(R.id.other_info_btn);
            otherInfoBtn.setOnClickListener(this);

            voteBtnSelect = getView().findViewById(R.id.vote_his_select);
            otherBtnSelect = getView().findViewById(R.id.other_info_select);

            otherInfoView = getView().findViewById(R.id.other_info_view);
            seniorityTV = getView().findViewById(R.id.seniority_txt);
            committeesTV = getView().findViewById(R.id.committees_txt);
            committeesTV.setOnClickListener(this);

            sponsoredBillsBtn = getView().findViewById(R.id.sponsored_bills_txt);
            sponsoredBillsBtn.setOnClickListener(this);

            nextElectionTV = getView().findViewById(R.id.next_election_txt);
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
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.vote_history_btn:

                if(selectedSubTab == OTHER_INFO_TAB){
                    selectedSubTab = VOTE_HISTORY_TAB;
                    voteListView.setVisibility(View.VISIBLE);
                    otherInfoView.setVisibility(View.GONE);

                    voteBtnSelect.setVisibility(View.INVISIBLE);
                    otherBtnSelect.setVisibility(View.VISIBLE);


                }
                break;
            case R.id.other_info_btn:

                if(selectedSubTab == VOTE_HISTORY_TAB){
                    selectedSubTab = OTHER_INFO_TAB;

                    voteListView.setVisibility(View.GONE);
                    otherInfoView.setVisibility(View.VISIBLE);

                    voteBtnSelect.setVisibility(View.VISIBLE);
                    otherBtnSelect.setVisibility(View.INVISIBLE);
                    committeesTV.setOnClickListener(this);


                    String seniority =  "Seniority\n";
                    if(selectedMember.getSeniorityFromTerms() == 1){
                        seniority +=  selectedMember.getSeniorityFromTerms()+ " Year";
                    }else {
                        seniority += selectedMember.getSeniorityFromTerms()+ " Years";
                    }


                    String nextElection = "Next Election\n";
                    nextElection += selectedMember.getNextElection();




                    seniorityTV.setText(seniority);
                    nextElectionTV.setText(nextElection);


                }
                break;
            case R.id.committees_txt:
                if(selectedSubTab == OTHER_INFO_TAB){
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());

                    View view = getLayoutInflater().inflate(R.layout.committee_popup, null);
                    ListView lv = view.findViewById(R.id.committee_listview_pu);
                    ImageButton closeBtn = view.findViewById(R.id.popup_close_btn);

                    ArrayList<Term> terms = selectedMember.getTerms();
                    ArrayList<String> committees = terms.get(0).getCommittees();

                    if(lv != null && committees != null){
                        ArrayAdapter<String> comAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, committees);
                        lv.setAdapter(comAdapter);
                    }
                    builder.setView(view);

                    final android.app.AlertDialog alertDialog = builder.create();

                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });


                    alertDialog.show();


                }
                break;

            case R.id.sponsored_bills_txt:
                if(selectedSubTab == OTHER_INFO_TAB){
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());

                    View view = getLayoutInflater().inflate(R.layout.sponsored_bills_popup, null);
                    ListView lv = view.findViewById(R.id.sponsored_listview_pu);
                    ImageButton closeBtn = view.findViewById(R.id.popup_close_btn);

                    final ArrayList<Bill> bills = selectedMember.getSponsoredBills();

                    if(lv != null && bills != null){

                        BillAdapter adapter = new BillAdapter(getContext(), bills);
                        lv.setAdapter(adapter);

                    }

                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            String billUri = bills.get(position).getBillUri();
                            Intent billIntent = new Intent(getContext(), BillActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            billIntent.putExtra(EXTRA_SELECT_BILL,billUri);
                            startActivity(billIntent);
                        }
                    });
                    builder.setView(view);

                    final android.app.AlertDialog alertDialog = builder.create();

                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });


                    alertDialog.show();


                }

                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        String billUri = memberVotes.get(position).getUri();
        String billID = memberVotes.get(position).getId();
        if(billID.contains("PN")){
            Toast.makeText(getContext(), "No Nomination Details to Show", Toast.LENGTH_SHORT).show();

        }else {
            Intent billIntent = new Intent(getContext(), BillActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            billIntent.putExtra(EXTRA_SELECT_BILL,billUri);
            startActivity(billIntent);

        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.app_info_btn){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = getLayoutInflater().inflate(R.layout.info_window, null);
            builder.setView(view);
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();


        }

        return super.onOptionsItemSelected(item);

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

        }
        return false;
    }

    public void updateUI()
    {
        profImage.setImageBitmap(memImage);
        nameTV.setText(selectedMember.getName());
        String party = selectedMember.getChamber() + ", " + selectedMember.getParty();
        partyTV.setText(party);
        stateTV.setText(selectedMember.getUnabreviated());
        String totalVStr = selectedMember.getTotalVotes()+ "";
        totalVotesTV.setText(totalVStr);
        String missVoteStr = selectedMember.getMissedVotePctAverage() + "%";
        missedVotePctTV.setText(missVoteStr);
        String voteWPStr = selectedMember.getVoteWPPctAverage() + "%";
        voteWPartyTV.setText(voteWPStr);
        String voteAPStr = selectedMember.getVoteAPPctAverage() + "%";
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
