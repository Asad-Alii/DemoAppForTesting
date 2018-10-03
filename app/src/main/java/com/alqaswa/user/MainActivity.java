package com.alqaswa.user;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alqaswa.user.Fragment.Home_Map_Fragment;
import com.alqaswa.user.Fragment.RatingFragment;
import com.alqaswa.user.Fragment.Travel_Map_Fragment;
import com.alqaswa.user.GCMhandlers.GCMRegisterHandler;
import com.alqaswa.user.GCMhandlers.GcmBroadcastReceiver;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Models.RequestDetail;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.ParseContent;
import com.alqaswa.user.Utils.PreferenceHelper;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    public String currentFragment = "";
    private Toolbar mainToolbar;
    private Bundle mbundle;
    private ParseContent pcontent;
    private AlertDialog gpsAlertDialog, internetDialog;
    private boolean isGpsDialogShowing = false, isRecieverRegistered = false, isNetDialogShowing = false;
    private boolean gpswindowshowing = false;
    AlertDialog.Builder gpsBuilder;
    private LocationManager manager;
    private ImageButton bnt_menu;
    private Dialog load_dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (!TextUtils.isEmpty(new PreferenceHelper(this).getLanguage())) {
            Locale myLocale = null;
            switch (new PreferenceHelper(this).getLanguage()) {
                case "":
                    myLocale = new Locale("en");
                    break;
                case "en":
                    myLocale = new Locale("en");

                    break;
                case "fr":
                    myLocale = new Locale("fr");
                    break;

            }


            Locale.setDefault(myLocale);
            Configuration config = new Configuration();
            config.locale = myLocale;
            this.getResources().updateConfiguration(config,
                    this.getResources().getDisplayMetrics());
        }
        setContentView(R.layout.activity_main);
        bnt_menu = (ImageButton) findViewById(R.id.bnt_menu);
        bnt_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        pcontent = new ParseContent(this);
        mainToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle(null);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        initDrawer();
        mbundle = savedInstanceState;
        if (TextUtils.isEmpty(new PreferenceHelper(this).getDeviceToken())) {

            registerGcmReceiver(new GcmBroadcastReceiver());

        }
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return;
            } else {
                //Log.d("mahesh","coming check1");
                checkreqstatus();
            }
        } else {
            //Log.d("mahesh","coming check2");
            checkreqstatus();
        }

    }


    public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
        if (mHandleMessageReceiver != null) {
            new GCMRegisterHandler(this, mHandleMessageReceiver);
        }
    }


    private void initDrawer() {
        // TODO Auto-generated method stub
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                mainToolbar, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {

                drawerToggle.syncState();
            }
        });


    }

    public void closeDrawer() {
        drawerLayout.closeDrawers();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public void onBackPressed() {
        //AndyUtils.appLog("CurrentFragment", currentFragment);
        if (currentFragment.equals(Const.REQUEST_FRAGMENT) || currentFragment.equals(Const.SEARCH_FRAGMENT) || currentFragment.equals(Const.HOURLY_FRAGMENT) || currentFragment.equals(Const.AIRPORT_FRAGMENT)) {

            addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);

        } else

        {
            if (!isFinishing()) {
                openExitDialog();
            }
        }

    }

    private void openExitDialog() {

        final Dialog exit_dialog = new Dialog(this, R.style.DialogSlideAnim_leftright);
        exit_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        exit_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        exit_dialog.setCancelable(true);
        exit_dialog.setContentView(R.layout.exit_layout);
        TextView tvExitOk = (TextView) exit_dialog.findViewById(R.id.tvExitOk);
        TextView tvExitCancel = (TextView) exit_dialog.findViewById(R.id.tvExitCancel);
        tvExitOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit_dialog.dismiss();
                finishAffinity();
            }
        });
        tvExitCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit_dialog.dismiss();
            }
        });
        exit_dialog.show();
    }

    private void ShowGpsDialog() {

        isGpsDialogShowing = true;

        gpsBuilder = new AlertDialog.Builder(
                this);


        gpsBuilder.setCancelable(false);
        gpsBuilder
                .setTitle(getResources().getString(R.string.txt_gps_off))
                .setMessage(getResources().getString(R.string.txt_gps_msg))
                .setPositiveButton(getResources().getString(R.string.txt_enable),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                Intent intent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                                removeGpsDialog();
                            }
                        })

                .setNegativeButton(getResources().getString(R.string.txt_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                removeGpsDialog();
                                finishAffinity();
                            }
                        });
        gpsAlertDialog = gpsBuilder.create();

        gpsAlertDialog.show();

    }


    private void removeGpsDialog() {
        if (gpsAlertDialog != null && gpsAlertDialog.isShowing()) {
            gpsAlertDialog.dismiss();
            isGpsDialogShowing = false;
            gpsAlertDialog = null;


        }
    }

    public void addFragment(Fragment fragment, boolean addToBackStack,
                            String tag, boolean isAnimate) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (isAnimate) {
            ft.setCustomAnimations(R.anim.slide_in_right,
                    R.anim.slide_out_left, R.anim.slide_in_left,
                    R.anim.slide_out_right);

        }

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }

        ft.replace(R.id.content_frame, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onResume() {
        super.onResume();


        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // do something else
            if (isGpsDialogShowing) {
                return;
            }
            ShowGpsDialog();
        } else {


            removeGpsDialog();
        }

        registerReceiver(internetConnectionReciever, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));

        registerReceiver(GpsChangeReceiver, new IntentFilter(
                LocationManager.PROVIDERS_CHANGED_ACTION));
        isRecieverRegistered = true;
       /* if(currentFragment.equals(Const.HOME_MAP_FRAGMENT)){
            Log.e("mahi","coming 1");
            addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);
        } else if(currentFragment.equals(Const.REQUEST_FRAGMENT)){
            addFragment(new RequestMapFragment(), false, Const.REQUEST_FRAGMENT, true);
        } else if(currentFragment.equals(Const.SEARCH_FRAGMENT)){
            addFragment(new SearchPlaceFragment(), false, Const.SEARCH_FRAGMENT, true);
        } else if(currentFragment.equals("")) {
            addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);
            Log.e("mahi","coming 2");
        }*/

    }

    public BroadcastReceiver internetConnectionReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo activeWIFIInfo = connectivityManager
                    .getNetworkInfo(connectivityManager.TYPE_WIFI);

            if (activeWIFIInfo.isConnected() || activeNetInfo.isConnected()) {
                removeInternetDialog();
            } else {
                if (isNetDialogShowing) {
                    return;
                }
                showInternetDialog();
            }
        }
    };

    private void removeInternetDialog() {
        if (internetDialog != null && internetDialog.isShowing()) {
            internetDialog.dismiss();
            isNetDialogShowing = false;
            internetDialog = null;

        }
    }

    private void showInternetDialog() {

        isNetDialogShowing = true;
        AlertDialog.Builder internetBuilder = new AlertDialog.Builder(
                MainActivity.this);
        internetBuilder.setCancelable(false);
        internetBuilder
                .setTitle(getString(R.string.dialog_no_internet))
                .setMessage(getString(R.string.dialog_no_inter_message))
                .setPositiveButton(getString(R.string.dialog_enable_3g),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                Intent intent = new Intent(
                                        android.provider.Settings.ACTION_SETTINGS);
                                startActivity(intent);
                                removeInternetDialog();
                            }
                        })
                .setNeutralButton(getString(R.string.dialog_enable_wifi),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // User pressed Cancel button. Write
                                // Logic Here
                                startActivity(new Intent(
                                        Settings.ACTION_WIFI_SETTINGS));
                                removeInternetDialog();
                            }
                        })
                .setNegativeButton(getString(R.string.dialog_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                removeInternetDialog();
                                finish();
                            }
                        });
        internetDialog = internetBuilder.create();
        internetDialog.show();
    }


    public BroadcastReceiver GpsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction() != null) {

                final LocationManager manager = (LocationManager) context
                        .getSystemService(Context.LOCATION_SERVICE);
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // do something

                    removeGpsDialog();
                } else {
                    // do something else
                    if (isGpsDialogShowing) {
                        return;
                    }

                    ShowGpsDialog();
                }

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isRecieverRegistered) {

            unregisterReceiver(GpsChangeReceiver);
            unregisterReceiver(internetConnectionReciever);
        }

    }

    private void checkreqstatus() {
        if (!AndyUtils.isNetworkAvailable(this)) {

            return;
        }
        AndyUtils.showSimpleProgressDialog(MainActivity.this, "", false);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.CHECKREQUEST_STATUS);
        map.put(Const.Params.ID, new PreferenceHelper(this).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(this).getSessionToken());


        Log.d("mahesh", "API calling" + map.toString());
        new VollyRequester(this, Const.POST, map, Const.ServiceCode.CHECKREQUEST_STATUS,
                this);
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.CHECKREQUEST_STATUS:
                Log.d("mahi", "check req" + response);
                if (response != null) {

                    Bundle bundle = new Bundle();
                    RequestDetail requestDetail = pcontent.parseRequestStatus(response);
                    Travel_Map_Fragment travalfragment = new Travel_Map_Fragment();
                    if (requestDetail == null) {
                        return;
                    }
                    Log.d("mahi", "check req status" + requestDetail.getTripStatus());

                    switch (requestDetail.getTripStatus()) {
                        case Const.NO_REQUEST:
                            new PreferenceHelper(this).clearRequestData();
                            if (!currentFragment.equals(Const.HOME_MAP_FRAGMENT)) {
                                addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);
                            }
                            break;
                        case Const.IS_CREATED:

                            //new PreferenceHelper(this).clearRequestData();
                            if (!currentFragment.equals(Const.HOME_MAP_FRAGMENT)) {
                                addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);
                            }
                            break;
                        case Const.IS_ACCEPTED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_ACCEPTED);
                            if (!currentFragment.equals(Const.TRAVEL_MAP_FRAGMENT)) {
                                travalfragment.setArguments(bundle);
                                addFragment(travalfragment, false, Const.TRAVEL_MAP_FRAGMENT,
                                        true);
                            }
                            break;
                        case Const.IS_DRIVER_DEPARTED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_DRIVER_DEPARTED);
                            if (!currentFragment.equals(Const.TRAVEL_MAP_FRAGMENT)) {
                                travalfragment.setArguments(bundle);
                                addFragment(travalfragment, false, Const.TRAVEL_MAP_FRAGMENT,
                                        true);
                            }
                            break;
                        case Const.IS_DRIVER_ARRIVED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_DRIVER_ARRIVED);
                            if (!currentFragment.equals(Const.TRAVEL_MAP_FRAGMENT)) {
                                travalfragment.setArguments(bundle);
                                addFragment(travalfragment, false, Const.TRAVEL_MAP_FRAGMENT,
                                        true);
                            }
                            break;
                        case Const.IS_DRIVER_TRIP_STARTED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_DRIVER_TRIP_STARTED);
                            if (!currentFragment.equals(Const.TRAVEL_MAP_FRAGMENT)) {
                                travalfragment.setArguments(bundle);
                                addFragment(travalfragment, false, Const.TRAVEL_MAP_FRAGMENT,
                                        true);
                            }
                            break;
                        case Const.IS_DRIVER_TRIP_ENDED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_DRIVER_TRIP_ENDED);

                            if (!currentFragment.equals(Const.RATING_FRAGMENT)) {
                                RatingFragment feedbackFragment = new RatingFragment();
                                feedbackFragment.setArguments(bundle);
                                addFragment(feedbackFragment, false, Const.RATING_FRAGMENT,
                                        true);
                            }
                            break;
                        case Const.IS_DRIVER_RATED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_DRIVER_TRIP_ENDED);

                            if (!currentFragment.equals(Const.RATING_FRAGMENT)) {
                                RatingFragment feedbackFragment = new RatingFragment();
                                feedbackFragment.setArguments(bundle);
                                addFragment(feedbackFragment, false, Const.RATING_FRAGMENT,
                                        true);
                            }
                            break;
                        default:
                            break;

                    }
                }


        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //Log.d("mahesh","coming to permission");
                    checkreqstatus();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
