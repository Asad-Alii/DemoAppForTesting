package com.alqaswa.user.Fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurelhubert.simpleratingbar.SimpleRatingBar;
import com.bumptech.glide.Glide;
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
import com.alqaswa.user.Adapter.PaymentModeAdapter;
import com.alqaswa.user.Adapter.TaxiAdapter;
import com.alqaswa.user.AdapterCallback;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Location.LocationHelper;
import com.alqaswa.user.Models.NearByDrivers;
import com.alqaswa.user.Models.Payments;
import com.alqaswa.user.Models.RequestDetail;
import com.alqaswa.user.Models.TaxiTypes;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Commonutils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.ItemClickSupport;
import com.alqaswa.user.Utils.ParseContent;
import com.alqaswa.user.Utils.PreferenceHelper;
import com.alqaswa.user.Utils.RecyclerLongPressClickListener;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Created by user on 2/3/2017.
 */

public class RequestMapFragment extends BaseMapFragment implements LocationHelper.OnLocationReceived, AsyncTaskCompleteListener, OnMapReadyCallback, AdapterCallback {

    private GoogleMap googleMap;
    private Bundle mBundle;
    SupportMapFragment user_request_map;
    private View view;
    private LocationHelper locHelper;
    private Location myLocation;
    private LatLng latlong;
    private static final int DURATION = 2000;
    private TextView btn_request_cab, tv_no_seats, tv_estimate_fare, tv_cashtype, tv_total_dis, tv_promocode;
    private static Marker pickup_marker, drop_marker;
    MarkerOptions pickup_opt;
    private static Polyline poly_line;
    private RelativeLayout lay_payment;
    public static ImageButton btn_mylocation;
    public static RelativeLayout request_layout;
    private ArrayList<TaxiTypes> typesList;
    private TaxiAdapter taxiAdapter;
    private RecyclerView lst_vehicle;
    private ArrayList<NearByDrivers> driverslatlngs = new ArrayList<>();
    private String nearest_eta = "--";
    private HashMap<Marker, Integer> markermap = new HashMap<>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private int marker_position;
    private String service_id = "1";
    private ProgressBar pbfareProgress;
    private Dialog req_load_dialog;
    private LatLng driverlatlan;
    Handler providerhandler;
    Handler checkreqstatus;
    private ArrayList<Payments> paymentlst;
    private String pickup_add = "", tax_price = "", promoCode = "";
    RelativeLayout mapLayout;
    private String base_price = "", min_price = "", booking_fee = "", currency = "", distance_unit = "";
    private List<LatLng> listLatLng = new ArrayList<>();
    private Polyline blackPolyLine, greyPolyLine;
    private Dialog promo_dialog;
    private EditText et_promocode;
    private LinearLayout promo_layout;
    private ProgressBar load_progress;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.request_fragment, container,
                false);

        btn_mylocation = (ImageButton) view.findViewById(R.id.btn_mylocation);
        request_layout = (RelativeLayout) view.findViewById(R.id.req_cabs);
        btn_request_cab = (TextView) view.findViewById(R.id.btn_request_cab);
        tv_cashtype = (TextView) view.findViewById(R.id.tv_cashtype);
        tv_no_seats = (TextView) view.findViewById(R.id.tv_no_seats);
        lst_vehicle = (RecyclerView) view.findViewById(R.id.lst_vehicle);
        lay_payment = (RelativeLayout) view.findViewById(R.id.lay_payment);
        promo_layout = (LinearLayout) view.findViewById(R.id.promo_layout);
        tv_promocode = (TextView) view.findViewById(R.id.tv_promocode);
        load_progress = (ProgressBar) view.findViewById(R.id.load_progress);

        btn_mylocation.setOnClickListener(this);
        btn_request_cab.setOnClickListener(this);
        lay_payment.setOnClickListener(this);
        promo_layout.setOnClickListener(this);

        user_request_map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.user_request_map);

        if (null != user_request_map) {
            user_request_map.getMapAsync(this);
        }

        mapLayout = (RelativeLayout) view.findViewById(R.id.map_lay);

        ItemClickSupport.addTo(lst_vehicle)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        new PreferenceHelper(activity).putRequestType(typesList.get(position).getId());
                        btn_request_cab.setText(getResources().getString(R.string.txt_reqst) + " " + typesList.get(position).getTaxitype());
                        tv_no_seats.setText("1-" + " " + typesList.get(position).getTaxi_seats());
                        taxiAdapter.OnItemClicked(position);
                        taxiAdapter.notifyDataSetChanged();
                        getAllProviders(latlong);
                        new PreferenceHelper(activity).putTaxi_name(typesList.get(position).getTaxitype());

                    }
                });
        lst_vehicle.addOnItemTouchListener(new RecyclerLongPressClickListener(activity, lst_vehicle, new RecyclerLongPressClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onLongItemClick(View view, int position) {
                service_id = typesList.get(position).getId();
                findDistanceAndTime(pic_latlan, drop_latlan);
                showfareestimate(typesList.get(position).getTaxitype(), typesList.get(position).getTaxi_price_distance(), typesList.get(position).getTaxi_price_min(), typesList.get(position).getTaxiimage(), typesList.get(position).getTaxi_seats(), typesList.get(position).getBasefare());

            }
        }));


        return view;
    }

    private void setuptypesView() {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        lst_vehicle.setLayoutManager(mLayoutManager);
        lst_vehicle.setItemAnimator(new DefaultItemAnimator());
        lst_vehicle.addItemDecoration(new SpacesItemDecoration(size.x / 20));

    }


    @Override
    public void onMapReady(GoogleMap mgoogleMap) {
        googleMap = mgoogleMap;
        if (googleMap != null) {
            googleMap.setTrafficEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
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

            googleMap.setMyLocationEnabled(false);


            if (pic_latlan != null) {

                pickup_opt = new MarkerOptions();
                pickup_opt.position(pic_latlan);
                pickup_opt.title(activity.getResources().getString(R.string.txt_current_loc));
                pickup_opt.anchor(0.5f, 0.5f);
                pickup_opt.zIndex(1);
                pickup_opt.icon(BitmapDescriptorFactory
                        .fromBitmap(getMarkerBitmapFromView(nearest_eta)));
                pickup_marker = googleMap.addMarker(pickup_opt);

                btn_mylocation.setVisibility(View.GONE);

            }

            if (drop_latlan != null) {
                MarkerOptions opt = new MarkerOptions();
                opt.position(drop_latlan);
                opt.title(activity.getResources().getString(R.string.txt_drop_loc));
                opt.anchor(0.5f, 0.5f);
                opt.icon(BitmapDescriptorFactory
                        .fromResource(R.mipmap.drop_location));
                drop_marker = googleMap.addMarker(opt);
            }

          /*  if (pickup_marker != null && drop_marker != null) {
                fitmarkers_toMap();
            }*/


        }

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                           @Override
                                           public View getInfoWindow(Marker marker) {
                                               View vew = null;
                                               if (drop_marker != null) {
                                                   if (marker.getId().equals(drop_marker.getId())) {
                                                       vew = activity.getLayoutInflater().inflate(R.layout.info_window_dest, null);
                                                     /*  new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                           @Override
                                                           public void run() {

                                                               if (drop_marker != null) {
                                                                   drop_marker.showInfoWindow();
                                                               }
                                                           }
                                                       });*/
                                                   } else if (marker.getId().equals(pickup_marker.getId())) {
                                                       pickup_marker.hideInfoWindow();
                                                      /* vew = activity.getLayoutInflater().inflate(R.layout.eta_info_window, null);
                                                       final TextView txt_eta_marker = (TextView) vew.findViewById(R.id.txt_eta);


                                                       txt_eta_marker.setText(nearest_eta);*/
/*

                                                       new CountDownTimer(2000, 1000) {

                                                           public void onTick(long millisUntilFinished) {

                                                           }

                                                           public void onFinish() {
                                                               if (driverlatlan != null && pic_latlan != null) {

                                                               }
                                                               if (pickup_marker != null) {
                                                                   pickup_marker.showInfoWindow();

                                                               }
                                                           }

                                                       }.start();*/
/*
                                                       new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                           @Override
                                                           public void run() {

                                                               if (pickup_marker != null) {
                                                                   pickup_marker.showInfoWindow();

                                                               }
                                                           }
                                                       });*/
                                                  /*     Thread t = new Thread() {

                                                           @Override
                                                           public void run() {
                                                               try {
                                                                   while (!isInterrupted()) {
                                                                       Thread.sleep(4000);
                                                                       runOnUiThread(new Runnable() {
                                                                           @Override
                                                                           public void run() {
                                                                               // update TextView here!

                                                                           }
                                                                       });
                                                                   }
                                                               } catch (InterruptedException e) {
                                                               }
                                                           }
                                                       };

                                                       t.start();*/

                                                       // final TextView txt_location_marker = (TextView) vew.findViewById(R.id.txt_location);
                                                   } else {
                                                       vew = activity.getLayoutInflater().inflate(R.layout.driver_info_window, null);
                                                       TextView txt_driver_name = (TextView) vew.findViewById(R.id.driver_name);
                                                       if (driverslatlngs.size() > 0) {
                                                           txt_driver_name.setText(driverslatlngs.get(marker_position).getDriver_name());
                                                           SimpleRatingBar driver_rate = (SimpleRatingBar) vew.findViewById(R.id.driver_rate);
                                                           driver_rate.setRating(driverslatlngs.get(marker_position).getDriver_rate());
                                                       }
                                                   }
                                               } else {
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

                if (pickup_marker != null) {
                    pickup_marker.hideInfoWindow();
                    if (!marker.getId().equals(pickup_marker.getId()) && !marker.getId().equals(drop_marker.getId())) {
                        marker_position = markermap.get(marker);
                    }
                } else {
                    marker_position = markermap.get(marker);
                }


                return false;
            }
        });


        if (pickup_marker != null && drop_marker != null) {
            fitmarkers_toMap();
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

    @Override
    public void onMethodCallback(String id, String taxitype, String taxi_price_distance, String taxi_price_min, String taxiimage, String taxi_seats, String basefare) {
        service_id = id;
        findDistanceAndTime(pic_latlan, drop_latlan);
        showfareestimate(taxitype, taxi_price_distance, taxi_price_min, taxiimage, taxi_seats, basefare);

    }


    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(space, 2, space, 2);

            // Add top margin only for the first item to avoid double space between items
           /* if(parent.getChildPosition(view) == 0)
                outRect.top = space;*/
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        typesList = new ArrayList<TaxiTypes>();
        paymentlst = new ArrayList<Payments>();
        providerhandler = new Handler();
        checkreqstatus = new Handler();


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

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        locHelper = new LocationHelper(activity);
        locHelper.setLocationReceivedLister(this);
        setuptypesView();
        getTypesforhome();

        if (new PreferenceHelper(activity).getRequestId() == Const.NO_REQUEST) {
            startgetProvider();
        }


        if (pic_latlan != null && drop_latlan != null) {

            getDirections(pic_latlan.latitude, pic_latlan.longitude, drop_latlan.latitude, drop_latlan.longitude);
            findDistanceAndTimeforTypes(pic_latlan, drop_latlan);

        }
        //updatepayment("card");
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


    @Override
    public void onLocationReceived(LatLng latlong) {
        if (latlong != null) {
            /*googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong,
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

        if (location != null && googleMap != null) {
            LatLng currentlatLang = new LatLng(location.getLatitude(), location.getLongitude());
            /*googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlatLang,
                    15));*/
            pickup_add = getCompleteAddressString(currentlatLang.latitude, currentlatLang.longitude);
        }
    }


    private Bitmap getMarkerBitmapFromView(String eta) {
        String time = eta.replaceAll("\\s+", "\n");
        View customMarkerView = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.eta_info_window, null);
        TextView markertext = (TextView) customMarkerView.findViewById(R.id.txt_eta);
        markertext.setText(time);
        markertext.setAllCaps(true);
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


    public void drawPath(String result) {


        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            PolylineOptions options = new PolylineOptions().width(8).color(getResources().getColor(R.color.black)).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    options.add(point);
                }
            }

            this.listLatLng.addAll(list);

            if (googleMap != null) {
                blackPolyLine = googleMap.addPolyline(options);
                poly_line = blackPolyLine;
            }


            PolylineOptions greyOptions = new PolylineOptions();
            greyOptions.width(8);
            greyOptions.color(Color.GRAY);
            greyPolyLine = googleMap.addPolyline(greyOptions);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Log.e("asher", "inside animate polyline ");
                animatePolyLine();
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


    private void fitmarkers_toMap() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
        builder.include(pickup_marker.getPosition());
        builder.include(drop_marker.getPosition());

        LatLngBounds bounds = builder.build();


        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (height * 0.19); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        if (null != googleMap) {
            googleMap.moveCamera(cu);
        }
       /* if (pic_latlan != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pic_latlan)
                    .zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        }*/

        //pickup_marker.showInfoWindow();

        request_layout.setVisibility(View.VISIBLE);
        //findDistanceAndTimeforeta(pic_latlan, driverlatlan);
        //findDistanceAndTime();

    }

    private void findDistanceAndTimeforeta(LatLng pic_latlan, LatLng latlan) {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.GOOGLE_MATRIX_URL + Const.Params.ORIGINS + "="
                + String.valueOf(pic_latlan.latitude) + "," + String.valueOf(pic_latlan.longitude) + "&" + Const.Params.DESTINATION + "="
                + String.valueOf(latlan.latitude) + "," + String.valueOf(latlan.longitude) + "&" + Const.Params.MODE + "="
                + "driving" + "&" + Const.Params.LANGUAGE + "="
                + "en-EN" + "&" + "key=" + Const.GOOGLE_API_KEY + "&" + Const.Params.SENSOR + "="
                + String.valueOf(false));
        Log.e("mahi", "distance api" + map);
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.GOOGLE_MATRIX_ETA, this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_mylocation:
/*
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong,
                        15));*/
                break;
            case R.id.btn_request_cab:
                RequestTaxi();
                break;
            case R.id.lay_payment:
                getPaymentsmethods();
                break;
            case R.id.promo_layout:
                showpromo();
                break;


        }

    }

    private void getPaymentsmethods() {
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_PAYMENT_MODES + Const.Params.ID + "="
                + new PreferenceHelper(activity).getUserId() + "&" + Const.Params.TOKEN + "="
                + new PreferenceHelper(activity).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.GET, map, Const.ServiceCode.GET_PAYMENT_MODES, this);


    }

    private void RequestTaxi() {
        showreqloader();
        startgetreqstatus();
        if (!AndyUtils.isNetworkAvailable(activity)) {

            return;
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.REQUEST_TAXI);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        if (pic_latlan != null) {
            map.put(Const.Params.S_LATITUDE, String.valueOf(pic_latlan.latitude));
            map.put(Const.Params.S_LONGITUDE, String.valueOf(pic_latlan.longitude));
        }
        if (drop_latlan != null) {
            map.put(Const.Params.D_LONGITUDE, String.valueOf(drop_latlan.longitude));
            map.put(Const.Params.D_LATITUDE, String.valueOf(drop_latlan.latitude));
        }
        map.put(Const.Params.SERVICE_TYPE, new PreferenceHelper(activity).getRequestType());
        map.put(Const.Params.S_ADDRESS, s_address);
        map.put(Const.Params.D_ADDRESS, d_address);
        map.put(Const.Params.REQ_STATUS_TYPE, "1");
        map.put(Const.Params.PROMOCODE, promoCode);

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.REQUEST_TAXI,
                this);
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return strAdd;
    }

    @Override
    public void onResume() {

        super.onResume();

        activity.currentFragment = Const.REQUEST_FRAGMENT;

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

            providerhandler.postDelayed(this, 4000);
        }
    };

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Log.e("mahi", "on destory view is calling");
        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.user_request_map);
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

        if (req_load_dialog != null && req_load_dialog.isShowing()) {
            req_load_dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopCheckingforproviders();
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    @Override
    public void onTaskCompleted(final String response, int serviceCode) {

        switch (serviceCode) {
            case Const.ServiceCode.GOOGLE_DIRECTION_API:

                if (response != null) {
                    drawPath(response);

                }
                break;
            case Const.ServiceCode.HOMETAXI_TYPE:
                Log.d("mahi", "service_list" + response);
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
                                new PreferenceHelper(activity).putRequestType(typesList.get(0).getId());
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
                                    taxiAdapter = new TaxiAdapter(activity, typesList, this);
                                    lst_vehicle.setAdapter(taxiAdapter);
                                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(activity, getResources().getIdentifier("layout_animation_from_right", "anim", activity.getPackageName()));
                                    lst_vehicle.setLayoutAnimation(animation);
                                    taxiAdapter.notifyDataSetChanged();
                                    lst_vehicle.scheduleLayoutAnimation();
                                    load_progress.setVisibility(View.GONE);

                                    if (typesList.size() > 0) {
                                        btn_request_cab.setText(getResources().getString(R.string.txt_reqst) + " " + typesList.get(0).getTaxitype());
                                        tv_no_seats.setText("1-" + " " + typesList.get(0).getTaxi_seats());
                                        new PreferenceHelper(activity).putTaxi_name(typesList.get(0).getTaxitype());

                                    }
                                }


                            }

                        } else {

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
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

                        }


                        if (driverslatlngs.size() > 0) {

                            driverlatlan = driverslatlngs.get(0).getLatlan();

                            if (driverlatlan != null && pic_latlan != null) {
                                findDistanceAndTimeforeta(pic_latlan, driverlatlan);
                            }

                            if (null != markers && markers.size() > 0) {
                                for (Marker marker : markers) {
                                    marker.remove();
                                }
                            }
                            final Marker[] driver_marker = new Marker[1];
                            for (int i = 0; i < driverslatlngs.size(); i++) {
                           /* CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                                    driverslatlngs.get(i), 15);*/
                                // Log.d("mahi","markers size"+driverslatlngs.get(i).toString());

                                final MarkerOptions currentOption = new MarkerOptions();
                                currentOption.position(driverslatlngs.get(i).getLatlan());
                                currentOption.title(driverslatlngs.get(i).getDriver_name());
                                currentOption.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_booking_lux_map_topview));
                                if (googleMap != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {

                                            driver_marker[0] = googleMap.addMarker(currentOption);

                                        }
                                    });
                                    markers.add(driver_marker[0]);
                                    //googleMap.animateCamera(location);
                                    markermap.put(driver_marker[0], i);
                                    btn_request_cab.setEnabled(true);
                                    /*googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong,
                                            15));*/
                                    btn_request_cab.setText(getResources().getString(R.string.txt_reqst) + " " + new PreferenceHelper(activity).getTaxi_name());
                                    btn_request_cab.setTextColor(activity.getResources().getColor(R.color.white));
                                    btn_request_cab.setBackgroundColor(activity.getResources().getColor(R.color.black));
                                    // setUpMap();
                                }
                            }


                        } else {

                            for (Marker marker : markers) {
                                marker.remove();
                            }
                            markers.clear();
                            // Toast.makeText(activity, activity.getString(R.string.txt_drivers_error), Toast.LENGTH_SHORT).show();
                            btn_request_cab.setEnabled(false);
                            btn_request_cab.setText(getResources().getString(R.string.btn_no_driver));
                           /* if (null != googleMap) {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong,
                                        14));
                            }*/
                            btn_request_cab.setTextColor(activity.getResources().getColor(R.color.deep_grey));
                            btn_request_cab.setBackgroundColor(activity.getResources().getColor(R.color.main_color));
                            if (pickup_opt != null && pickup_marker != null) {
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        pickup_marker.setIcon((BitmapDescriptorFactory
                                                .fromBitmap(getMarkerBitmapFromView("--"))));
                                    }
                                });

                            }

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
                        getFare(String.valueOf(trip_dis), dur, service_id);
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
            case Const.ServiceCode.GOOGLE_MATRIX_ETA:
                Log.d("mahi", "google distance api" + response);
                if (response != null) {
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
                            final String duration = durationObject.getString("text");

                            nearest_eta = duration;
                            if (pickup_opt != null && pickup_marker != null) {
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        pickup_marker.setIcon((BitmapDescriptorFactory
                                                .fromBitmap(getMarkerBitmapFromView(duration))));

                                    }
                                });

                            }

                            // setUpMap();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
            case Const.ServiceCode.PAYMENT_MODE_UPDATE:
                if (response != null) {
                    try {
                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {
                            // Commonutils.showtoast("Payment Option Updated Successfully!", activity);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case Const.ServiceCode.REQUEST_TAXI:
                Log.d("mahi", "create req_response" + response);
                try {
                    JSONObject job1 = new JSONObject(response);
                    if (job1.getString("success").equals("true")) {

                    } else {
                        // startgetProvider();
                        if (req_load_dialog != null && req_load_dialog.isShowing()) {
                            req_load_dialog.dismiss();
                            stopCheckingforstatus();
                        }
                        String error = job1.getString("error");
                        Commonutils.showtoast(error, activity);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Const.ServiceCode.CANCEL_CREATE_REQUEST:
                Log.d("mahi", "cancel req_response" + response);

                if (req_load_dialog != null && req_load_dialog.isShowing()) {
                    req_load_dialog.dismiss();
                }
                break;
            case Const.ServiceCode.VALIDATE_PROMO:
                Log.d("mahi", "validate promo response" + response);
                AndyUtils.removeProgressDialog();
                if (response != null) {
                    try {
                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {
                            AndyUtils.showShortToast(job1.optString("error_message"), activity);
                            promoCode = et_promocode.getText().toString();
                            promo_dialog.dismiss();
                            tv_promocode.setText(getResources().getString(R.string.txt_promo_success));
                            tv_promocode.setTextColor(getResources().getColor(R.color.dark_green));
                            promo_layout.setEnabled(false);

                        } else {
                            AndyUtils.showShortToast(job1.optString("error_message"), activity);
                            et_promocode.requestFocus();
                            promo_layout.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Const.ServiceCode.GET_PAYMENT_MODES:
                Log.d("mahi", "payment response" + response);

                if (response != null) {
                    try {
                        JSONObject job1 = new JSONObject(response);
                        if (job1.getString("success").equals("true")) {
                            paymentlst.clear();
                            JSONArray paymentarray = job1.getJSONArray("payment_modes");
                            if (paymentarray.length() > 0) {
                                for (int i = 0; i < paymentarray.length(); i++) {
                                    Payments paymnts = new Payments();
                                    paymnts.setPayment_name(paymentarray.getString(i));
                                    paymentlst.add(paymnts);
                                }
                            }
                            if (paymentlst != null && isAdded()) {
                                showpaymentoptionlst(paymentlst);
                            }


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
                            if (req_load_dialog != null && req_load_dialog.isShowing()) {
                                req_load_dialog.dismiss();
                                Commonutils.showtoast(getResources().getString(R.string.txt_no_provider_error), activity);
                                stopCheckingforstatus();
                            }

                            break;

                        case Const.IS_ACCEPTED:
                            bundle.putSerializable(Const.REQUEST_DETAIL,
                                    requestDetail);
                            bundle.putInt(Const.DRIVER_STATUS,
                                    Const.IS_ACCEPTED);
                            if (!activity.currentFragment.equals(Const.TRAVEL_MAP_FRAGMENT)) {
                                stopCheckingforproviders();
                                stopCheckingforstatus();
                                if (req_load_dialog != null && req_load_dialog.isShowing()) {
                                    req_load_dialog.dismiss();
                                }
                                travalfragment.setArguments(bundle);
                                activity.addFragment(travalfragment, false, Const.TRAVEL_MAP_FRAGMENT,
                                        true);

                            }


                            BaseMapFragment.drop_latlan = null;
                            BaseMapFragment.pic_latlan = null;
                            BaseMapFragment.s_address = "";
                            BaseMapFragment.d_address = "";

                            break;
                        case Const.IS_DRIVER_DEPARTED:

                            break;
                        case Const.IS_DRIVER_ARRIVED:

                            break;
                        case Const.IS_DRIVER_TRIP_STARTED:

                            break;
                        case Const.IS_DRIVER_TRIP_ENDED:


                            break;
                        case Const.IS_DRIVER_RATED:

                            break;
                        default:
                            break;

                    }
                }
        }
    }

    private void showpaymentoptionlst(final ArrayList<Payments> paymentlst) {

        final Dialog pay_dialog = new Dialog(activity, R.style.DialogThemeforview);
        pay_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pay_dialog.setCancelable(true);
        pay_dialog.setContentView(R.layout.select_payment);
        ImageButton btn_pay_viewcancel = (ImageButton) pay_dialog.findViewById(R.id.btn_pay_viewcancel);
        RecyclerView lv_cards = (RecyclerView) pay_dialog.findViewById(R.id.lv_cards);
        TextView tv_payment_title = (TextView) pay_dialog.findViewById(R.id.tv_payment_title);
        PaymentModeAdapter payadapter = new PaymentModeAdapter(activity, paymentlst);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        lv_cards.setLayoutManager(mLayoutManager);
        lv_cards.setItemAnimator(new DefaultItemAnimator());
        lv_cards.setAdapter(payadapter);

        if (new PreferenceHelper(activity).getPaymentMode().equals("cod")) {
            tv_payment_title.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_cash));
        } else if (new PreferenceHelper(activity).getPaymentMode().equals("paypal")) {
            tv_payment_title.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_paypal));
        } else if (new PreferenceHelper(activity).getPaymentMode().equals("walletbay")) {
            tv_payment_title.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_wallet));
        } else {
            tv_payment_title.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_card));
        }

        lv_cards.addOnItemTouchListener(new RecyclerLongPressClickListener(activity, lv_cards, new RecyclerLongPressClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                updatepayment(paymentlst.get(position).getPayment_name());
                new PreferenceHelper(activity).putPaymentMode(paymentlst.get(position).getPayment_name());

                if (new PreferenceHelper(activity).getPaymentMode().equals("cod")) {
                    tv_cashtype.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_cash));
                } else if (new PreferenceHelper(activity).getPaymentMode().equals("paypal")) {
                    tv_cashtype.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_paypal));
                } else if (new PreferenceHelper(activity).getPaymentMode().equals("walletbay")) {
                    tv_cashtype.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_wallet));
                } else {
                    tv_cashtype.setText(getResources().getString(R.string.txt_selected_type) + " " + getResources().getString(R.string.txt_card));
                }

                pay_dialog.dismiss();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        btn_pay_viewcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pay_dialog.dismiss();
            }
        });
        pay_dialog.show();
    }

    private void updatepayment(String payment_name) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.PAYMENT_MODE_UPDATE);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());

        map.put(Const.Params.PAYMENT_MODE, payment_name);

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.PAYMENT_MODE_UPDATE,
                this);
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


    private void showreqloader() {
        stopCheckingforproviders();

        req_load_dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        req_load_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        req_load_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_black)));
        req_load_dialog.setCancelable(false);
        req_load_dialog.setContentView(R.layout.request_loading);
        final RippleBackground rippleBackground = (RippleBackground) req_load_dialog.findViewById(R.id.content);
        TextView cancel_req_create = (TextView) req_load_dialog.findViewById(R.id.cancel_req_create);
        final TextView req_status = (TextView) req_load_dialog.findViewById(R.id.req_status);
        rippleBackground.startRippleAnimation();
        cancel_req_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                req_status.setText(getResources().getString(R.string.txt_canceling_req));
                cancel_create_req();
                new PreferenceHelper(activity).clearRequestData();
                stopCheckingforstatus();
                startgetProvider();
            }
        });


        req_load_dialog.show();
    }

    private void cancel_create_req() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.CANCEL_CREATE_REQUEST);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.CANCEL_CREATE_REQUEST,
                this);
    }


    private void animatePolyLine() {

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(1200);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                List<LatLng> latLngList = blackPolyLine.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * listLatLng.size()) / 100;

                if (initialPointSize < newPoints) {
                    latLngList.addAll(listLatLng.subList(initialPointSize, newPoints));
                    blackPolyLine.setPoints(latLngList);
                }


            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            Log.e("asher", "inside animate polyline listener");
            animator.addListener(polyLineAnimationListener);
            animator.start();
        }

    }

    Animator.AnimatorListener polyLineAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

            //addMarker(listLatLng.get(listLatLng.size()-1));
        }

        @Override
        public void onAnimationEnd(Animator animator) {

            List<LatLng> blackLatLng = blackPolyLine.getPoints();
            List<LatLng> greyLatLng = greyPolyLine.getPoints();

            greyLatLng.clear();
            greyLatLng.addAll(blackLatLng);
            blackLatLng.clear();

            blackPolyLine.setPoints(blackLatLng);
            greyPolyLine.setPoints(greyLatLng);

            blackPolyLine.setZIndex(2);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                animator.start();
                animatePolyLine();
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private void showpromo() {
        promo_dialog = new Dialog(activity, R.style.DialogSlideAnim_leftright);
        promo_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        promo_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        promo_dialog.setCancelable(true);
        promo_dialog.setContentView(R.layout.promo_layout);
        et_promocode = promo_dialog.findViewById(R.id.et_promocode);
        et_promocode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        TextView cencel_promocode = (TextView) promo_dialog.findViewById(R.id.cencel_promocode);
        TextView apply_promocode = (TextView) promo_dialog.findViewById(R.id.apply_promocode);
        apply_promocode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(et_promocode.getText().toString())) {
                    ApplyPromoCode(et_promocode.getText().toString());
                } else {
                    AndyUtils.showShortToast(getResources().getString(R.string.txt_error_promo), activity);
                    et_promocode.requestFocus();
                }
            }
        });

        cencel_promocode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promo_dialog.dismiss();
            }
        });

        promo_dialog.show();
    }

    private void ApplyPromoCode(String promoValue) {

        if (!AndyUtils.isNetworkAvailable(activity)) {
            return;
        }
        AndyUtils.showSimpleProgressDialog(activity, "", false);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.VALIDATE_PROMO);
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.PROMOCODE, promoValue);
        Log.d("mahi", map.toString());
        new VollyRequester(activity, Const.POST, map, Const.ServiceCode.VALIDATE_PROMO,
                this);

    }

}

