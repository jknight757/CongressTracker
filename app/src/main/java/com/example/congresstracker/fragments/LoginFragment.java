package com.example.congresstracker.fragments;


import android.content.Context;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener, FirebaseAuth.AuthStateListener {

    public final String TAG = "LoginFragment.TAG";
    public LoginListener listener;

    private MaterialButton loginBtn;
    private MaterialButton signupBtn;
    private MaterialButton skipbtn;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    String email;
    String password;

    private FirebaseAuth mAuth;


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
            mAuth = FirebaseAuth.getInstance();
            loginBtn = getView().findViewById(R.id.login_btn);
            loginBtn.setOnClickListener(this);

            signupBtn = getView().findViewById(R.id.to_signup_btn);
            signupBtn.setOnClickListener(this);

            skipbtn = getView().findViewById(R.id.skip_btn);
            skipbtn.setOnClickListener(this);
            emailInput = getView().findViewById(R.id.email_textInput);
            passwordInput = getView().findViewById(R.id.password_textInput);

            mAuth.addAuthStateListener(this);

        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            listener.LoginClicked();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.login_btn:
                if(validateInput()){
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if(currentUser == null){
                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    listener.LoginClicked();
                                }else {

                                    Log.d(TAG, "onComplete: unsuccessful");
                                    try {
                                        throw task.getException();


                                    } catch (FirebaseAuthInvalidUserException invalidEmail)
                                    {
                                        Log.d(TAG, "onComplete: invalid_email");
                                    }
                                    // if user enters wrong password.
                                    catch (FirebaseAuthInvalidCredentialsException wrongPassword)
                                    {
                                        Log.d(TAG, "onComplete: wrong_password");

                                    }
                                    catch (Exception e)
                                    {
                                        Log.d(TAG, "onComplete: " + e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                }

                break;
            case R.id.to_signup_btn:
                listener.BackToSignup();
                break;
            case R.id.skip_btn:
                listener.SkipClickedTwo();
                break;
        }

    }

    public boolean validateInput(){

        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();

        if(!email.isEmpty()){
            if( !password.isEmpty()){
                return true;
            }else{
                Toast.makeText(getContext(),"Enter Password", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext(),"Enter Email", Toast.LENGTH_SHORT).show();

        }

        return false;
    }
}
