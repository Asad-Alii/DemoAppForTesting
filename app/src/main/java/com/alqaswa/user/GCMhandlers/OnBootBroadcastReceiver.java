package com.alqaswa.user.GCMhandlers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 1/2/2017.
 */

public class OnBootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("mahi","on kill app received");
        Intent i = new Intent("com.doctor.ghealth.GCMhandlers.GCMRegisterHandler");
        i.setClass(context, GCMRegisterHandler.class);
        context.startService(i);
    }
}