package com.example.congresstracker.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;

import com.example.congresstracker.R;
import com.example.congresstracker.fragments.LoginFragment;
import com.example.congresstracker.fragments.SignupFragment;
import com.example.congresstracker.other.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements SignupFragment.SignupListener, LoginFragment.LoginListener {

    LoginFragment loginFragment;
    SignupFragment signupFragment;
    private FirebaseAuth mAuth;
    public static boolean validUser = false;
    private int currentFragment;
    private final int LOGIN_FRAG = 2;
    private final int SIGNUP_FRAG = 4;

    private LocalNetworkChangeReceiver receiver = new LocalNetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gold comment

        if(NetworkUtils.isConnected(this)) {

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("");
                }

                loginFragment = LoginFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.launch_fragment_container, loginFragment, loginFragment.TAG).commit();
                currentFragment = LOGIN_FRAG;
            } else {
                validUser = true;
                Intent congressIntent = new Intent(this, CongressActivity.class);
                startActivity(congressIntent);
            }
        }else {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet")
                    .setMessage("Please connect to the internet if you would like to use this application")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dialog.cancel();
                        }
                    })
                    .show();
            findViewById(R.id.internet_alert_msg).setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    // Sign up Callback methods//

    @Override
    public void SignUpClicked() {
        validUser = true;
        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);
    }

    @Override
    public void BackToLogin() {

        loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launch_fragment_container,loginFragment,loginFragment.TAG).commit();
        currentFragment = LOGIN_FRAG;

    }

    @Override
    public void SkipClicked() {
        validUser = false;
        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);

    }
    // Sign up Callback methods//

    // Login Callback methods//

    @Override
    public void LoginClicked() {
        validUser = true;
        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);
    }

    @Override
    public void BackToSignup() {

        signupFragment = SignupFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launch_fragment_container,signupFragment, signupFragment.TAG).commit();
        currentFragment = SIGNUP_FRAG;

    }

    @Override
    public void SkipClickedTwo() {
        validUser = false;
        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);

    }
    // Login Callback methods//

    public class LocalNetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(NetworkUtils.isConnected(getApplicationContext())){
                if(currentFragment == LOGIN_FRAG){
                    updateUI();
                }
            }

        }
    }
    public void updateUI(){
        loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launch_fragment_container, loginFragment, loginFragment.TAG).commit();
        findViewById(R.id.internet_alert_msg).setVisibility(View.GONE);

    }
}
