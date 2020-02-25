package com.example.congresstracker.fragments;


import android.content.Context;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment implements View.OnClickListener {

    public final String TAG = "SignupFragment.TAG";
    public SignupListener listener;

    private MaterialButton loginBtn;
    private MaterialButton signupBtn;
    private MaterialButton skipbtn;

    private TextInputEditText emailInput;
    private TextInputEditText nameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    String email;
    String name;
    String password;
    String confirmPassword;
    private FirebaseAuth mAuth;
    FirebaseUser user;

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

            emailInput = getView().findViewById(R.id.email_textInput);
            nameInput = getView().findViewById(R.id.name_textInput);
            passwordInput = getView().findViewById(R.id.password_textInput);
            confirmPasswordInput = getView().findViewById(R.id.reenter_password_textInput);

            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            if(user != null){
                listener.SignUpClicked();
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.to_login_btn:
                listener.BackToLogin();
                break;
            case R.id.signup_btn:

                if(validateInput()){

                    user = mAuth.getCurrentUser();

                    if(user == null){
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(),"onComplete: SignUp Successful", Toast.LENGTH_SHORT).show();
                                    listener.SignUpClicked();
                                }else {
                                    Log.d(TAG, "onComplete: unsuccessful");
                                    try {
                                        throw task.getException();

                                    } catch (FirebaseAuthWeakPasswordException weakPassword)
                                    {
                                        Log.d(TAG, "onComplete: weak_password");

                                    }
                                    // if user enters wrong password.
                                    catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                                    {
                                        Log.d(TAG, "onComplete: malformed_email");

                                    }
                                    catch (FirebaseAuthUserCollisionException existEmail)
                                    {
                                        Log.d(TAG, "onComplete: exist_email");

                                    }
                                    catch (Exception e)
                                    {
                                        Log.d(TAG, "onComplete: " + e.getMessage());
                                    }
                                    Toast.makeText(getContext(),"SignUp Failed: " + task.getResult().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }


                break;
            case R.id.skip_btn:
                listener.SkipClicked();
                break;
        }

    }

    public boolean validateInput(){
        email = emailInput.getText().toString();
        name = nameInput.getText().toString();
        password = passwordInput.getText().toString();
        confirmPassword =confirmPasswordInput.getText().toString();


        if(!email.isEmpty() && !email.contains(" ")){
            if(!name.isEmpty()){
                if(!password.isEmpty() && !confirmPassword.isEmpty()){
                    if(password.equals(confirmPassword)){
                        if(password.length() > 6){
                            return true;
                        }else{
                            // password not long enough
                        }
                    }else{
                        //passwords dont match
                    }
                }else{
                    // passwords can't be empty
                }
            }else {
                // name can't be empty
            }
        }else {
            // email can't be empty nor contain spaces
        }


        return false;
    }
}
