package com.alqaswa.user.Fragment;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aurelhubert.simpleratingbar.SimpleRatingBar;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.alqaswa.user.Adapter.AdsAdapter;
import com.alqaswa.user.Adapter.TaxiAdapter;
import com.alqaswa.user.AdapterCallback;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Location.LocationHelper;
import com.alqaswa.user.Models.AdsList;
import com.alqaswa.user.Models.NearByDrivers;
import com.alqaswa.user.Models.RequestDetail;
import com.alqaswa.user.Models.TaxiTypes;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Commonutils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.ItemClickSupport;
import com.alqaswa.user.Utils.ParseContent;
import com.alqaswa.user.Utils.PreferenceHelper;
import com.alqaswa.user.WelcomeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by user on 1/5/2017.
 */

public class Home_Map_Fragment extends BaseMapFragment implements LocationHelper.OnLocationReceived, AsyncTaskCompleteListener, OnMapReadyCallback, AdapterCallback, GoogleMap.OnMapClickListener, GoogleMap.OnCameraMoveListener {

    private String TAG = Home_Map_Fragment.class.getSimpleName();
    private GoogleMap googleMap;
    private Bundle mBundle;
    SupportMapFragment HomemapFragment;
    private View view;
    private LocationHelper locHelper;
    private Location myLocation;
    private LatLng latlong;
    private static final int DURATION = 1500;
    private TextView tv_current_location, tv_time_date, tv_total_dis, tv_estimate_fare;
    private static Marker pickup_marker, drop_marker, my_marker;
    MarkerOptions pickup_opt;
    private FloatingActionButton btn_floating_hourly, btn_floating_airport, btn_floating_bolt;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private AutoCompleteTextView et_sch_destination_address, et_sch_source_address;
    private String base_price = "", min_price = "", booking_fee = "", currency = "", distance_unit = "", tax_price = "";

