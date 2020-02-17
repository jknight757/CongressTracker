package com.example.congresstracker.fragments;


import android.content.Context;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.congresstracker.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    public final String TAG = "LoginFragment.TAG";
    public LoginListener listener;

    private com.google.android.material.button.MaterialButton loginBtn;
    private com.google.android.material.button.MaterialButton signupBtn;
    private com.google.android.material.button.MaterialButton skipbtn;


    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {

        Bundle args = new Bundle();

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public interface LoginListener{
        void LoginClicked();
        void BackToSignup();
        void SkipClickedTwo();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof LoginListener){
            listener = (LoginListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){

            loginBtn = getView().findViewById(R.id.login_btn);
            loginBtn.setOnClickListener(this);

            signupBtn = getView().findViewById(R.id.to_signup_btn);
            signupBtn.setOnClickListener(this);

            skipbtn = getView().findViewById(R.id.skip_btn);
            skipbtn.setOnClickListener(this);

        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.login_btn:
                listener.LoginClicked();
                break;
            case R.id.to_signup_btn:
                listener.BackToSignup();
                break;
            case R.id.skip_btn:
                listener.SkipClickedTwo();
                break;
        }

    }
}
