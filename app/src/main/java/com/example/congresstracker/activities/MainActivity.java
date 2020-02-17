package com.example.congresstracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.congresstracker.R;
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

    }

    @Override
    public void BackToLogin() {

        loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launch_fragment_container,loginFragment,loginFragment.TAG).commit();

    }

    @Override
    public void SkipClicked() {

    }
    // Sign up Callback methods//

    // Login Callback methods//

    @Override
    public void LoginClicked() {

    }

    @Override
    public void BackToSignup() {

        signupFragment = SignupFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launch_fragment_container,signupFragment, signupFragment.TAG).commit();

    }

    @Override
    public void SkipClickedTwo() {

    }
    // Login Callback methods//



}
