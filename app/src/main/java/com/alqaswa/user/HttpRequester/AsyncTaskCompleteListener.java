package com.alqaswa.user.HttpRequester;

/**
 * Created by Amal on 28-06-2015.
 */
public interface AsyncTaskCompleteListener {

    void onTaskCompleted(String response, int serviceCode);
}
