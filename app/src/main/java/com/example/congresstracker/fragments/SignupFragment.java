package com.example.congresstracker.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.congresstracker.R;
import com.example.congresstracker.models.NetworkUtils;
import com.example.congresstracker.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment implements View.OnClickListener, FirebaseAuth.AuthStateListener {

    public final String TAG = "SignupFragment.TAG";
    public SignupListener listener;

    private MaterialButton loginBtn;
    private MaterialButton signupBtn;
    private MaterialButton skipbtn;
    private ImageButton getProf;

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
    private FirebaseFirestore fireStoreDB;
    private StorageReference mStorageRef;

    private static final int REQUEST_IMAGE = 0x0010;
    private static File folderPath;
    private String imgPath;
    private static final String FOLDER_NAME = "profileImage";

    private byte[] profileImg;
    private boolean hasImage = false;

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

            getProf = getView().findViewById(R.id.profile_img_btn);
            getProf.setOnClickListener(this);

            emailInput = getView().findViewById(R.id.email_textInput);
            nameInput = getView().findViewById(R.id.name_textInput);
            passwordInput = getView().findViewById(R.id.password_textInput);
            confirmPasswordInput = getView().findViewById(R.id.reenter_password_textInput);


            mAuth = FirebaseAuth.getInstance();
            mAuth.addAuthStateListener(this);
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

                String userID;

                if(validateInput()){
                    if(NetworkUtils.isConnected(getContext())) {


                        user = mAuth.getCurrentUser();

                        if (user == null) {
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "onComplete: SignUp Successful", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Log.d(TAG, "onComplete: unsuccessful");
                                        try {
                                            throw task.getException();

                                        } catch (FirebaseAuthWeakPasswordException weakPassword) {
                                            Log.d(TAG, "onComplete: weak_password");

                                        }
                                        // if user enters wrong password.
                                        catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                            Log.d(TAG, "onComplete: malformed_email");

                                        } catch (FirebaseAuthUserCollisionException existEmail) {
                                            Log.d(TAG, "onComplete: exist_email");

                                        } catch (Exception e) {
                                            Log.d(TAG, "onComplete: " + e.getMessage());
                                        }
                                        Toast.makeText(getContext(), "SignUp Failed: " + task.getResult().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            Log.i(TAG, "signUp Success Has Image?  " + hasImage);

                        }
                    }
                }


                break;
            case R.id.skip_btn:
                listener.SkipClicked();
                break;

            case R.id.profile_img_btn:
                getGalleryImage();
                break;
        }

    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        String userID;

        if(user != null){

            Log.i(TAG, "signUp Success user not null ");
            user = mAuth.getCurrentUser();
            userID = user.getUid();
            fireStoreDB = FirebaseFirestore.getInstance();

            // get firebase image storage folder
            mStorageRef = FirebaseStorage.getInstance().getReference("profileImgs");
            StorageReference storageReference = mStorageRef.child("/" + userID + ".jpg");

            // set firebase user info storage location
            DocumentReference firebaseUsers = fireStoreDB.collection("Users").document(userID);

            User thisUser = new User(name,email,password);
            thisUser.setHasProfImg(hasImage);

            // save user info to firebase
            firebaseUsers.set(thisUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "User Info Saved", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Info Stored ");

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "info not stored ");
                            Toast.makeText(getContext(), "User Info Not Saved", Toast.LENGTH_SHORT).show();
                        }
                    });

            // save user profile image to firebase storage
            if(hasImage) {
                Log.i(TAG, "signUp has Image ");

                Uri imageUri = Uri.parse(imgPath);
                storageReference.putBytes(profileImg).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                        Log.i("Add", "onSuccess: "+ downloadUrl.getPath());
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Add", "onFailure: ");
                                Log.i("Add", "onFailure: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });

                listener.SignUpClicked();
                Log.i(TAG, "Sign up valid with Image");

            }else {
                listener.SignUpClicked();
                Log.i(TAG, "Sign up valid without Image");
            }

        }else {
            Log.i(TAG, "signUp Success user null ");
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

    public void getGalleryImage(){
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE){
            getProf.setImageURI(data.getData());
            imgPath = data.getData().toString();
            byte[] byteArray = null;
            try{
                InputStream stream = getContext().getContentResolver().openInputStream(Uri.parse(imgPath));
                byteArray= getBytes(stream);
            }catch (IOException e){
                Log.i(TAG, "onActivityResult: Image Not Converted to Byte[]");
                e.printStackTrace();
            }

            if(byteArray != null){
                profileImg = byteArray;
                hasImage = true;
            }



        }

    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
