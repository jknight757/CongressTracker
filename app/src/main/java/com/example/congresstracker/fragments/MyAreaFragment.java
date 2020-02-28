package com.example.congresstracker.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.activities.MyAreaActivity;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.models.MemberDataPull;
import com.example.congresstracker.models.User;
import com.example.congresstracker.models.UserDataPull;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAreaFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MyArea.TAG";
    public static final String EXTRA_USER = "EXTRA_USER";
    public static final String EXTRA_IMAGE = "EXTRA_IMAGE";

    private TextView nameTV;
    private TextView partyTV;
    private TextView stateTV;
    private ImageView profileImg;

    private BottomNavigationView bottomNavigation;

    private final UserDataReceiver receiver = new UserDataReceiver();


    public MyAreaFragment() {
        // Required empty public constructor
    }

    public static MyAreaFragment newInstance() {

        Bundle args = new Bundle();

        MyAreaFragment fragment = new MyAreaFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_area, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){
            nameTV = getView().findViewById(R.id.name_txt_lbl);
            partyTV = getView().findViewById(R.id.party_txt_lbl);
            stateTV = getView().findViewById(R.id.state_txt_lbl);
            profileImg = getView().findViewById(R.id.prof_img);

            bottomNavigation = getView().findViewById(R.id.bottom_tab_bar);
            bottomNavigation.setSelectedItemId(R.id.local_tab_item);
            bottomNavigation.setOnNavigationItemSelectedListener(this);

        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_dropdown_menu,menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_dropdown1:
                editProfile();
                break;
            case R.id.action_dropdown2:
                signOut();
                break;
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserDataPull.ACTION_SEND_PROFILE);
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
                Intent congressIntent = new Intent(getContext(), CongressActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(congressIntent);
                break;
            case R.id.bill_tab_item:
                Intent billIntent = new Intent(getContext(), BillActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(billIntent);
                break;
            case R.id.local_tab_item:
                break;
        }
        return false;
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent signoutIntent = new Intent(getContext(), MainActivity.class);
        startActivity(signoutIntent);

    }
    public void editProfile(){

    }



    class UserDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: ");

            if (intent.hasExtra(EXTRA_USER)) {
                User thisUser = (User) intent.getSerializableExtra(EXTRA_USER);
                byte[] image = null;

                if (thisUser.getHasProfImg()){
                     if(intent.hasExtra(EXTRA_IMAGE)){
                         image = intent.getByteArrayExtra(EXTRA_IMAGE);

                    }
                }
                updateUI(thisUser,image);


            }
        }
    }

    public void updateUI(User user, byte[] img){
        nameTV.setText(user.getName());

        if(img != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(img,0,img.length);
            profileImg.setImageBitmap(bitmap);
        }

        if(user.getParty() != null){
            partyTV.setText(user.getParty());
        }

        if(user.getZip() != null){
            stateTV.setText(user.getZip());
        }
    }
}
