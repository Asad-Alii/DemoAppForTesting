package com.alqaswa.user.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.simpleratingbar.SimpleRatingBar;
import com.bumptech.glide.Glide;
import com.gdacciaro.iOSDialog.iOSDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.alqaswa.user.Adapter.CancelReasonAdapter;
import com.alqaswa.user.ChatActivity;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Location.LocationHelper;
import com.alqaswa.user.Models.CancelReason;
import com.alqaswa.user.Models.RequestDetail;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.CarAnimation.AnimateMarker;
import com.alqaswa.user.Utils.Commonutils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.ParseContent;
import com.alqaswa.user.Utils.PreferenceHelper;
import com.alqaswa.user.Utils.RecyclerLongPressClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by user on 1/12/2017.
 */

public class Travel_Map_Fragment extends BaseMapFragment implements LocationHelper.OnLocationReceived, AsyncTaskCompleteListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private Bundle mBundle;
    SupportMapFragment user_travel_map;
    private View view;
    private LocationHelper locHelper;
    private Location myLocation;
    private RequestDetail requestDetail;
    private int jobStatus = 0;
    private TextView tv_current_location, driver_name, driver_car_number,driver_car_model, driver_mobile_number, address_title, tv_driver_status;
    private CircleImageView driver_img;
    private Marker driver_car, source_marker, destination_marker;
    private LatLng d_latlon, s_latlon, driver_latlan;
    Handler checkreqstatus;
    private String eta_time = "--";
    private String mobileNo = "";
    private Socket mSocket;
    private Boolean isConnected = true;
    private Boolean isMarkerRotating = false;
    private boolean iscancelpopup = false;
    MarkerOptions pickup_opt;
    private LatLng delayLatlan;
    private CircleImageView driver_car_img;
    private List<LatLng> mPathPolygonPoints;
    int mIndexCurrentPoint = 0;
    Bitmap mMarkerIcon;
    private LinearLayout cancel_trip;
    private ArrayList<CancelReason> cancelReasonLst;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.travel_fragment, container,
                false);


        driver_img = (CircleImageView) view.findViewById(R.id.driver_img);
        driver_car_img = (CircleImageView) view.findViewById(R.id.imageView2);
        tv_current_location = (TextView) view.findViewById(R.id.tv_current_location);
        driver_name = (TextView) view.findViewById(R.id.driver_name);
        driver_car_number = (TextView) view.findViewById(R.id.driver_car_number);
        driver_car_model= (TextView) view.findViewById(R.id.driver_car_model);
        driver_mobile_number = (TextView) view.findViewById(R.id.driver_mobile_number);
        address_title = (TextView) view.findViewById(R.id.address_title);
        tv_driver_status = (TextView) view.findViewById(R.id.tv_driver_status);


        tv_current_location.setSelected(true);

        user_travel_map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.user_travel_map);

        if (null != user_travel_map) {
            user_travel_map.getMapAsync(this);
        }

        cancel_trip = (LinearLayout) view.findViewById(R.id.cancel_trip);
        cancel_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final iOSDialog cancelDialog = new iOSDialog(activity);
                cancelDialog.setTitle(getResources().getString(R.string.cancel_ride));
                cancelDialog.setSubtitle(getResources().getString(R.string.cancel_txt));

                cancelDialog.setNegativeLabel(getResources().getString(R.string.txt_no));
                cancelDialog.setPositiveLabel(getResources().getString(R.string.txt_yes));
                cancelDialog.setBoldPositiveLabel(false);
                cancelDialog.setNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelDialog.dismiss();
                    }
                });
                cancelDialog.setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // cancelride();
                        GetcancelrideList();
                        stopCheckingforstatus();
                        cancelDialog.dismiss();
                    }
                });
                cancelDialog.show();
            }
        });

        ((LinearLayout) view.findViewById(R.id.driver_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final iOSDialog ContactDialog = new iOSDialog(activity);
                ContactDialog.setTitle(getResources().getString(R.string.txt_contact_driver));
                ContactDialog.setSubtitle(mobileNo);

                ContactDialog.setNegativeLabel(getResources().getString(R.string.txt_call));
                ContactDialog.setPositiveLabel(getResources().getString(R.string.txt_msg));
                ContactDialog.setBoldPositiveLabel(false);
                ContactDialog.setNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!mobileNo.equals("")) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + mobileNo));
                            startActivity(callIntent);

                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                //request permission from user if the app hasn't got the required permission
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.CALL_PHONE},   //request specific permission from user
                                        10);
                                return;

                            } else {     //have got permission
                                try {
                                    startActivity(callIntent);  //call activity and make phone call
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(activity, "yourActivity is not founded", Toast.LENGTH_SHORT).show();
                                }
                            }


                        }
                        ContactDialog.dismiss();
                    }
                });
                ContactDialog.setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendnotification();
                        if (requestDetail != null) {

                            Intent i = new Intent(activity, ChatActivity.class);
                            i.putExtra("reciver_id", requestDetail.getDriver_id());
                            startActivity(i);
                        }
                        ContactDialog.dismiss();
                    }
                });
                ContactDialog.show();
            }
        });

        return view;
    }

    private void sendnotification() {

        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ServiceType.USER_MESSAGE_NOTIFY + Const.Params.ID + "="
                + new PreferenceHelper(activity).getUserId() + "&" + Const.Params.TOKEN + "="
                + new PreferenceHelper(activity).getSessionToken() + "&" + Const.Params.REQUEST_ID + "=" + requestDetail.getRequestId());
        Log.d("mahi", "send_noty" + map.toString());
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.USER_MESSAGE_NOTIFY,
                this);
    }


    private void cancelride(String reason_id, String reasontext) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        Commonutils.progressdialog_show(activity, "Canceling...");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.CANCEL_RIDE);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.REQUEST_ID, String.valueOf(new PreferenceHelper(activity).getRequestId()));
        map.put("reason_id", reason_id);
        map.put("cancellation_reason", reasontext);

        Log.d("mahi", "cancel_reg" + map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.CANCEL_RIDE,
                this);
    }

    private void GetcancelrideList() {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        Commonutils.progressdialog_show(activity, "Loading...");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.CANCEL_REASON);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());

        Log.d("mahi", "cancel_req lst" + map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.CANCEL_REASON,
                this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        checkreqstatus = new Handler();
        cancelReasonLst = new ArrayList<CancelReason>();

        mPathPolygonPoints = new ArrayList<LatLng>();
        mMarkerIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_booking_lux_map_topview);
        
        try {
            mSocket = IO.socket(Const.ServiceType.SOCKET_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.connect();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
        }


        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        locHelper = new LocationHelper(activity);
        locHelper.setLocationReceivedLister(this);

        Bundle mBundle = getArguments();
        if (mBundle != null) {
            requestDetail = (RequestDetail) mBundle.getSerializable(
                    Const.REQUEST_DETAIL);
            jobStatus = mBundle.getInt(Const.DRIVER_STATUS,
                    Const.IS_DRIVER_DEPARTED);
            Glide.with(activity).load(requestDetail.getDriver_picture()).error(R.drawable.defult_user).into(driver_img);
            Glide.with(activity).load(requestDetail.getDriver_car_picture()).error(R.mipmap.car_background).into(driver_car_img);

            if (jobStatus == 1 || jobStatus == 2) {
                address_title.setText(activity.getString(R.string.txt_pickup_address));
                address_title.setTextColor(ContextCompat.getColor(activity, R.color.green));
                tv_current_location.setText(requestDetail.getS_address());
            } else {
                address_title.setText(activity.getString(R.string.txt_drop_address));
                address_title.setTextColor(ContextCompat.getColor(activity, R.color.red));
                if (!requestDetail.getD_address().equals("")) {
                    tv_current_location.setText(requestDetail.getD_address());
                } else {
                    tv_current_location.setText(getResources().getString(R.string.not_available));
                }

                if (source_marker != null) {
                    source_marker.hideInfoWindow();

                }
            }
            driver_name.setText(requestDetail.getDriver_name());
            driver_mobile_number.setText(getResources().getString(R.string.txt_mobile) + " " + requestDetail.getDriver_mobile());
            driver_car_number.setText(getResources().getString(R.string.txt_car_no) + " " + requestDetail.getDriver_car_number());
            driver_car_model.setText(requestDetail.getDriver_car_color()+" "+requestDetail.getDriver_car_model());
            mobileNo = requestDetail.getDriver_mobile();

        }

    }


    private Bitmap getMarkerBitmapFromViewforsource(String value) {

        View customMarkerView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.source_infowindow, null);
        ImageView info_iv = (ImageView) customMarkerView.findViewById(R.id.info_iv);
        if (value.equals("1")) {
            info_iv.setImageResource(R.mipmap.pickup_location);
        } else {
            info_iv.setImageResource(R.mipmap.drop_location);
        }
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private Bitmap getMarkerBitmapFromView(String eta, String value) {
        String time = eta.replaceAll("\\s+", "\n");
        View customMarkerView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.eta_info_window, null);
        TextView markertext = (TextView) customMarkerView.findViewById(R.id.txt_eta);
        ImageView iv = (ImageView) customMarkerView.findViewById(R.id.eta_iv);
        if (value.equals("1")) {
            iv.setImageResource(R.drawable.s_eta_circle);
        } else {
            iv.setImageResource(R.drawable.d_eta_circle);
        }
        markertext.setText(time);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private void getDirections(double latitude, double longitude, double latitude1, double longitude1) {

        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.DIRECTION_API_BASE + Const.ORIGIN + "="
                + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&" + Const.DESTINATION + "="
                + String.valueOf(latitude1) + "," + String.valueOf(longitude1) + "&" + Const.EXTANCTION);

        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.GOOGLE_DIRECTION_API, this);
    }


    @Override
    public void onResume() {
        super.onResume();
        activity.currentFragment = Const.TRAVEL_MAP_FRAGMENT;
        startgetreqstatus();
        //Log.e("mahi", "Trip fragment");

    }

    @Override
    public void onLocationReceived(LatLng latlong) {

        if (mSocket.connected()) {
            attemptSend(latlong);

        }

    }

    private void attemptSend(LatLng latlong) {

        if (!mSocket.connected()) return;

        JSONObject messageObj = new JSONObject();
        try {
            messageObj.put("latitude", String.valueOf(latlong.latitude));
            messageObj.put("longitude", String.valueOf(latlong.longitude));
            messageObj.put("sender", new PreferenceHelper(activity).getUserId());
            if (null != requestDetail.getDriver_id()) {
                messageObj.put("receiver", requestDetail.getDriver_id());
            }
            messageObj.put("status", "1");
            messageObj.put("request_id", new PreferenceHelper(activity).getRequestId());

            Log.e("mahi", "calling socket" + messageObj.toString());

            mSocket.emit("send location", messageObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationReceived(Location location) {

    }

    @Override
    public void onConntected(Bundle bundle) {

    }

    @Override
    public void onConntected(Location location) {
        if (null != googleMap) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(s_latlon,
                    17));
        }
    }

    public void drawPath(String result) {


        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);


            PolylineOptions options = new PolylineOptions().width(8).color(Color.BLACK).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            if (googleMap != null) {
                Polyline poly_line = googleMap.addPolyline(options);
            }

           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        } catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void startgetreqstatus() {
        startCheckstatusTimer();
    }

    private void startCheckstatusTimer() {
        checkreqstatus.postDelayed(reqrunnable, 4000);
    }

    private void stopCheckingforstatus() {
        if (checkreqstatus != null) {
            checkreqstatus.removeCallbacks(reqrunnable);

            Log.d("mahi", "stop status handler");
        }
    }

    Runnable reqrunnable = new Runnable() {
        public void run() {
            checkreqstatus();
            checkreqstatus.postDelayed(this, 4000);
        }
    };

    private void checkreqstatus() {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.CHECKREQUEST_STATUS);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());


        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.CHECKREQUEST_STATUS,
                this);
    }

    private void fitmarkers_toMap() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
        builder.include(source_marker.getPosition());
        builder.include(destination_marker.getPosition());
        ;

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        googleMap.moveCamera(cu);


      /* if (s_latlon != null) {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(s_latlon)
                    .zoom(17).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //  source_marker.showInfoWindow();

        }*/


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.user_travel_map);
        if (f != null) {
            try {
                getFragmentManager().beginTransaction().remove(f).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

         /*TO clear all views */
        ViewGroup mContainer = (ViewGroup) getActivity().findViewById(R.id.content_frame);
        mContainer.removeAllViews();


        googleMap = null;
    }


    private void findDistanceAndTime(LatLng s_latlan, LatLng d_latlan) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        if (null != s_latlan && null != d_latlan) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Const.Params.URL, Const.GOOGLE_MATRIX_URL + Const.Params.ORIGINS + "="
                    + String.valueOf(s_latlan.latitude) + "," + String.valueOf(s_latlan.longitude) + "&" + Const.Params.DESTINATION + "="
                    + String.valueOf(d_latlan.latitude) + "," + String.valueOf(d_latlan.longitude) + "&" + Const.Params.MODE + "="
                    + "driving" + "&" + Const.Params.LANGUAGE + "="
                    + "en-EN" + "&" + "key=" + Const.GOOGLE_API_KEY + "&" + Const.Params.SENSOR + "="
                    + String.valueOf(false));
            Log.e("mahi", "distance api" + map);
            new VollyRequester(activity, Const.GET, map, Const.ServiceCode.GOOGLE_MATRIX, this);
        } else {
            if (null != source_marker) {
                source_marker.setIcon((BitmapDescriptorFactory
                        .fromBitmap(getMarkerBitmapFromViewforsource("1"))));

            }
        }
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {
            case Const.ServiceCode.GOOGLE_DIRECTION_API:

                if (response != null) {
                    drawPath(response);

                }
                break;
            case Const.ServiceCode.CANCEL_REASON:
                Log.d("mahi", "cancel reason" + response);
                Commonutils.progressdialog_hide();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    cancelReasonLst.clear();
                    if (jsonObject.getString("success").equals("true")) {
                        JSONArray dataArray = jsonObject.optJSONArray("data");
                        if (null != dataArray && dataArray.length() > 0)
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObj = dataArray.optJSONObject(i);
                                CancelReason cancel = new CancelReason();
                                cancel.setReasonId(dataObj.optString("id"));
                                cancel.setReasontext(dataObj.optString("cancel_reason"));
                                cancelReasonLst.add(cancel);

                            }
                        if (null != cancelReasonLst && cancelReasonLst.size() > 0) {
                            CancelReasonDialog(cancelReasonLst);
                        } else {
                            AndyUtils.showShortToast(getResources().getString(R.string.txt_no_cancel_reason),activity);
                        }
                    } else {
                        AndyUtils.showShortToast(jsonObject.optString("error_message"), activity);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case Const.ServiceCode.USER_MESSAGE_NOTIFY:
                Log.e("mahi", "notify trip" + response);
                if (response != null) {

                }
                break;
            case Const.ServiceCode.CANCEL_RIDE:
                Log.d("mahi", "cancel request" + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("success").equals("true")) {
                        Commonutils.progressdialog_hide();

                        new PreferenceHelper(activity).clearRequestData();
                        activity.addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);

                    } else {
                        Commonutils.progressdialog_hide();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case Const.ServiceCode.GOOGLE_MATRIX:
                Log.d("mahi", "google distance api" + response);
                try {
                    if (googleMap != null) {
                        googleMap.getUiSettings().setScrollGesturesEnabled(true);
                    }
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("OK")) {
                        JSONArray sourceArray = jsonObject.getJSONArray("origin_addresses");
                        String sourceObject = (String) sourceArray.get(0);

                        JSONArray destinationArray = jsonObject.getJSONArray("destination_addresses");
                        String destinationObject = (String) destinationArray.get(0);

                        JSONArray jsonArray = jsonObject.getJSONArray("rows");
                        JSONObject elementsObject = jsonArray.getJSONObject(0);
                        JSONArray elementsArray = elementsObject.getJSONArray("elements");
                        JSONObject distanceObject = elementsArray.getJSONObject(0);
                        JSONObject dObject = distanceObject.getJSONObject("distance");
                        String distance = dObject.getString("text");
                        JSONObject durationObject = distanceObject.getJSONObject("duration");
                        final String duration = durationObject.getString("text");
                        eta_time = duration;
                        if (pickup_opt != null && source_marker != null) {
                            // Commonutils.showtoast("showing",activity);
                            if (jobStatus == 1 || jobStatus == 2 || jobStatus == 3) {
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (jobStatus == 3) {
                                            source_marker.setIcon((BitmapDescriptorFactory
                                                    .fromBitmap(getMarkerBitmapFromView("0 MIN", "1"))));
                                        } else {
                                            source_marker.setIcon((BitmapDescriptorFactory
                                                    .fromBitmap(getMarkerBitmapFromView(duration, "1"))));
                                        }
                                        if (null != destination_marker) {
                                            destination_marker.setIcon((BitmapDescriptorFactory
                                                    .fromBitmap(getMarkerBitmapFromViewforsource("2"))));
                                        }

                                    }
                                });

                            } else {
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {

                                        source_marker.setIcon((BitmapDescriptorFactory
                                                .fromBitmap(getMarkerBitmapFromViewforsource("1"))));
                                        if (null != destination_marker) {
                                            destination_marker.setIcon((BitmapDescriptorFactory
                                                    .fromBitmap(getMarkerBitmapFromView(duration, "2"))));
                                        }

                                    }
                                });

                            }
                            // pickup_marker = googleMap.addMarker(pickup_opt);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Const.ServiceCode.CHECKREQUEST_STATUS:
                Log.d("mahi", "check req status" + response);

                if (response != null) {

                    Bundle bundle = new Bundle();
                    RequestDetail requestDetail = new ParseContent(activity).parseRequestStatus(response);
                    Travel_Map_Fragment travalfragment = new Travel_Map_Fragment();
                    if (requestDetail == null) {
                        return;
                    }


                    switch (requestDetail.getTripStatus()) {
                        case Const.NO_REQUEST:
                            if (isAdded() && iscancelpopup == false && googleMap != null && activity.currentFragment.equals(Const.TRAVEL_MAP_FRAGMENT)) {
                                iscancelpopup = true;
                                stopCheckingforstatus();
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(getResources().getString(R.string.txt_cancel_driver))
                                        .setCancelable(false)
                                        .setPositiveButton(getResources().getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                new PreferenceHelper(activity).clearRequestData();
                                                //googleMap.clear();
                                                dialog.dismiss();
                                                activity.addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);

                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();

                            }
                            break;

                        case Const.IS_ACCEPTED:
                            jobStatus = Const.IS_ACCEPTED;
                            address_title.setText(activity.getString(R.string.txt_pickup_address));
                            address_title.setTextColor(ContextCompat.getColor(activity, R.color.green));
                            tv_current_location.setText(requestDetail.getS_address());
                            tv_driver_status.setText(activity.getString(R.string.text_job_accepted));
                            findDistanceAndTime(s_latlon, driver_latlan);
                            break;
                        case Const.IS_DRIVER_DEPARTED:
                            jobStatus = Const.IS_DRIVER_DEPARTED;
                            address_title.setText(activity.getString(R.string.txt_pickup_address));
                            address_title.setTextColor(ContextCompat.getColor(activity, R.color.green));
                            tv_current_location.setText(requestDetail.getS_address());
                            tv_driver_status.setText(activity.getString(R.string.text_driver_started));
                            findDistanceAndTime(s_latlon, driver_latlan);
                            break;
                        case Const.IS_DRIVER_ARRIVED:
                            jobStatus = Const.IS_DRIVER_ARRIVED;
                            address_title.setText(activity.getString(R.string.txt_drop_address));
                            address_title.setTextColor(ContextCompat.getColor(activity, R.color.red));
                            if (!requestDetail.getD_address().equals("")) {
                                tv_current_location.setText(requestDetail.getD_address());
                            } else {
                                tv_current_location.setText(getResources().getString(R.string.not_available));
                            }
                            tv_driver_status.setText(activity.getString(R.string.text_driver_arrvied));
                            cancel_trip.setVisibility(View.GONE);
                            driver_latlan = new LatLng(requestDetail.getDriver_latitude(), requestDetail.getDriver_longitude());
                            findDistanceAndTime(s_latlon, driver_latlan);
                            break;
                        case Const.IS_DRIVER_TRIP_STARTED:
                            jobStatus = Const.IS_DRIVER_TRIP_STARTED;
                            address_title.setText(activity.getString(R.string.txt_drop_address));
                            address_title.setTextColor(ContextCompat.getColor(activity, R.color.red));
                            if (!requestDetail.getD_address().equals("")) {
                                tv_current_location.setText(requestDetail.getD_address());
                            } else {
                                tv_current_location.setText(getResources().getString(R.string.not_available));
                            }
                            tv_driver_status.setText(activity.getString(R.string.text_trip_started));
                            cancel_trip.setVisibility(View.GONE);
                            findDistanceAndTime(d_latlon, driver_latlan);
                            break;
                        case Const.IS_DRIVER_TRIP_ENDED:
                            jobStatus = Const.IS_DRIVER_TRIP_ENDED;
                            address_title.setText(activity.getString(R.string.txt_drop_address));
                            address_title.setTextColor(ContextCompat.getColor(activity, R.color.red));
                            if (!requestDetail.getD_address().equals("")) {
                                tv_current_location.setText(requestDetail.getD_address());
                            } else {
                                tv_current_location.setText(getResources().getString(R.string.not_available));
                            }
                            tv_driver_status.setText(activity.getString(R.string.text_trip_completed));
                            cancel_trip.setVisibility(View.GONE);
                            findDistanceAndTime(d_latlon, driver_latlan);
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_DRIVER_TRIP_ENDED);


                            if (!activity.currentFragment.equals(Const.RATING_FRAGMENT) && !activity.isFinishing()) {

                                stopCheckingforstatus();

                                RatingFragment feedbackFragment = new RatingFragment();
                                feedbackFragment.setArguments(bundle);
                                if (activity != null)
                                    activity.addFragment(feedbackFragment, false, Const.RATING_FRAGMENT,
                                            true);
                            }
                            break;
                        case Const.IS_DRIVER_RATED:
                            jobStatus = Const.IS_DRIVER_TRIP_ENDED;
                            address_title.setText(activity.getString(R.string.txt_drop_address));
                            address_title.setTextColor(ContextCompat.getColor(activity, R.color.red));
                            findDistanceAndTime(d_latlon, driver_latlan);
                            if (!requestDetail.getD_address().equals("")) {
                                tv_current_location.setText(requestDetail.getD_address());
                            } else {
                                tv_current_location.setText(getResources().getString(R.string.not_available));
                            }
                            tv_driver_status.setText(activity.getString(R.string.text_trip_completed));

                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_DRIVER_TRIP_ENDED);
                            cancel_trip.setVisibility(View.GONE);

                            if (!activity.currentFragment.equals(Const.RATING_FRAGMENT)) {

                                stopCheckingforstatus();

                                RatingFragment feedbackFragment = new RatingFragment();
                                feedbackFragment.setArguments(bundle);
                                activity.addFragment(feedbackFragment, false, Const.RATING_FRAGMENT,
                                        true);
                            }

                            break;
                        default:
                            break;

                    }
                }
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isConnected) {

                        try {
                            if (requestDetail.getDriver_id() != null) {
                                JSONObject object = new JSONObject();
                                object.put("sender", new PreferenceHelper(activity).getUserId());
                                object.put("receiver", requestDetail.getDriver_id());
                                Log.e("update_object", "" + object);
                                mSocket.emit("update sender", object);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        isConnected = true;
                    }
                    if (isConnected) {


                        try {
                            if (requestDetail.getDriver_id() != null) {
                                JSONObject object = new JSONObject();
                                object.put("sender", new PreferenceHelper(activity).getUserId());
                                object.put("receiver", requestDetail.getDriver_id());
                                Log.e("update_object", "" + object);
                                mSocket.emit("update sender", object);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;

                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String sender;
                    String receiver;
                    String latitude;
                    String longitude;
                    String bearing;
                    String location;
                    try {
                        sender = data.getString("sender");
                        receiver = data.getString("receiver");
                        latitude = data.getString("latitude");
                        longitude = data.getString("longitude");
                        bearing = data.getString("bearing");


                        Log.d("mahi", "message from socket" + data.toString());
                        if (googleMap != null) {

                            driver_latlan = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                            delayLatlan = driver_latlan;
                            Location driver_location = new Location("Driver Location");
                            driver_location.setLatitude(driver_latlan.latitude);
                            driver_location.setLongitude(driver_latlan.longitude);

                            if (driver_car == null && null != driver_latlan && null != googleMap) {
                                driver_car = googleMap.addMarker(new MarkerOptions()
                                        .position(driver_latlan)
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_booking_lux_map_topview))
                                        .title(getResources().getString(R.string.txt_driver)));

                                AnimateMarker.animateMarker(activity, driver_location, driver_car, googleMap, bearing);
                            } else {
                                AnimateMarker.animateMarker(activity, driver_location, driver_car, googleMap, bearing);
                            }

                        }

                    } catch (JSONException e) {
                        return;
                    }


                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }
/*
                    addLog(getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);*/
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

  /*  private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };*/

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCheckingforstatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopCheckingforstatus();

        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
    }


    @Override
    public void onMapReady(GoogleMap mgoogleMap) {
        // map.
        googleMap = mgoogleMap;
        AndyUtils.removeProgressDialog();

        if (googleMap != null) {
            googleMap.setTrafficEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            /*MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(
                    activity, R.raw.maps_style);
            googleMap.setMapStyle(style);*/

            // AndyUtils.removeLoader();
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            googleMap.setMyLocationEnabled(false);

            LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = lm.getProviders(true);
            Location l = null;

            if (requestDetail.getS_lat() != null) {
                s_latlon = new LatLng(Double.valueOf(requestDetail.getS_lat()), Double.valueOf(requestDetail.getS_lon()));

                pickup_opt = new MarkerOptions();
                pickup_opt.position(s_latlon);
                pickup_opt.title(getResources().getString(R.string.txt_pickup));
                pickup_opt.anchor(0.5f, 0.5f);
                pickup_opt.icon(BitmapDescriptorFactory
                        .fromBitmap(getMarkerBitmapFromView(eta_time, "1")));
                source_marker = googleMap.addMarker(pickup_opt);
            }
            if (requestDetail.getD_lat() != null && !requestDetail.getD_lat().equals("") && !requestDetail.getD_address().equals("")) {
                d_latlon = new LatLng(Double.valueOf(requestDetail.getD_lat()), Double.valueOf(requestDetail.getD_lon()));

                MarkerOptions opt = new MarkerOptions();
                opt.position(d_latlon);
                opt.title(activity.getResources().getString(R.string.txt_drop_loc));
                opt.anchor(0.5f, 0.5f);
                opt.icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.map_drop_marker));
                destination_marker = googleMap.addMarker(opt);
            }

            driver_latlan = new LatLng(requestDetail.getDriver_latitude(), requestDetail.getDriver_longitude());
            if (d_latlon != null && s_latlon != null) {

                getDirections(s_latlon.latitude, s_latlon.longitude, d_latlon.latitude, d_latlon.longitude);
            }

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                               @Override
                                               public View getInfoWindow(Marker marker) {
                                                   View vew = null;
                                                   if (destination_marker != null) {
                                                       if (marker.getId().equals(destination_marker.getId())) {
                                                           vew = activity.getLayoutInflater().inflate(R.layout.info_window_dest, null);
                                                     /*  new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                           @Override
                                                           public void run() {

                                                               if (drop_marker != null) {
                                                                   drop_marker.showInfoWindow();
                                                               }
                                                           }
                                                       });*/
                                                       } else if (marker.getId().equals(source_marker.getId())) {
                                                          /* vew = activity.getLayoutInflater().inflate(R.layout.eta_info_window, null);
                                                           TextView txt_location = (TextView) vew.findViewById(R.id.txt_location);
                                                           txt_location.setText(activity.getString(R.string.txt_pickup_loc));
                                                           final TextView txt_eta_marker = (TextView) vew.findViewById(R.id.txt_eta);
*/
                                                           if (jobStatus == 1 || jobStatus == 2) {
                                                               //  txt_eta_marker.setText(eta_time);
                                                           } else {
                                                              /* vew = activity.getLayoutInflater().inflate(R.layout.info_window_dest, null);
                                                               TextView location = (TextView) vew.findViewById(R.id.txt_location);
                                                               location.setText(activity.getString(R.string.txt_pickup_loc));*/

                                                           }

                                                         /*  new CountDownTimer(2000, 1000) {

                                                               public void onTick(long millisUntilFinished) {

                                                               }

                                                               public void onFinish() {
                                                                   if (jobStatus == 1 || jobStatus == 2) {
                                                                       if (source_marker != null) {
                                                                           source_marker.showInfoWindow();

                                                                       }
                                                                   } else {
                                                                       if (source_marker != null) {
                                                                           source_marker.hideInfoWindow();
                                                                           if (isAdded()) {
                                                                               fitmarkers_toMap();
                                                                           }
                                                                       }
                                                                   }
                                                               }

                                                           }.start();*/

                                                           // final TextView txt_location_marker = (TextView) vew.findViewById(R.id.txt_location);
                                                       } else {
                                                           vew = activity.getLayoutInflater().inflate(R.layout.driver_info_window, null);
                                                           TextView txt_driver_name = (TextView) vew.findViewById(R.id.driver_name);
                                                           SimpleRatingBar driver_rate = (SimpleRatingBar) vew.findViewById(R.id.driver_rate);
                                                           driver_rate.setVisibility(View.GONE);
                                                       }
                                                   } else {
                                                     /*  vew = activity.getLayoutInflater().inflate(R.layout.driver_info_window, null);
                                                       SimpleRatingBar driver_rate = (SimpleRatingBar) vew.findViewById(R.id.driver_rate);
                                                       driver_rate.setVisibility(View.GONE);*/
                                                   }

                                                   return vew;

                                               }

                                               @Override
                                               public View getInfoContents(Marker marker) {
                                                   // Getting view from the layout file infowindowlayout.xml
                                                   return null;
                                               }
                                           }
            );

            if (isAdded() && destination_marker != null) {
                fitmarkers_toMap();
            }

        }
    }

    private void CancelReasonDialog(final ArrayList<CancelReason> cancelReasonLst) {
        final Dialog CancelReasondialog = new Dialog(activity, R.style.DialogThemeforview);
        CancelReasondialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CancelReasondialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.fade_drawable));
        CancelReasondialog.setCancelable(false);
        CancelReasondialog.setContentView(R.layout.cancel_request_layout);
        RecyclerView cancel_reason_lst = (RecyclerView) CancelReasondialog.findViewById(R.id.cancel_reason_lst);

        CancelReasonAdapter CancelAdapter = new CancelReasonAdapter(activity, cancelReasonLst);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        cancel_reason_lst.setLayoutManager(mLayoutManager);
        cancel_reason_lst.setItemAnimator(new DefaultItemAnimator());
        cancel_reason_lst.setAdapter(CancelAdapter);

        cancel_reason_lst.addOnItemTouchListener(new RecyclerLongPressClickListener(activity, cancel_reason_lst, new RecyclerLongPressClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                cancelride(cancelReasonLst.get(position).getReasonId(), cancelReasonLst.get(position).getReasontext());
                CancelReasondialog.dismiss();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        CancelReasondialog.show();

    }




}
