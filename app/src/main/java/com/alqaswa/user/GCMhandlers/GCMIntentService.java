package com.alqaswa.user.GCMhandlers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.alqaswa.user.ChatActivity;
import com.alqaswa.user.MainActivity;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by user on 6/29/2015.
 */
public class GCMIntentService extends GcmListenerService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private PreferenceHelper preferenceHelper;
    private String date = "";


    @Override
    public void onMessageReceived(final String from, final Bundle bundle) {

        Bundle extras = bundle;
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        preferenceHelper = new PreferenceHelper(this);

        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        // String messageType = gcm.getMessageType(intent);


        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */


            String recieved_message = extras.getString("message");
            Log.e("mahi", "rec push" + recieved_message);
            try {
                JSONObject responsobj = new JSONObject(recieved_message);
                /*JSONObject dataobj =responsobj.getJSONObject(recieved_message);
                JSONObject msgObj = dataobj.getJSONObject("message");*/
                String title = responsobj.getString("title");
                String type = responsobj.optString("type");
                Intent pushIntent = new Intent(Const.REQUEST_ACCEPT);
                pushIntent.putExtra(Const.REQUEST_ACCEPT, recieved_message);

                if (preferenceHelper.getUserId() != null) {
                    sendNotification(title,type);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushIntent);
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }




        }
    }

    private void sendNotification(String msg,String type) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent chat_intent = null;
        if(type.equals("2")){
            chat_intent = new Intent(this, ChatActivity.class);
            chat_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            chat_intent.putExtra("newques", "new_quest");
        } else {
            chat_intent = new Intent(this, MainActivity.class);
            chat_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            chat_intent.putExtra("newques", "new_quest");
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),
                chat_intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.e("mahi", "rec push o" + msg);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(String.valueOf(NOTIFICATION_ID));
            if (mChannel == null) {
                mChannel = new NotificationChannel(String.valueOf(NOTIFICATION_ID), getResources().getString(R.string.app_name), importance);
                mChannel.setDescription(msg);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 500});
                mNotificationManager.createNotificationChannel(mChannel);
            }
            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this, String.valueOf(NOTIFICATION_ID))
                    .setContentTitle(msg)  // required
                    .setSmallIcon(R.mipmap.ic_launcher) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setTicker(msg)
                    .setVibrate(new long[]{100, 500});
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }else {

            Log.e("mahi", "rec push " + msg);
            Notification.Builder mBuilder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setStyle(new Notification.BigTextStyle().bigText(msg))
                    .setVibrate(new long[]{100, 500})
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setAutoCancel(true)
                    .setContentText(msg);
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }

    }


}
