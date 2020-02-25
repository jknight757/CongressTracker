package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.fragments.CongressFragment;
import com.example.congresstracker.fragments.LoginFragment;
import com.example.congresstracker.fragments.SignupFragment;

public class MainActivity extends AppCompatActivity implements SignupFragment.SignupListener, LoginFragment.LoginListener {

    LoginFragment loginFragment;
    SignupFragment signupFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("");
        }

        loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.launch_fragment_container,loginFragment,loginFragment.TAG).commit();
    }



    // Sign up Callback methods//

    @Override
    public void SignUpClicked() {
        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);
    }

    @Override
    public void BackToLogin() {

        loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launch_fragment_container,loginFragment,loginFragment.TAG).commit();

    }

    @Override
    public void SkipClicked() {
        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);

    }
    // Sign up Callback methods//

    // Login Callback methods//

    @Override
    public void LoginClicked() {
        Toast.makeText(this,"onComplete: login Successful", Toast.LENGTH_SHORT).show();

        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);
    }

    @Override
    public void BackToSignup() {

        signupFragment = SignupFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launch_fragment_container,signupFragment, signupFragment.TAG).commit();

    }

    @Override
    public void SkipClickedTwo() {
        Intent congressIntent = new Intent(this,CongressActivity.class);
        startActivity(congressIntent);

    }
    // Login Callback methods//



}
