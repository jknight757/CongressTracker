package com.example.congresstracker.fragments;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.models.CongressMember;
import com.example.congresstracker.models.States;
import com.example.congresstracker.other.MemberAdapter;
import com.example.congresstracker.other.NetworkUtils;
import com.example.congresstracker.models.User;
import com.example.congresstracker.other.StateRepsAdapter;
import com.example.congresstracker.services.MemberDataPull;
import com.example.congresstracker.services.UserDataPull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAreaFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = "MyArea.TAG";
    public static final String EXTRA_USER = "EXTRA_USER";
    public static final String EXTRA_IMAGE = "EXTRA_IMAGE";
    public static final String EXTRA_MEMBER_ID = "EXTRA_MEMBER_ID";

    private TextView nameTV;
    private TextView partyTV;
    private TextView stateTV;
    private ImageView profileImg;
    private TextView setStateEmpty;
    private ListView localRepsLV;
    private ProgressBar loadingPB;

    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    FirebaseUser user;
    private FirebaseFirestore fireStoreDB;
    User thisUser;

    private ArrayList<CongressMember> myReps;

    private String[] stateList = new String[] {"Alaska","Alabama","Arkansas","Arizona","California","Colorado","Connecticut",
            "Delaware","Florida","Georgia","Hawaii","Iowa","Idaho", "Illinois","Indiana","Kansas",
            "Kentucky","Louisiana","Massachusetts","Maryland","Maine","Michigan", "Minnesota","Missouri","Mississippi",
            "Montana","North Carolina","North Dakota","Nebraska","New Hampshire", "New Jersey","New Mexico","Nevada",
            "New York", "Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota",
            "Tennessee","Texas","Utah", "Virginia","Vermont","Washington","Wisconsin","West Virginia","Wyoming"};


    private final UserDataReceiver receiver = new UserDataReceiver();

    private final StateRepReceiver repReceiver = new StateRepReceiver();
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
            setStateEmpty = getView().findViewById(R.id.empty_lv_text);
            setStateEmpty.setOnClickListener(this);

            localRepsLV = getView().findViewById(R.id.local_rep_listview);
            localRepsLV.setOnItemClickListener(this);

            loadingPB = getView().findViewById(R.id.loading_local_pb);
            loadingPB.setVisibility(View.VISIBLE);

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
            case R.id.action_dropdown3:
                changeLogin();
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

        IntentFilter repFilter = new IntentFilter();
        repFilter.addAction(MemberDataPull.ACTION_SEND_STATE_REPS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(repReceiver,repFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        //getContext().unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(repReceiver);
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
    public void changeLogin(){
        if(NetworkUtils.isConnected(getContext())) {


            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = getLayoutInflater().inflate(R.layout.change_login_alert, null);

            final TextInputEditText emailInput = view.findViewById(R.id.email_textInput);
            final TextInputEditText passwordInput = view.findViewById(R.id.password_textInput);
            final String oldEmail = thisUser.getEmail();
            final String oldPassword = thisUser.getPassword();
            emailInput.setText(oldEmail);
            passwordInput.setText(oldPassword);

            builder.setView(view);
            final android.app.AlertDialog alertDialog = builder.create();



            view.findViewById(R.id.save_changes_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean emailChanged = false;
                    boolean passwordChanged = false;

                mAuth = FirebaseAuth.getInstance();
                user = mAuth.getCurrentUser();
                String userID = user.getUid();
                fireStoreDB = FirebaseFirestore.getInstance();


                final String email = emailInput.getText().toString();
                final String password = passwordInput.getText().toString();





                if (!email.isEmpty() && !email.equals(oldEmail)) {

                    thisUser.setEmail(email);
                    emailChanged = true;


                }
                if (!password.isEmpty() && !password.equals(oldPassword)) {
                    thisUser.setPassword(password);
                    passwordChanged = true;

                }


                if (emailChanged && passwordChanged) {
                    AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, oldPassword);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseUser _user = FirebaseAuth.getInstance().getCurrentUser();
                            _user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getContext(), "Account Updated", Toast.LENGTH_SHORT).show();
                                }
                            });

                            _user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });


                } else {
                    if (emailChanged) {
                        AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, oldPassword);
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseUser _user = FirebaseAuth.getInstance().getCurrentUser();
                                _user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext(), "Email Updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }

                    if (passwordChanged) {
                        AuthCredential credential = EmailAuthProvider.getCredential(oldPassword, oldPassword);
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseUser _user = FirebaseAuth.getInstance().getCurrentUser();
                                _user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Password Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }


                }
                }
            });

            view.findViewById(R.id.cancel_changes_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });


            alertDialog.show();

        }
    }
    public void editProfile(){
        if(NetworkUtils.isConnected(getContext())){

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            View view = getLayoutInflater().inflate(R.layout.edit_profile_alert, null);

            final TextInputEditText nameInput = view.findViewById(R.id.name_textInput);
            final TextInputEditText partyInput = view.findViewById(R.id.party_textInput);
            final TextInputEditText zipInput = view.findViewById(R.id.zip_textInput);
            final Spinner stateSpinner = view.findViewById(R.id.state_spinner);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1, stateList);
            stateSpinner.setAdapter(adapter);

            if(thisUser.getState() != null){
                int position = 0;
                for (int i = 0; i < stateList.length; i++) {
                    if(thisUser.getState().equals(stateList[i])){
                        position = i;
                    }
                }
                stateSpinner.setSelection(position);
            }

            stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    thisUser.setState(stateList[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            builder.setTitle("Edit Profile");

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAuth = FirebaseAuth.getInstance();
                    user = mAuth.getCurrentUser();
                    String userID = user.getUid();
                    fireStoreDB = FirebaseFirestore.getInstance();

                    String name = nameInput.getText().toString();
                    String party = partyInput.getText().toString();
                    String zip = zipInput.getText().toString();
                    String state = (String)stateSpinner.getSelectedItem();



                    if(!name.isEmpty()){

                        thisUser.setName(name);

                    }
                    if(!party.isEmpty()){
                        thisUser.setParty(party);

                    }
                    if(!zip.isEmpty()){
                        thisUser.setZip(zip);

                    }

                    if(name.isEmpty() && party.isEmpty() && zip.isEmpty() && thisUser.getState() == null){
                        Toast.makeText(getContext(), "No Changes made!", Toast.LENGTH_SHORT).show();
                    }else {

                        DocumentReference firebaseUsers = fireStoreDB.collection("Users").document(userID);

                        firebaseUsers.set(thisUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "Info Stored ");

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i(TAG, "info not stored ");
                                    }
                                });

                        Intent pullDataIntent = new Intent(getContext(), UserDataPull.class);
                        pullDataIntent.setAction(UserDataPull.ACTION_PULL_PROFILE);
                        getContext().startService(pullDataIntent);

                    }

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setView(view);
            builder.show();


        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.empty_lv_text){
            editProfile();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String memberId = myReps.get(position).getId();

        Intent memberDetailIntent = new Intent(getContext(), CongressActivity.class);
        memberDetailIntent.putExtra(EXTRA_MEMBER_ID,memberId);
        startActivity(memberDetailIntent);

    }


    class UserDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: ");

            if (intent.hasExtra(EXTRA_USER)) {
                thisUser = (User) intent.getSerializableExtra(EXTRA_USER);
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
        loadingPB.setVisibility(View.GONE);
        nameTV.setText(user.getName());

        if(img != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(img,0,img.length);
            profileImg.setImageBitmap(bitmap);
        }

        if(user.getParty() != null){
            partyTV.setText(user.getParty());
        }
        String state =user.getState();
        if(state != null){
            stateTV.setText(state);
            // pull users state reps

            loadingPB.setVisibility(View.VISIBLE);



                String abrv = States.getAbreviation(state);
                Toast.makeText(getContext(), "" + abrv, Toast.LENGTH_SHORT).show();

                Intent pullDataIntent = new Intent(getContext(), MemberDataPull.class);
                pullDataIntent.setAction(MemberDataPull.ACTION_PULL_STATE);
                pullDataIntent.putExtra(MemberDataPull.EXTRA_USER_STATE, abrv);
                getContext().startService(pullDataIntent);



        }else {
            if(user.getZip() != null){
                stateTV.setText(user.getZip());
            }

            setStateEmpty.setVisibility(View.VISIBLE);

        }

    }

    class StateRepReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: State Reps");
            if(intent.hasExtra(MemberDataPull.EXTRA_STATE_REPS)){
                myReps = (ArrayList<CongressMember>) intent.getSerializableExtra(MemberDataPull.EXTRA_STATE_REPS);
                updateListView();
                loadingPB.setVisibility(View.GONE);

            }
        }

        public void updateListView(){

            if(myReps != null){
                StateRepsAdapter adapter = new StateRepsAdapter(getContext(), myReps);
                localRepsLV.setAdapter(adapter);
            }

        }
    }
}
