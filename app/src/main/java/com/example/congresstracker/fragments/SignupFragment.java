package com.example.congresstracker.fragments;


import android.content.Context;
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
public class SignupFragment extends Fragment implements View.OnClickListener {

    public final String TAG = "SignupFragment.TAG";
    public SignupListener listener;

    private com.google.android.material.button.MaterialButton loginBtn;
    private com.google.android.material.button.MaterialButton signupBtn;
    private com.google.android.material.button.MaterialButton skipbtn;

    public SignupFragment() {
        // Required empty public constructor
    }

    public static SignupFragment newInstance() {

        Bundle args = new Bundle();

        SignupFragment fragment = new SignupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface SignupListener{
        void SignUpClicked();
        void BackToLogin();
        void SkipClicked();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof SignupListener){
            listener = (SignupListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){
            loginBtn = getView().findViewById(R.id.to_login_btn);
            loginBtn.setOnClickListener(this);

            signupBtn = getView().findViewById(R.id.signup_btn);
            signupBtn.setOnClickListener(this);

            skipbtn = getView().findViewById(R.id.skip_btn);
            skipbtn.setOnClickListener(this);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.to_login_btn:
                listener.BackToLogin();
                break;
            case R.id.signup_btn:
                listener.SignUpClicked();
                break;
            case R.id.skip_btn:
                listener.SkipClicked();
                break;
        }

    }
}
