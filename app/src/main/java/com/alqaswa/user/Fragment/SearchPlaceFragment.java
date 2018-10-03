package com.alqaswa.user.Fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.alqaswa.user.Adapter.FavouritesDisplayAdapter;
import com.alqaswa.user.Adapter.PlacesAutoCompleteAdapter;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Location.LocationHelper;
import com.alqaswa.user.Models.Favourites;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.MarkerUtils.SmoothMoveMarker;
import com.alqaswa.user.Utils.PreferenceHelper;
import com.alqaswa.user.Utils.RecyclerLongPressClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by user on 1/7/2017.
 */

public class SearchPlaceFragment extends BaseMapFragment implements View.OnClickListener, LocationHelper.OnLocationReceived, OnMapReadyCallback {

    private AutoCompleteTextView et_source_address, et_destination_address, et_source_dia_address, et_destination_dia_address;
    private PlacesAutoCompleteAdapter placesadapter;
    private PlacesAutoCompleteAdapter dest_placesadapter;
    private ImageButton search_back,search_fav;
    private LatLng des_latLng;
    private Button btn_search;
    private GoogleMap gMap;
    private LocationHelper locHelper;
    private LatLng currentLatLan;
    private ImageView pin_marker;
    private boolean s_click = false, d_click = false,isClicked=false;
    private Bundle mBundle;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    SupportMapFragment search_place_map;
    private GoogleMap googleMap;
    private ArrayList<Favourites> favouritesArrayList;
    private Marker PickUpMarker, DropMarker;
    private ImageView btn_pickLoc, pin_drop_location,close_popup_fav;
    FavouritesDisplayAdapter favouritesAdapter;
    RecyclerView fav_lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locHelper = new LocationHelper(activity);
        locHelper.setLocationReceivedLister(this);
        mBundle = savedInstanceState;
        favouritesArrayList= new ArrayList<>();
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.source_destination_layout, container,
                false);
        et_source_address = (AutoCompleteTextView) view.findViewById(R.id.et_source_address);
        et_destination_address = (AutoCompleteTextView) view.findViewById(R.id.et_destination_address);
        search_back = (ImageButton) view.findViewById(R.id.search_back);
        search_fav = (ImageButton) view.findViewById(R.id.search_fav);
        pin_drop_location = (ImageView) view.findViewById(R.id.pin_drop_location);
        search_back.requestFocus();
        btn_search = (Button) view.findViewById(R.id.btn_search);
        ((ImageView) view.findViewById(R.id.btn_pickLoc)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != googleMap&&currentLatLan != null)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLan,
                            15));
            }
        });
        search_place_map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.search_place_map);

        if (null != search_place_map) {
            search_place_map.getMapAsync(this);
        }
        search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activity.addFragment(new Home_Map_Fragment(), false, Const.HOME_MAP_FRAGMENT, true);

            }
        });
        search_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFavouritePlaces();
            }
        });
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (des_latLng != null) {
                    BaseMapFragment.drop_latlan = des_latLng;
                    BaseMapFragment.searching = true;
                    AndyUtils.hideKeyBoard(activity);
                    BaseMapFragment.s_address = et_source_address.getText().toString();
                  //  BaseMapFragment.d_address = et_destination_address.getText().toString();
                    activity.addFragment(new RequestMapFragment(), false, Const.REQUEST_FRAGMENT, true);
                }

                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });

        /*placesadapter = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        dest_placesadapter = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);


        if (placesadapter != null) {
            et_source_address.setAdapter(placesadapter);

        }
        if (dest_placesadapter != null) {
            et_destination_address.setAdapter(dest_placesadapter);
        }*/

     /*   et_source_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                et_source_address.setSelection(0);
                //LatLng latLng = getLocationFromAddress(activity, et_source_address.getText().toString());
                AndyUtils.hideKeyBoard(activity);
                final String selectedSourcePlace = placesadapter.getItem(i);
                try {
                    getLatlanfromAddress(URLEncoder.encode(selectedSourcePlace, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        });*/
        /*et_source_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchMap(et_source_address.getText().toString(), et_destination_address.getText().toString(), savedInstanceState);
            }
        });*/

        et_source_address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isClicked==false) {
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
                            isClicked=true;
                        } catch (GooglePlayServicesRepairableException e) {
                            // TODO: Handle the error.
                        } catch (GooglePlayServicesNotAvailableException e) {
                            // TODO: Handle the error.
                        }
                    }
                }
                return true; // return is important...
            }
        });


        et_destination_address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isClicked == false) {
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
                            isClicked=true;
                        } catch (GooglePlayServicesRepairableException e) {
                            // TODO: Handle the error.
                        } catch (GooglePlayServicesNotAvailableException e) {
                            // TODO: Handle the error.
                        }

                    }
                }
                return true; // return is important...
            }
        });

       /* et_destination_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchMap(et_source_address.getText().toString(), et_destination_address.getText().toString(), savedInstanceState);
            }
        });*/
        et_source_address.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                et_source_address.setText("");
                et_source_address.requestFocus();
                return false;
            }
        });
        et_destination_address.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                et_destination_address.setText("");
                et_destination_address.requestFocus();
                return false;
            }
        });

      /*  et_destination_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                et_destination_address.setSelection(0);
                AndyUtils.hideKeyBoard(activity);
                final String selectedDestPlace = dest_placesadapter.getItem(i);
                try {
                    getLocationforDest(URLEncoder.encode(selectedDestPlace, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }


        });*/
       /* et_destination_address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //Commonutils.showtoast("search clicked", getApplicationContext());

                    if (des_latLng != null) {
                        BaseMapFragment.drop_latlan = des_latLng;
                        BaseMapFragment.searching = true;
                        AndyUtils.hideKeyBoard(activity);
                        BaseMapFragment.s_address = et_source_address.getText().toString();
                        BaseMapFragment.d_address = et_destination_address.getText().toString();
                        activity.addFragment(new RequestMapFragment(), false, Const.REQUEST_FRAGMENT, true);
                    }
                    return true;
                }
                return false;
            }
        });*/

        String source_address = "";
        if (savedInstanceState == null) {
            Bundle mBundle = getArguments();
            if (mBundle == null) {
                source_address = "";
            } else {
                source_address = mBundle.getString("pickup_address");
                et_source_address.setText(source_address);
                /*String[] address_lst = source_address.split(",");
                if (address_lst.length > 2) {
                    et_source_address.setText(address_lst[0] + "," + address_lst[1]);
                } else {
                    et_source_address.setText(address_lst[0]);
                }*/
                et_source_address.setSelection(0);

            }
        } else {
            source_address = (String) savedInstanceState.getSerializable("pickup_address");
            et_source_address.setText(source_address);
            /*String[] address_lst = source_address.split(",");
            if (address_lst.length > 2) {
                et_source_address.setText(address_lst[0] + "," + address_lst[1]);
            } else {
                et_source_address.setText(address_lst[0]);
            }*/
            et_source_address.setSelection(0);
        }

        et_destination_address.requestFocus();
        try {
            getLatlanfromAddress(URLEncoder.encode(source_address, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        btn_search.setEnabled(false);
        btn_search.setBackgroundColor(getResources().getColor(R.color.light_grey));


        return view;
    }

    private void getFavouritePlaces() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        AndyUtils.showSimpleProgressDialog(activity, "", false);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_SAVED_PLACES);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.GET_SAVED_PLACES, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final Place place = PlaceAutocomplete.getPlace(activity, data);


                /*gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),
                        15));*/

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!d_click) {
                            et_source_address.setText(place.getAddress());
                            BaseMapFragment.pic_latlan = place.getLatLng();
                            if (null != PickUpMarker) {
                                PickUpMarker.setPosition(place.getLatLng());
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),
                                        16));
                            }
                            isClicked=false;

                        } else {
                            et_destination_address.setText(place.getAddress());
                            des_latLng = place.getLatLng();

                            BaseMapFragment.d_address = place.getAddress().toString();
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),
                                    16));

                            if (null != getActivity() && isAdded()) {
                                btn_search.setEnabled(true);
                                btn_search.setBackgroundColor(getResources().getColor(R.color.black));
                            }

                            if (DropMarker == null) {
                                MarkerOptions markerOpt = new MarkerOptions();
                                markerOpt.position(place.getLatLng());
                                markerOpt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.drop_location));
                                DropMarker = googleMap.addMarker(markerOpt);
                            } else {
                                DropMarker.setPosition(place.getLatLng());
                            }
                            isClicked=false;
                        }

                    }
                });

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(activity, data);
                // TODO: Handle the error.
                Log.i("mahi", status.getStatusMessage());
                isClicked=false;

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                isClicked=false;
            }
        }
    }

    private void showSearchMap(String place, String d_place, Bundle savedInstanceState) {


        final Dialog searchMap = new Dialog(activity, R.style.DialogSlideAnim_leftright_Fullscreen);
        searchMap.requestWindowFeature(Window.FEATURE_NO_TITLE);
        searchMap.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_black)));
        searchMap.setCancelable(true);
        searchMap.setContentView(R.layout.search_map_dialog);
        MapView mMapView = (MapView) searchMap.findViewById(R.id.search_map);
        pin_marker = (ImageView) searchMap.findViewById(R.id.pin_location);

        final Button btn_done = (Button) searchMap.findViewById(R.id.btn_done);
        ImageButton search_dai_back = (ImageButton) searchMap.findViewById(R.id.search_dai_back);
        et_source_dia_address = (AutoCompleteTextView) searchMap.findViewById(R.id.et_source_dia_address);
        et_source_dia_address.setText(place);
        btn_done.requestFocus();


        et_destination_dia_address = (AutoCompleteTextView) searchMap.findViewById(R.id.et_destination_dia_address);
        final PlacesAutoCompleteAdapter S_placesadapter = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        et_source_dia_address.setAdapter(S_placesadapter);
        et_source_dia_address.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        final PlacesAutoCompleteAdapter D_placesadapter = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        et_destination_dia_address.setAdapter(D_placesadapter);
        et_destination_dia_address.setText(d_place);
        et_destination_dia_address.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        et_source_dia_address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                s_click = true;
                d_click = false;

                AndyUtils.hideKeyBoard(activity);
                pin_marker.setImageResource(R.mipmap.pickup_location);
                return false;
            }
        });


        et_destination_dia_address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                s_click = false;
                d_click = true;
                //AndyUtils.hideKeyBoard(activity);
                pin_marker.setImageResource(R.mipmap.drop_location);
                return false;
            }
        });


        search_dai_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_source_address.setText(et_source_dia_address.getText().toString());
                et_destination_address.setText(et_destination_dia_address.getText().toString());
                try {
                    getLatlanfromAddress(URLEncoder.encode(et_source_dia_address.getText().toString(), "utf-8"));
                    if (!(et_destination_dia_address.getText().toString().length() == 0)) {
                        getLocationforDest(URLEncoder.encode(et_destination_dia_address.getText().toString(), "utf-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                searchMap.dismiss();
            }
        });

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_source_address.setText(et_source_dia_address.getText().toString());
                et_destination_address.setText(et_destination_dia_address.getText().toString());
                try {
                    getLatlanfromAddress(URLEncoder.encode(et_source_dia_address.getText().toString(), "utf-8"));
                    if (!(et_destination_dia_address.getText().toString().length() == 0)) {
                        getLocationforDest(URLEncoder.encode(et_destination_dia_address.getText().toString(), "utf-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                searchMap.dismiss();
            }
        });


        et_source_dia_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                et_source_dia_address.setSelection(0);
                //LatLng latLng = getLocationFromAddress(activity, et_source_address.getText().toString());
                // AndyUtils.hideKeyBoard(activity);
                final String selectedSourcePlace = S_placesadapter.getItem(i);
                try {
                    getLatlanfromAddress(URLEncoder.encode(selectedSourcePlace, "utf-8"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }
        });

        et_destination_dia_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                et_destination_dia_address.setSelection(0);
                //LatLng latLng = getLocationFromAddress(activity, et_source_address.getText().toString());
                AndyUtils.hideKeyBoard(activity);
                final String selectedplace = D_placesadapter.getItem(position);
                try {
                    getLocationforDest(URLEncoder.encode(selectedplace, "utf-8"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;
                gMap.getUiSettings().setMyLocationButtonEnabled(true);
                gMap.getUiSettings().setMapToolbarEnabled(true);
                gMap.getUiSettings().setScrollGesturesEnabled(true);
                //gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
               /* MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(
                        activity, R.raw.maps_style);
                gMap.setMapStyle(style);*/

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

                gMap.setMyLocationEnabled(true);


                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLan,
                        15));

                gMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        getCompleteAddressString(cameraPosition.target);
                        btn_done.requestFocus();

                    }
                });

            }
        });

        if (d_click == true) {
            pin_marker.setImageResource(R.mipmap.drop_location);
        } else {
            pin_marker.setImageResource(R.mipmap.pickup_location);

        }
        searchMap.show();
    }

    private void getCompleteAddressString(LatLng target) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ADDRESS_API_BASE + target.latitude + "," + target.longitude + "&key=" + Const.GOOGLE_API_KEY);

        Log.d("mahi", "map for address" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.ADDRESS_API_BASE, this);
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

    private void getLatlanfromAddress(String selectedSourcePlace) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.LOCATION_API_BASE + selectedSourcePlace + "&key=" + Const.GOOGLE_API_KEY);

        Log.d("mahi", "map for s_loc" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.LOCATION_API_BASE_SOURCE, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        activity.currentFragment = Const.SEARCH_FRAGMENT;
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {

            case Const.ServiceCode.GET_SAVED_PLACES:
                AndyUtils.removeProgressDialog();
                Log.d("mahi", "res saved places" + response);
                if (response != null) {
                    try {
                        JSONObject favobj = new JSONObject(response);
                        if (favobj.getString("success").equals("true")) {
                            //  fav_progress_bar.setVisibility(View.GONE);
                            if (favouritesArrayList != null) {
                                favouritesArrayList.clear();
                            }
                            JSONArray hisArray = favobj.getJSONArray("data");
                            showFavouritesDialog();
                            parseFavourites(hisArray);

                        }
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



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Const.ServiceCode.ADDRESS_API_BASE:
                if (null != response) {
                    try {
                        JSONObject job = new JSONObject(response);
                        JSONArray jarray = job.optJSONArray("results");
                        final JSONObject locObj = jarray.getJSONObject(0);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!d_click) {
                                    et_source_dia_address.setText(locObj.optString("formatted_address"));

                                } else {
                                    BaseMapFragment.d_address = locObj.optString("formatted_address");

                                }

                            }
                        });

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
                        des_latLng = new LatLng(lat, lan);
                        if (null != gMap) {
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(des_latLng,
                                    15));
                        }
                        if (null != getActivity() && isAdded()) {
                            btn_search.setEnabled(true);
                            btn_search.setBackgroundColor(getResources().getColor(R.color.black));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;

        }

    }

    private void parseFavourites(JSONArray hisArray) {
        if (hisArray.length() > 0) {
            for (int i = 0; i < hisArray.length(); i++) {
                JSONObject obj = null;
                try {
                    obj = hisArray.getJSONObject(i);
                    Favourites fav = new Favourites();
                    fav.setFav_Id(obj.getString("id"));
                    fav.setFav_Address(obj.getString("address"));
                    fav.setFav_Latitude(obj.getString("latitude"));
                    fav.setFav_Longitude(obj.getString("longitude"));
                    fav.setFav_Name(obj.getString("favourite_name"));
                    favouritesArrayList.add(fav);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (favouritesArrayList != null) {
                favouritesAdapter = new FavouritesDisplayAdapter(activity, favouritesArrayList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
                fav_lv.setLayoutManager(mLayoutManager);
                fav_lv.setItemAnimator(new DefaultItemAnimator());
                fav_lv.setAdapter(favouritesAdapter);

            }
        }
    }

    private void showFavouritesDialog() {
        final Dialog addFavDialog = new Dialog(activity, R.style.DialogThemeforview);
        addFavDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addFavDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.fade_drawable));
        addFavDialog.setCancelable(false);
        addFavDialog.setContentView(R.layout.display_fav_layout);
        close_popup_fav=(ImageView)addFavDialog.findViewById(R.id.close_popup_fav);
        close_popup_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFavDialog.dismiss();
            }
        });
        fav_lv=(RecyclerView)addFavDialog.findViewById(R.id.favRecyc);
        fav_lv.addOnItemTouchListener(new RecyclerLongPressClickListener(activity, fav_lv, new RecyclerLongPressClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //    showDetailedHistroy(historylst.get(position));
                Log.e("asher","fav item address "+favouritesArrayList.get(position).getFav_Address());
                Log.e("asher","fav item lat "+favouritesArrayList.get(position).getFav_Latitude());
                Log.e("asher","fav item lng "+favouritesArrayList.get(position).getFav_Longitude());


            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        addFavDialog.show();

    }

    @Override
    public void onLocationReceived(LatLng latlong) {

    }

    @Override
    public void onLocationReceived(Location location) {

    }

    @Override
    public void onConntected(Bundle bundle) {

    }

    @Override
    public void onConntected(Location location) {
        if (null != location) {
            LatLng latlong = new LatLng(location.getLatitude(), location.getLongitude());
            currentLatLan = latlong;
            if (null != gMap) {
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLan,
                        15));
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        gMap = null;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
          /*TO clear all views */
        ViewGroup mContainer = (ViewGroup) getActivity().findViewById(R.id.content_frame);
        mContainer.removeAllViews();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        AndyUtils.removeProgressDialog();
        if (googleMap != null) {
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
           /* MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(
                    activity, R.raw.maps_style);
            googleMap.setMapStyle(style);*/

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(BaseMapFragment.pic_latlan)
                    .zoom(16).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (null != googleMap) {
                MarkerOptions markerOpt = new MarkerOptions();
                markerOpt.position(BaseMapFragment.pic_latlan);
                markerOpt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.pickup_location));
                PickUpMarker = googleMap.addMarker(markerOpt);

            }
            googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    if (d_click) {
                        //pin_drop_location.setVisibility(View.VISIBLE);
                    }
                }
            });
            googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    if (d_click) {
                        des_latLng = googleMap.getCameraPosition().target;
                         getCompleteAddressString(des_latLng);
                        //  pin_drop_location.setVisibility(View.VISIBLE);
                        if (null != DropMarker) {
                            SmoothMoveMarker.animateMarker(DropMarker, googleMap.getCameraPosition().target, false, googleMap);
                        }

                    }
                }
            });
            googleMap.setTrafficEnabled(true);
        }
    }
}
