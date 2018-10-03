package com.alqaswa.user.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.SignInActivity;

/**
 * Created by user on 8/29/2016.
 */
public class BaseRegFragment extends Fragment implements
        View.OnClickListener, AsyncTaskCompleteListener {
    SignInActivity activity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        activity = (SignInActivity) getActivity();


    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

    }

    @Override
    public void onClick(View v) {


    }
}
