package com.example.congresstracker.fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.congresstracker.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAreaFragment extends Fragment {

    public static final String TAG = "MyArea.TAG";

    private TextView nameTV;
    private TextView partyTV;
    private TextView stateTV;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_area, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}
