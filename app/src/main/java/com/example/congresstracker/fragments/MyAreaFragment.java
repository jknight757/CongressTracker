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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.activities.BillActivity;
import com.example.congresstracker.activities.CongressActivity;
import com.example.congresstracker.activities.MainActivity;
import com.example.congresstracker.other.NetworkUtils;
import com.example.congresstracker.models.User;
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

    private FirebaseAuth mAuth;
    FirebaseUser user;
    private FirebaseFirestore fireStoreDB;
    User thisUser;

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
        if(NetworkUtils.isConnected(getContext())){

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            View view = getLayoutInflater().inflate(R.layout.edit_profile_alert, null);

            final TextInputEditText nameInput = view.findViewById(R.id.name_textInput);
            final TextInputEditText emailInput = view.findViewById(R.id.email_textInput);
            final TextInputEditText passwordInput = view.findViewById(R.id.password_textInput);
            final TextInputEditText partyInput = view.findViewById(R.id.party_textInput);
            final TextInputEditText zipInput = view.findViewById(R.id.zip_textInput);

            builder.setTitle("Edit Profile");

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAuth = FirebaseAuth.getInstance();
                    user = mAuth.getCurrentUser();
                    String userID = user.getUid();
                    fireStoreDB = FirebaseFirestore.getInstance();

                    String name = nameInput.getText().toString();
                    final String email = emailInput.getText().toString();
                    final String password = passwordInput.getText().toString();
                    String party = partyInput.getText().toString();
                    String zip = zipInput.getText().toString();
                    boolean emailChanged = false;
                    boolean passwordChanged = false;
                    String oldEmail = thisUser.getEmail();
                    String oldPassword = thisUser.getPassword();


                    if(!name.isEmpty()){

                        thisUser.setName(name);

                    }
                    if(!email.isEmpty()){

                        thisUser.setEmail(email);
                        emailChanged = true;


                    }
                    if(!password.isEmpty()){
                        thisUser.setPassword(password);
                        passwordChanged = true;

                    }
                    if(!party.isEmpty()){
                        thisUser.setParty(party);

                    }
                    if(!zip.isEmpty()){
                        thisUser.setZip(zip);

                    }

                    if(name.isEmpty() && email.isEmpty() && password.isEmpty() && party.isEmpty() && zip.isEmpty()){
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


                        if(emailChanged && passwordChanged){
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


                        }else {
                            if(emailChanged){
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

                            if(passwordChanged){
                                AuthCredential credential = EmailAuthProvider.getCredential(oldPassword, oldPassword);
                                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        FirebaseUser _user = FirebaseAuth.getInstance().getCurrentUser();
                                        _user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getContext(), "Password Updated", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }



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
