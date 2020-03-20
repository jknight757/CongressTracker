package com.example.congresstracker.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.congresstracker.other.NetworkUtils;
import com.example.congresstracker.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class UserDataPull extends IntentService {

    public static final String TAG = "UserDataPull.TAG";

    public static final String ACTION_PULL_PROFILE = "com.example.congresstracker.models.action.PULL_PROFILE";
    public static final String ACTION_SEND_PROFILE = "com.example.congresstracker.models.action.SEND_PROFILE";

    public static final String EXTRA_USER = "EXTRA_USER";
    public static final String EXTRA_IMAGE = "EXTRA_IMAGE";


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore fireStoreDB;
    private String Uid;
    private StorageReference mStorageRef;

    public UserDataPull() {
        super("UserDataPull");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.i(TAG, "onHandleIntent:  User Data Pull");
        if(intent != null){
            switch (intent.getAction()){
                case ACTION_PULL_PROFILE:
                    getUserInfo();
                    break;
            }
        }
    }

    public void getUserInfo(){

        if(NetworkUtils.isConnected(getBaseContext())){
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            Uid = currentUser.getUid();
            fireStoreDB = FirebaseFirestore.getInstance();

            DocumentReference firebaseUsers = fireStoreDB.collection("Users").document(Uid);
            firebaseUsers.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if(documentSnapshot.exists()){
                        String email = documentSnapshot.getString("email");
                        boolean hasImg = documentSnapshot.getBoolean("hasProfImg");
                        String name = documentSnapshot.getString("name");
                        String password = documentSnapshot.getString("password");
                        String state = documentSnapshot.getString("state");
                        User me = new User(name,email,password);
                        me.setHasProfImg(hasImg);
                        me.setState(state);
                        String party = "";
                        if(documentSnapshot.contains("party")){
                            party = documentSnapshot.getString("party");
                            me.setParty(party);
                        }

                        String zip = "";
                        if(documentSnapshot.contains("zip")){
                            zip = documentSnapshot.getString("zip");
                            me.setZip(zip);
                        }

                        broadCastResults(me);




                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: User not found");
                    e.printStackTrace();
                }
            });


        }
    }



    public void getProfilePic(final User me){

        mStorageRef = FirebaseStorage.getInstance().getReference("profileImgs");
        StorageReference storageReference = mStorageRef.child("/" + Uid + ".jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                broadCastWithImage(me,bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "getProfilePic: ERROR");
                e.printStackTrace();
            }
        });


    }

    public void broadCastWithImage(User me, byte[] img){
        if(me.hasProfImg){
            Intent broadcastIntent;
            broadcastIntent = new Intent(ACTION_SEND_PROFILE);
            broadcastIntent.putExtra(EXTRA_USER,me);
            broadcastIntent.putExtra(EXTRA_IMAGE,img);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }else {
            Log.i(TAG, "broadCastWithImage: ERROR");
        }
    }

    public void broadCastResults(User me){

        if(me.hasProfImg){
            getProfilePic(me);
        }else {
            Intent broadcastIntent;
            broadcastIntent = new Intent(ACTION_SEND_PROFILE);
            broadcastIntent.putExtra(EXTRA_USER,me);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }
    }
}
