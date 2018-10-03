package com.alqaswa.user.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.MainActivity;

/**
 * Created by user on 1/5/2017.
 */

public abstract class BaseMapFragment extends Fragment implements
        View.OnClickListener, AsyncTaskCompleteListener {
    MainActivity activity;
    public static LatLng pic_latlan;
    public static LatLng drop_latlan;
    public static boolean searching =false;
    public static String s_address,d_address="";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();


    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

    }

    @Override
    public void onClick(View v) {


    }

    @Nullable
    public abstract View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);
}
