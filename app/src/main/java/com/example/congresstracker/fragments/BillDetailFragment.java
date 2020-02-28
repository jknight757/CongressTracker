package com.example.congresstracker.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.activities.MyAreaActivity;
import com.example.congresstracker.models.Bill;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillDetailFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener{

    public static final String TAG = "BillDetail.TAG";
    public static final String EXTRA_BILL = "EXTRA_BILL";

    private TextView billNameTV;
    private TextView sponsorTV;
    private TextView statusTV;

    private Bill selectedBill;

    private BottomNavigationView bottomNav;
    private BillDetailListener listener;

    public BillDetailFragment() {
        // Required empty public constructor
    }

    public static BillDetailFragment newInstance(Bill bill) {

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_BILL,bill);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bill_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView()!= null && getArguments() != null){
            billNameTV = getView().findViewById(R.id.bill_txt_lbl);
            sponsorTV = getView().findViewById(R.id.sponsor_txt_lbl);
            statusTV = getView().findViewById(R.id.status_txt_lbl);
            bottomNav = getView().findViewById(R.id.bottom_tab_bar);
            bottomNav.setSelectedItemId(R.id.bill_tab_item);
            bottomNav.setOnNavigationItemSelectedListener(this);
            selectedBill = (Bill) getArguments().getSerializable(EXTRA_BILL);


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
            }


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.updateTitle();
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
}