    private static LinearLayout layout_search;
    TaxiAdapter taxiAdapter;
    private boolean s_click = false, d_click = false;
    public static ImageButton btn_mylocation;
    private ArrayList<TaxiTypes> typesList;
    private ArrayList<NearByDrivers> driverslatlngs = new ArrayList<>();
    private HashMap<Marker, Integer> markermap = new HashMap<>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private int marker_position;
    private LatLng driverlatlan;
    Handler providerhandler;
    public static String pickup_add = "";
    private ImageButton btn_schedule;
    DatePickerDialog dpd;
    TimePickerDialog tpd;
    String datetime = "";
    private LatLng sch_pic_latLng, sch_drop_latLng;
    private String taxi_type;
    private ProgressBar pbfareProgress;
    private ImageView bottomSheetArrowImage;
    private RecyclerView adsRecyclerView;
    private AdsAdapter adsAdapter;
    private List<AdsList> adsLists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.home_map_fragment, container,
                false);

        tv_current_location = (TextView) view.findViewById(R.id.tv_current_location);
        layout_search = (LinearLayout) view.findViewById(R.id.layout_search);
        btn_mylocation = (ImageButton) view.findViewById(R.id.btn_mylocation);
        btn_schedule = (ImageButton) view.findViewById(R.id.btn_schedule);
        btn_floating_hourly = (FloatingActionButton) view.findViewById(R.id.btn_floating_hourly);
        btn_floating_airport = (FloatingActionButton) view.findViewById(R.id.btn_floating_airport);
        btn_floating_bolt = (FloatingActionButton) view.findViewById(R.id.btn_floating_bolt);
        HomemapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.home_map);
        btn_mylocation.setOnClickListener(this);
        tv_current_location.setOnClickListener(this);
        btn_schedule.setOnClickListener(this);
        btn_floating_hourly.setOnClickListener(this);
        btn_floating_airport.setOnClickListener(this);
        btn_floating_bolt.setOnClickListener(this);

        if (HomemapFragment != null) {
            HomemapFragment.getMapAsync(this);
        }

        bottomSheetArrowImage = (ImageView) view.findViewById(R.id.imageViewArrow);
        final View bottomSheet = view.findViewById(R.id.design_bottom_sheet);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.e("BottomSheetCallback", "BottomSheetBehavior.STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.e("BottomSheetCallback", "BottomSheetBehavior.STATE_SETTLING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                       /* if (!bottomsheet_actionbar.isShown()) {
                         //   bottomsheet_actionbar.setVisibility(View.VISIBLE);
                        }*/
                        Log.e("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                       /* if (bottomsheet_actionbar.isShown()) {
                       //     bottomsheet_actionbar.setVisibility(View.GONE);
                        }*/
                        Log.e("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.e("BottomSheetCallback", "BottomSheetBehavior.STATE_HIDDEN");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.e("asher", "slideOffset value " + slideOffset);
                rotateArrow(slideOffset);
            }
        });
        behavior.setHideable(false);
        behavior.setSkipCollapsed(false);
        adsRecyclerView = (RecyclerView) view.findViewById(R.id.recycAds);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        adsRecyclerView.setLayoutManager(mLayoutManager);
        adsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemClickSupport.addTo(adsRecyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Log.e("asher", "item click " + adsLists.get(position).getAdUrl());
//                        if (position == 0) {
//                            startNewActivity(activity, "com.coffeedrop.user");
//                        } else {
//                            // do it

                            String url = adsLists.get(position).getAdUrl();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
//                        }


                    }
                });
        getAds();

        bottomSheetArrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });


        return view;
    }

    private void getAds() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ServiceType.ADVERTISEMENTS);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());

        AndyUtils.appLog(TAG, "adsList " + map);

        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.ADVERTISEMENTS, this);
    }

    private void rotateArrow(float v) {
        bottomSheetArrowImage.setRotation(-180 * v);
    }


    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName + "&hle=en"));
            context.startActivity(intent);
        }
    }


    @Override
    public void onMapReady(GoogleMap mgoogleMap) {
        googleMap = mgoogleMap;
        if (googleMap != null) {
            AndyUtils.removeProgressDialog();
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            /*MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(
                    activity, R.raw.maps_style);
            googleMap.setMapStyle(style);*/
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
            // googleMap.setMyLocationEnabled(true);
            //googleMap.setMaxZoomPreference(17);
            //  AndyUtils.removeLoader();
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

            /*MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(activity, R.raw.night_modemap);
            googleMap.setMapStyle(style);*/

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                               @Override
                                               public View getInfoWindow(Marker marker) {
                                                   View vew = null;
                                                   if (markermap.get(marker) != -1 && markermap.get(marker) != -2) {
                                                       vew = activity.getLayoutInflater().inflate(R.layout.driver_info_window, null);
                                                       TextView txt_driver_name = (TextView) vew.findViewById(R.id.driver_name);

                                                       if (driverslatlngs.size() > 0) {
                                                           txt_driver_name.setText(driverslatlngs.get(marker_position).getDriver_name());
                                                           SimpleRatingBar driver_rate = (SimpleRatingBar) vew.findViewById(R.id.driver_rate);
                                                           driver_rate.setRating(driverslatlngs.get(marker_position).getDriver_rate());

                                                       }
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

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    if (markermap.get(marker) != -1 && markermap.get(marker) != -2) {
                        marker_position = markermap.get(marker);

                    } else if (markermap.get(marker) == -1) {
                        SearchPlaceFragment searcfragment = new SearchPlaceFragment();
                        Bundle mbundle = new Bundle();
                        mbundle.putString("pickup_address", pickup_add);
                        searcfragment.setArguments(mbundle);
                        activity.addFragment(searcfragment, false, Const.SEARCH_FRAGMENT, true);
                    } else {

                    }


                    return false;
                }
            });


        }

        mgoogleMap.setOnMapClickListener(this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBundle = savedInstanceState;
        typesList = new ArrayList<TaxiTypes>();
        providerhandler = new Handler();

        getTypesforhome();
    }

    private void getTypesforhome() {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.TAXI_TYPE);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.HOMETAXI_TYPE,
                this);
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

        locHelper = new LocationHelper(activity);
        locHelper.setLocationReceivedLister(this);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        getTypesforhome();

        if (new PreferenceHelper(activity).getRequestId() == Const.NO_REQUEST) {
            startgetProvider();
        }

    }


    @Override
    public void onLocationReceived(final LatLng latlong) {
        if (latlong != null) {
           /* googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong,
                    16));*/

        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }


    @Override
    public void onLocationReceived(Location location) {
        if (location != null) {
            // drawTrip(latlong);
            myLocation = location;
            LatLng latLang = new LatLng(location.getLatitude(),
                    location.getLongitude());
            latlong = latLang;

        }

    }


    private void addOverlay(double latitude, double longitude) {
        if (null != googleMap) {
            GroundOverlay groundOverlay = googleMap.addGroundOverlay(new
                    GroundOverlayOptions()
                    .position(new LatLng(latitude, longitude), 100)
                    .transparency(0.5f)
                    .zIndex(3)
                    .image(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(activity.getResources().getDrawable(R.drawable.map_overlay)))));

            startOverlayAnimation(groundOverlay);
        }
    }


    private void startOverlayAnimation(final GroundOverlay groundOverlay) {

        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator vAnimator = ValueAnimator.ofInt(0, 200);
        vAnimator.setRepeatCount(ValueAnimator.INFINITE);
        vAnimator.setRepeatMode(ValueAnimator.RESTART);
        vAnimator.setInterpolator(new LinearInterpolator());
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final Integer val = (Integer) valueAnimator.getAnimatedValue();
                groundOverlay.setDimensions(val);

            }
        });

        ValueAnimator tAnimator = ValueAnimator.ofFloat(0, 1);
        tAnimator.setRepeatCount(ValueAnimator.INFINITE);
        tAnimator.setRepeatMode(ValueAnimator.RESTART);
        tAnimator.setInterpolator(new LinearInterpolator());
        tAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float val = (Float) valueAnimator.getAnimatedValue();
                groundOverlay.setTransparency(val);
            }
        });

        animatorSet.setDuration(3000);
        animatorSet.playTogether(vAnimator, tAnimator);
        animatorSet.start();
    }


    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    private void getAllProviders(LatLng latlong) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_PROVIDERS);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        if (latlong != null) {
            map.put(Const.Params.LATITUDE, String.valueOf(latlong.latitude));
            map.put(Const.Params.LONGITUDE, String.valueOf(latlong.longitude));
        }

        map.put(Const.Params.TAXI_TYPE, new PreferenceHelper(activity).getRequestType());

        Log.d("mahi", "nearby drivers" + map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.GET_PROVIDERS,
                this);

    }

    @Override
    public void onConntected(Bundle bundle) {

    }

    @Override
    public void onConntected(Location location) {

        if (location != null && null != googleMap) {
            final LatLng currentlatLang = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatLang,
                    16));

            BaseMapFragment.pic_latlan = currentlatLang;
            getCompleteAddressString(currentlatLang.latitude, currentlatLang.longitude);
            addOverlay(currentlatLang.latitude, currentlatLang.longitude);
            MarkerOptions markerOpt = new MarkerOptions();
            markerOpt.position(currentlatLang);
            markerOpt.icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_map));
            Marker locMark = googleMap.addMarker(markerOpt);
            markermap.put(locMark, -2);

        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_current_location:
                SearchPlaceFragment searcfragment = new SearchPlaceFragment();
                Bundle mbundle = new Bundle();
                mbundle.putString("pickup_address", pickup_add);
                searcfragment.setArguments(mbundle);
                activity.addFragment(searcfragment, false, Const.SEARCH_FRAGMENT, true);
                break;
            case R.id.btn_mylocation:
                if (googleMap != null && latlong != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong,
                            16));
                }
                break;
            case R.id.btn_schedule:

                if (null != pickup_add && null != typesList) {
                    showshcdule(pickup_add, typesList);
                } else {
                    AndyUtils.showShortToast(getResources().getString(R.string.txt_error), activity);
                }
                break;
            case R.id.btn_floating_hourly:
                HourlyBookngFragment hourlyfragment = new HourlyBookngFragment();
                Bundle nbundle = new Bundle();
                nbundle.putString("pickup_address", pickup_add);
                hourlyfragment.setArguments(nbundle);
                activity.addFragment(hourlyfragment, false, Const.HOURLY_FRAGMENT, true);
                break;
            case R.id.btn_floating_airport:
                activity.addFragment(new AirportBookingFragment(), false, Const.AIRPORT_FRAGMENT, true);
                break;
            case R.id.btn_floating_bolt:
                // startActivity(new Intent(activity, Bolt_Msg_Fragment.class));
                activity.addFragment(new Bolt_Msg_Fragment(), false, Const.BOLT_FRAGMENT, true);
                break;


        }

    }

    private void showshcdule(String pickup_add, final ArrayList<TaxiTypes> typesList) {
        sch_drop_latLng = null;

        final Dialog schedule_dialog = new Dialog(activity, R.style.DialogSlideAnim);
        schedule_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        schedule_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        schedule_dialog.setCancelable(true);
        schedule_dialog.setContentView(R.layout.schedule_layout);
        et_sch_source_address = (AutoCompleteTextView) schedule_dialog.findViewById(R.id.et_sch_source_address);
        et_sch_destination_address = (AutoCompleteTextView) schedule_dialog.findViewById(R.id.et_sch_destination_address);
        et_sch_source_address.setText(pickup_add);
        /*et_sch_source_address.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));
        et_sch_destination_address.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));*/

        try {
            getLatlanfromAddress(URLEncoder.encode(et_sch_source_address.getText().toString(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        et_sch_source_address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    d_click = false;
                    s_click = true;

                    //showSearchMap(et_source_address.getText().toString(), et_destination_address.getText().toString(), mBundle);

                    try {
                        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                                .setTypeFilter(Place.TYPE_COUNTRY)
                                .setCountry("PK")
                                .build();
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                        .setFilter(autocompleteFilter)
                                        .build(activity);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException e) {
                        // TODO: Handle the error.
                    } catch (GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                    }
                }

                return true; // return is important...
            }
        });


        et_sch_destination_address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    d_click = true;
                    s_click = false;

                    // showSearchMap(et_source_address.getText().toString(), et_destination_address.getText().toString(), mBundle);

                    try {
                        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                                .setTypeFilter(Place.TYPE_COUNTRY)
                                .setCountry("PK")
                                .build();
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                        .setFilter(autocompleteFilter)
                                        .build(activity);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException e) {
                        // TODO: Handle the error.
                    } catch (GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                    }

                }

                return true; // return is important...
            }
        });

        TextView btn_sch_submit = (TextView) schedule_dialog.findViewById(R.id.btn_sch_submit);
        RecyclerView lst_sch_vehicle = (RecyclerView) schedule_dialog.findViewById(R.id.lst_sch_vehicle);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        lst_sch_vehicle.setLayoutManager(mLayoutManager);
        lst_sch_vehicle.setItemAnimator(new DefaultItemAnimator());
        lst_sch_vehicle.addItemDecoration(new SpacesItemDecoration(18));

       /* final PlacesAutoCompleteAdapter placesadapter = new PlacesAutoCompleteAdapter(getContext(),
                R.layout.autocomplete_list_text);
        final PlacesAutoCompleteAdapter placesadapter2 = new PlacesAutoCompleteAdapter(getContext(),
                R.layout.autocomplete_list_text);

        if (placesadapter != null) {
            et_sch_source_address.setAdapter(placesadapter);

        }
        if (placesadapter2 != null) {
            et_sch_destination_address.setAdapter(placesadapter2);
        }*/

      /*  et_sch_source_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                et_sch_source_address.setSelection(0);
                //sch_pic_latLng[0] = getLocationFromAddress(activity, et_sch_source_address.getText().toString());
                AndyUtils.hideKeyBoard(activity);
                final String selectedSourcePlace = placesadapter.getItem(i);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getLatlanfromAddress(URLEncoder.encode(selectedSourcePlace, "utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });*/

     /*   et_sch_destination_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                et_sch_destination_address.setSelection(0);
                // sch_drop_latLng[0] = getLocationFromAddress(activity, et_sch_destination_address.getText().toString());
                AndyUtils.hideKeyBoard(activity);
                final String selectedDestPlace = placesadapter2.getItem(i);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            getLocationforDest(URLEncoder.encode(selectedDestPlace, "utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });*/

        if (typesList != null) {
            taxiAdapter = new TaxiAdapter(activity, typesList, this);
            lst_sch_vehicle.setAdapter(taxiAdapter);

            if (typesList.size() > 0) {
                taxi_type = typesList.get(0).getId();
            }

        }

        btn_sch_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_time_date.getText().toString().length() == 0) {
                    Commonutils.showtoast(getResources().getString(R.string.txt_error_date_time), activity);
                } else if (et_sch_destination_address.getText().toString().length() == 0) {
                    Commonutils.showtoast(getResources().getString(R.string.txt_destination_error), activity);
                } else {
                    schedule_dialog.dismiss();

                    bookschedule(sch_drop_latLng, sch_pic_latLng, taxi_type, tv_time_date.getText().toString(), et_sch_source_address.getText().toString(), et_sch_destination_address.getText().toString());
                }


            }
        });

        ItemClickSupport.addTo(lst_sch_vehicle)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        taxi_type = typesList.get(position).getId();
                        taxiAdapter.OnItemClicked(position);
                        taxiAdapter.notifyDataSetChanged();


                    }
                });

        tv_time_date = (TextView) schedule_dialog.findViewById(R.id.tv_time_date);
        tv_time_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePicker();
            }
        });

        schedule_dialog.show();

    }

    private void bookschedule(LatLng droplatlan, LatLng pciklatLng, String type, String datetime, String s_address, String d_addressa) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }
        Commonutils.progressdialog_show(activity, "Requesting...");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.REQUEST_LATER);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.SERVICE_TYPE, type);
        map.put(Const.Params.S_ADDRESS, s_address);
        map.put(Const.Params.D_ADDRESS, d_addressa);
        if (pciklatLng != null) {
            map.put(Const.Params.LATITUDE, String.valueOf(pciklatLng.latitude));
            map.put(Const.Params.LONGITUDE, String.valueOf(pciklatLng.longitude));
        }

        if (droplatlan != null) {
            map.put(Const.Params.D_LATITUDE, String.valueOf(droplatlan.latitude));
            map.put(Const.Params.D_LONGITUDE, String.valueOf(droplatlan.longitude));
        } else {
            map.put(Const.Params.D_LATITUDE, "");
            map.put(Const.Params.D_LONGITUDE, "");
        }
        map.put("requested_time", datetime);
        map.put(Const.Params.REQ_STATUS_TYPE, "1");
        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.REQUEST_LATER,
                this);

    }

    private void DatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        dpd = new DatePickerDialog(getActivity(), R.style.datepicker,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(android.widget.DatePicker view,
                                          int year, int monthOfYear, int dayOfMonth) {
                        // txtDate.setText(dayOfMonth + "-"
                        // + (monthOfYear + 1) + "-" + year);

                        if (view.isShown()) {
                            // Toast.makeText(
                            // getActivity(),
                            // dayOfMonth + "-" + (monthOfYear + 1) + "-"
                            // + year, Toast.LENGTH_LONG).show();

                            datetime = Integer.toString(year) + "-"
                                    + Integer.toString(monthOfYear + 1) + "-"
                                    + Integer.toString(dayOfMonth);

                            TimePicker();

                            dpd.dismiss();
                        }
                    }
                }, mYear, mMonth, mDay);

        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.txt_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dpd.dismiss();
                    }
                });
        // dpd.getDatePicker().setMaxDate(addDays(new Date(),90).getTime());
        // dpd.getDatePicker().setMinDate(new Date().getTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        dpd.getDatePicker().setMaxDate(cal.getTimeInMillis());
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        dpd.getDatePicker().setMinDate(cal.getTimeInMillis());

        dpd.show();
    }

    public void TimePicker() {

        //Log.d("pavan", "in time picker");

        final Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 30);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);


        tpd = new TimePickerDialog(getActivity(), R.style.datepicker,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(android.widget.TimePicker view,
                                          int hourOfDay, int minute) {
                        // txtTime.setText(hourOfDay + ":" + minute);
                        if (view.isShown()) {
                            tpd.dismiss();
                            // isTimePickerOpen = false;
                            // call api here
                            datetime = datetime.concat(" "
                                    + Integer.toString(hourOfDay) + ":"
                                    + Integer.toString(minute) + ":" + "00");

                            tv_time_date.setText(datetime);
                        }
                    }
                }, mHour, mMinute, false);

        tpd.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.txt_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tpd.dismiss();
                    }
                });

        tpd.show();

    }

    @Override
    public void onMethodCallback(String id, String taxitype, String taxi_price_distance, String taxi_price_min, String taxiimage, String taxi_seats, String basefare) {
        taxi_type = id;
        if (null != sch_pic_latLng && null != sch_drop_latLng) {
            findDistanceAndTime(sch_pic_latLng, sch_drop_latLng);
            showfareestimate(taxitype, taxi_price_distance, taxi_price_min, taxiimage, taxi_seats, basefare);
        } else {
            AndyUtils.showShortToast(getResources().getString(R.string.txt_drop_pick_error), activity);
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (null != my_marker) {
            my_marker.remove();
        }

        MarkerOptions pickup_opt = new MarkerOptions();
        pickup_opt.position(latLng);
        pickup_opt.icon(BitmapDescriptorFactory
                .fromBitmap(getMarkerBitmapFromView("---")));

        if (null != googleMap) {
            my_marker = googleMap.addMarker(pickup_opt);
            markermap.put(my_marker, -1);
            BaseMapFragment.pic_latlan = latLng;
            getCompleteAddress(latLng.latitude, latLng.longitude);
        }


    }

    @Override
    public void onCameraMove() {

    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
           /* if(parent.getChildPosition(view) == 0)
                outRect.top = space;*/
        }
    }


    @Override
    public void onResume() {
        super.onResume();


        activity.currentFragment = Const.HOME_MAP_FRAGMENT;
        //setUpMap();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        stopCheckingforproviders();
    }

    private void startgetProvider() {
        startCheckProviderTimer();
        //Log.e("mahi", "req_id" + new PreferenceHelper(activity).getRequestId());
    }

    private void startCheckProviderTimer() {
        providerhandler.postDelayed(runnable, 4000);
    }

    private void stopCheckingforproviders() {
        if (providerhandler != null) {
            providerhandler.removeCallbacks(runnable);

            // Log.d("mahi", "stop provider handler");
        }
    }

    Runnable runnable = new Runnable() {
        public void run() {

            getAllProviders(latlong);
            checkreqstatus();
            providerhandler.postDelayed(this, 4000);
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

    private void getLatlanfromAddress(String selectedSourcePlace) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.LOCATION_API_BASE + selectedSourcePlace + "&key=" + Const.GOOGLE_API_KEY);

        Log.d("mahi", "map for s_loc" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.LOCATION_API_BASE_SOURCE, this);
    }

    private void getLocationforDest(String selectedDestPlace) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.LOCATION_API_BASE + selectedDestPlace + "&key=" + Const.GOOGLE_API_KEY);
        Log.d("mahi", "map for d_loc" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.LOCATION_API_BASE_DESTINATION, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("mahi", "on destory view is calling");
        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.home_map);
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

    @Override
    public void onDestroy() {
        stopCheckingforproviders();
        super.onDestroy();

    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {

            case Const.ServiceCode.ADDRESS_API_BASE:
                if (null != response) {
                    try {
                        JSONObject job = new JSONObject(response);
                        JSONArray jarray = job.optJSONArray("results");
                        JSONObject locObj = jarray.getJSONObject(0);
                        pickup_add = locObj.optString("formatted_address");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case Const.ServiceCode.GOOGLE_ADDRESS_API:
                if (null != response) {
                    try {
                        JSONObject job = new JSONObject(response);
                        JSONArray jarray = job.optJSONArray("results");
                        JSONObject locObj = jarray.getJSONObject(0);
                        pickup_add = locObj.optString("formatted_address");
                        tv_current_location.setText(pickup_add);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (null != my_marker && null != googleMap)
                                    my_marker.setIcon((BitmapDescriptorFactory
                                            .fromBitmap(getMarkerBitmapFromView(pickup_add))));
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Const.ServiceCode.LOCATION_API_BASE_SOURCE:
                if (null != response) {
                    try {
                        JSONObject job = new JSONObject(response);
                        JSONArray jarray = job.optJSONArray("results");
                        JSONObject locObj = jarray.getJSONObject(0);
                        JSONObject geometryOBJ = locObj.optJSONObject("geometry");
                        JSONObject locationOBJ = geometryOBJ.optJSONObject("location");
                        double lat = locationOBJ.getDouble("lat");
                        double lan = locationOBJ.getDouble("lng");
                        sch_pic_latLng = new LatLng(lat, lan);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Const.ServiceCode.LOCATION_API_BASE_DESTINATION:
                if (null != response) {
                    try {
                        JSONObject job = new JSONObject(response);
                        JSONArray jarray = job.optJSONArray("results");
                        JSONObject locObj = jarray.getJSONObject(0);
                        JSONObject geometryOBJ = locObj.optJSONObject("geometry");
                        JSONObject locationOBJ = geometryOBJ.optJSONObject("location");
                        double lat = locationOBJ.getDouble("lat");
                        double lan = locationOBJ.getDouble("lng");
                        sch_drop_latLng = new LatLng(lat, lan);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case Const.ServiceCode.HOMETAXI_TYPE:
                try {
                    JSONObject job = new JSONObject(response);
                    if (job.getString("success").equals("true")) {
                        typesList.clear();
                        JSONArray jarray = job.getJSONArray("services");
                        if (jarray.length() > 0) {
                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject taxiobj = jarray.getJSONObject(i);
                                TaxiTypes type = new TaxiTypes();
                                type.setCurrencey_unit(job.optString("currency"));
                                type.setId(taxiobj.getString("id"));
                                type.setTaxi_cost(taxiobj.getString("min_fare"));
                                type.setTaxiimage(taxiobj.getString("picture"));
                                type.setTaxitype(taxiobj.getString("name"));
                                type.setTaxi_price_min(taxiobj.getString("price_per_min"));
                                type.setTaxi_price_distance(taxiobj.getString("price_per_unit_distance"));
                                type.setTaxi_seats(taxiobj.getString("number_seat"));
                                typesList.add(type);
                            }

                            if (typesList != null && typesList.size() > 0) {
                                new PreferenceHelper(activity).putRequestType(typesList.get(0).getId());
                                if (null != taxiAdapter) {
                                    taxiAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Const.ServiceCode.REQUEST_LATER:
                Log.d("mahi", "create req later" + response);
                if (response != null) {
                    try {
                        JSONObject job = new JSONObject(response);
                        if (job.getString("success").equals("true")) {
                            Commonutils.progressdialog_hide();
                            Commonutils.showtoast(getResources().getString(R.string.txt_trip_schedule_success), activity);
                        } else {
                            Commonutils.progressdialog_hide();
                            String error = job.getString("error");
                            Commonutils.showtoast(error, activity);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                            new PreferenceHelper(activity).clearRequestData();
                            // startgetProvider();


                            break;

                        case Const.IS_ACCEPTED:
                        case Const.IS_DRIVER_DEPARTED:
                        case Const.IS_DRIVER_ARRIVED:
                        case Const.IS_DRIVER_TRIP_STARTED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_ACCEPTED);
                            if (!activity.currentFragment.equals(Const.TRAVEL_MAP_FRAGMENT)) {
                                stopCheckingforproviders();

                                travalfragment.setArguments(bundle);
                                activity.addFragment(travalfragment, false, Const.TRAVEL_MAP_FRAGMENT,
                                        true);

                            }


                            BaseMapFragment.drop_latlan = null;
                            BaseMapFragment.pic_latlan = null;
                            BaseMapFragment.s_address = "";
                            BaseMapFragment.d_address = "";

                            break;
                        case Const.IS_DRIVER_TRIP_ENDED:


                            break;
                        case Const.IS_DRIVER_RATED:

                            break;
                        default:
                            break;

                    }
                }
                break;
            case Const.ServiceCode.GET_PROVIDERS:
                Log.d("mahi", "providers" + response);

                if (response != null) {
                    try {
                        if (googleMap != null) {
                            googleMap.getUiSettings().setScrollGesturesEnabled(true);
                        }
                        JSONObject job = new JSONObject(response);
                        if (job.getString("success").equals("true")) {
                            driverslatlngs.clear();
                            JSONArray jarray = job.getJSONArray("providers");
                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject driversObj = jarray.getJSONObject(i);
                                NearByDrivers drivers = new NearByDrivers();
                                drivers.setLatlan(new LatLng(Double.valueOf(driversObj.getString("latitude")), Double.valueOf(driversObj.getString("longitude"))));
                                drivers.setId(driversObj.getString("id"));
                                drivers.setDriver_name(driversObj.getString("first_name"));
                                if (driversObj.getString("rating").equals("0")) {
                                    drivers.setDriver_rate(0);
                                } else {
                                    drivers.setDriver_rate(Integer.valueOf(driversObj.getString("rating").charAt(0)));
                                }
                                drivers.setDriver_distance(driversObj.getString("distance"));
                                driverslatlngs.add(drivers);
                            }

                        } else {
                            String error_code = job.optString("error_code");
                            if (error_code.equals("104")) {
                                AndyUtils.showShortToast("You have logged in other device!", activity);
                                new PreferenceHelper(activity).Logout();
                                startActivity(new Intent(activity, WelcomeActivity.class));
                                activity.finish();

                            }
                        }


                        if (driverslatlngs.size() > 0) {

                            /*if (googleMap != null) {
                                googleMap.clear();
                            }*/
                            for (Marker marker : markers) {
                                marker.remove();
                            }
                            markers.clear();

                            for (int i = 0; i < driverslatlngs.size(); i++) {
                           /* CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                                    driverslatlngs.get(i), 15);*/
                                // Log.d("mahi","markers size"+driverslatlngs.get(i).toString());

                                final MarkerOptions currentOption = new MarkerOptions();
                                currentOption.position(driverslatlngs.get(i).getLatlan());
                                currentOption.title(driverslatlngs.get(i).getDriver_name());
                                currentOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_limo));
                                if (googleMap != null) {
                                    final Marker[] driver_marker = new Marker[1];
                                    final int finalI = i;

                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            if (null == driver_marker[0]) {

                                                //driver_marker[0].remove();
                                                driver_marker[0] = googleMap.addMarker(currentOption);

                                            } else {
                                                driver_marker[0].setPosition(driverslatlngs.get(finalI).getLatlan());
                                            }

                                        }
                                    });

                                    markers.add(driver_marker[0]);
                                    //googleMap.animateCamera(location);
                                    markermap.put(driver_marker[0], i);

                                }
                            }

                        } else {

                            for (Marker marker : markers) {
                                marker.remove();
                            }
                            markers.clear();


                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 101:

                try {
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
                        String duration = durationObject.getString("text");
                        String dis = dObject.getString("value");
                        String dur = durationObject.getString("value");
                        //Log.d("mahi", "time and dis" + dur + " " + dis);
                        double trip_dis = Integer.valueOf(dis) * 0.001;
                        getTypes(String.valueOf(trip_dis), dur);


                     /*   et_clientlocation.setText(destinationObject);
                        et_doctorlocation.setText(sourceObject);
                        et_distance.setText("Distance:" + " " + distance);
                        et_eta.setText("ETA:" + " " + duration);*/


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case Const.ServiceCode.GOOGLE_MATRIX:
                // Log.d("mahi", "google distance api" + response);
                try {
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
                        String duration = durationObject.getString("text");
                        String dis = dObject.getString("value");
                        String dur = durationObject.getString("value");
                        // Log.d("mahi", "time and dis" + dur + " " + dis);
                        double trip_dis = Integer.valueOf(dis) * 0.001;
                        getFare(String.valueOf(trip_dis), dur, taxi_type);
                        tv_total_dis.setText(distance);

                     /*   et_clientlocation.setText(destinationObject);
                        et_doctorlocation.setText(sourceObject);
                        et_distance.setText("Distance:" + " " + distance);
                        et_eta.setText("ETA:" + " " + duration);*/


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Const.ServiceCode.FARE_CALCULATION:
                Log.d("mahi", "estimate fare" + response);

                if (response != null) {
                    try {
                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {
                            String fare = job1.getString("estimated_fare");
                            tax_price = job1.getString("tax_price");
                            base_price = job1.optString("base_price");
                            min_price = job1.optString("min_fare");
                            booking_fee = job1.optString("booking_fee");
                            currency = job1.optString("currency");
                            distance_unit = job1.optString("distance_unit");
                            tv_estimate_fare.setVisibility(View.VISIBLE);
                            tv_estimate_fare.setText(currency + fare);
                            if (pbfareProgress != null) {
                                pbfareProgress.setVisibility(View.GONE);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                break;
            case Const.ServiceCode.TAXI_TYPE:
                Log.d("mahi", "taxi type" + response);

                if (response != null) {
                    try {
                        JSONObject job = new JSONObject(response);
                        if (job.getString("success").equals("true")) {
                            typesList.clear();
                            JSONArray jarray = job.getJSONArray("services");
                            if (jarray.length() > 0) {
                                for (int i = 0; i < jarray.length(); i++) {
                                    JSONObject taxiobj = jarray.getJSONObject(i);
                                    TaxiTypes type = new TaxiTypes();
                                    type.setCurrencey_unit(job.optString("currency"));
                                    type.setId(taxiobj.getString("id"));
                                    type.setTaxi_cost(taxiobj.getString("estimated_fare"));
                                    type.setTaxiimage(taxiobj.getString("picture"));
                                    type.setTaxitype(taxiobj.getString("name"));
                                    type.setTaxi_price_min(taxiobj.getString("price_per_min"));
                                    type.setTaxi_price_distance(taxiobj.getString("price_per_unit_distance"));
                                    type.setTaxi_seats(taxiobj.getString("number_seat"));
                                    type.setBasefare(taxiobj.optString("min_fare"));
                                    typesList.add(type);
                                }

                                if (typesList != null) {
                                    // taxiAdapter = new TaxiAdapter(activity, typesList, this);
                                    taxiAdapter.notifyDataSetChanged();
                                }


                            }

                        } else {

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                break;

            case Const.ServiceCode.ADVERTISEMENTS:
                AndyUtils.appLog(TAG, "addsListResponse " + response);
                try {
                    JSONObject job1 = new JSONObject(response);
                    if (job1.getString("success").equals("true")) {
                        JSONArray jsonArray = job1.optJSONArray("data");
                        if (null != adsLists) {
                            adsLists.clear();
                        }
                        if (null != jsonArray && jsonArray.length() > 0) {
                            adsLists = new ParseContent(activity).parseAdsList(jsonArray);
                            if (adsLists != null) {
                                adsAdapter = new AdsAdapter(adsLists, activity);
                                //Adding adapter to Listview
                                adsRecyclerView.setAdapter(adsAdapter);
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;

        }

    }


    private void getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ADDRESS_API_BASE + LATITUDE + "," + LONGITUDE + "&key=" + Const.GOOGLE_API_KEY);

        Log.d("mahi", "map for address" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.ADDRESS_API_BASE, this);
    }

    private void getCompleteAddress(double LATITUDE, double LONGITUDE) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ADDRESS_API_BASE + LATITUDE + "," + LONGITUDE + "&key=" + Const.GOOGLE_API_KEY);

        Log.d("mahi", "map for address" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.GOOGLE_ADDRESS_API, this);
    }


    private Bitmap getMarkerBitmapFromView(String place) {
        View customMarkerView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.info_window_pickup, null);
        TextView markertext = (TextView) customMarkerView.findViewById(R.id.txt_pickup_location);

        markertext.setText(place);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final Place place = PlaceAutocomplete.getPlace(activity, data);


                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!d_click) {
                            et_sch_source_address.setText(place.getAddress());
                            sch_pic_latLng = place.getLatLng();
                            if (null != sch_drop_latLng && null != sch_pic_latLng) {
                                findDistanceAndTimeforTypes(sch_pic_latLng, sch_drop_latLng);
                            }

                        } else {
                            et_sch_destination_address.setText(place.getAddress());
                            sch_drop_latLng = place.getLatLng();
                            findDistanceAndTimeforTypes(sch_pic_latLng, sch_drop_latLng);
                        }

                    }
                });

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(activity, data);
                // TODO: Handle the error.
                Log.i("mahi", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void findDistanceAndTimeforTypes(LatLng pic_latlan, LatLng drop_latlan) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.GOOGLE_MATRIX_URL + Const.Params.ORIGINS + "="
                + String.valueOf(pic_latlan.latitude) + "," + String.valueOf(pic_latlan.longitude) + "&" + Const.Params.DESTINATION + "="
                + String.valueOf(drop_latlan.latitude) + "," + String.valueOf(drop_latlan.longitude) + "&" + Const.Params.MODE + "="
                + "driving" + "&" + Const.Params.LANGUAGE + "="
                + "en-EN" + "&" + "key=" + Const.GOOGLE_API_KEY + "&" + Const.Params.SENSOR + "="
                + String.valueOf(false));
        Log.e("mahi", "distance api" + map);
        new VollyRequester(activity, Const.GET, map, 101, this);
    }

    private void getTypes(String dis, String dur) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.TAXI_TYPE);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.DISTANCE, dis);
        map.put(Const.Params.TIME, dur);

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.TAXI_TYPE,
                this);
    }

    private void findDistanceAndTime(LatLng s_latlan, LatLng d_latlan) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.GOOGLE_MATRIX_URL + Const.Params.ORIGINS + "="
                + String.valueOf(s_latlan.latitude) + "," + String.valueOf(s_latlan.longitude) + "&" + Const.Params.DESTINATION + "="
                + String.valueOf(d_latlan.latitude) + "," + String.valueOf(d_latlan.longitude) + "&" + Const.Params.MODE + "="
                + "driving" + "&" + Const.Params.LANGUAGE + "="
                + "en-EN" + "&" + "key=" + Const.GOOGLE_API_KEY + "&" + Const.Params.SENSOR + "="
                + String.valueOf(false));
        Log.e("mahi", "distance api" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.GOOGLE_MATRIX, this);
    }


    private void showfareestimate(String taxitype, final String taxi_price_distance, final String taxi_price_min, String taxiimage, String taxi_seats, final String taxi_cost) {


        final Dialog faredialog = new Dialog(activity, R.style.DialogSlideAnim);
        faredialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        faredialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        faredialog.setCancelable(false);
        faredialog.setContentView(R.layout.fare_popup);
        ImageView type_img = (ImageView) faredialog.findViewById(R.id.fare_taxi_img);
        TextView tv_fare_taxi_name = (TextView) faredialog.findViewById(R.id.tv_fare_taxi_name);
        tv_estimate_fare = (TextView) faredialog.findViewById(R.id.tv_estimate_fare);
        tv_total_dis = (TextView) faredialog.findViewById(R.id.tv_total_dis);
        pbfareProgress = (ProgressBar) faredialog.findViewById(R.id.pbfareProgress);
        //TextView tv_cost_dis_fare = (TextView) faredialog.findViewById(R.id.tv_cost_dis_fare);
        TextView tv_total_capcity = (TextView) faredialog.findViewById(R.id.tv_total_capcity);
        TextView fare_done = (TextView) faredialog.findViewById(R.id.fare_done);
        ImageView btn_info = (ImageView) faredialog.findViewById(R.id.btn_info);


        Glide.with(activity).load(taxiimage).error(R.drawable.frontal_taxi_cab).into(type_img);
        tv_fare_taxi_name.setText(taxitype);
        tv_total_capcity.setText("1-" + taxi_seats);

        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showfarebreakdown(base_price, taxi_price_distance, taxi_price_min, min_price, booking_fee, currency, distance_unit);
            }
        });
        fare_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                faredialog.dismiss();
            }
        });


        faredialog.show();
    }

    private void showfarebreakdown(String base_price, String taxi_price_distance, String taxi_price_min, String min_price, String booking_fee, String currency, String distance_unit) {
        final Dialog farebreak = new Dialog(activity, R.style.DialogSlideAnim_leftright_Fullscreen);
        farebreak.requestWindowFeature(Window.FEATURE_NO_TITLE);
        farebreak.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_black)));
        farebreak.setCancelable(true);
        farebreak.setContentView(R.layout.fare_breakdown);
        TextView tv_dis_title = (TextView) farebreak.findViewById(R.id.tv_dis_title);
        TextView tv_base_fare = (TextView) farebreak.findViewById(R.id.tv_base_fare);
        TextView tv_min_fare = (TextView) farebreak.findViewById(R.id.tv_min_fare);
        TextView tv_per_min_cost = (TextView) farebreak.findViewById(R.id.tv_per_min_cost);
        TextView tv_per_km_price = (TextView) farebreak.findViewById(R.id.tv_per_km_price);
        TextView tv_service_tax_price = (TextView) farebreak.findViewById(R.id.tv_service_tax_price);
        TextView tv_booking_price = (TextView) farebreak.findViewById(R.id.tv_booking_price);

        tv_base_fare.setText(currency + base_price);
        tv_booking_price.setText(currency + booking_fee);
        tv_min_fare.setText(currency + min_price);
        tv_per_min_cost.setText(currency + taxi_price_min);
        tv_per_km_price.setText(currency + taxi_price_distance);
        tv_service_tax_price.setText(currency + tax_price);
        ImageView close_popup = (ImageView) farebreak.findViewById(R.id.close_popup);
        tv_dis_title.setText(getResources().getString(R.string.txt_per) + " " + distance_unit);
        close_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                farebreak.cancel();
            }
        });


        farebreak.show();

    }

    private void getFare(String distance, String duration, String service_id) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.FARE_CALCULATION);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.DISTANCE, distance);
        map.put(Const.Params.TIME, duration);
        map.put(Const.Params.TAXI_TYPE, service_id);

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.FARE_CALCULATION,
                this);
    }

}
