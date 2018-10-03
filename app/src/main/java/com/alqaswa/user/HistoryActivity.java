package com.alqaswa.user;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.alqaswa.user.Adapter.HistoryAdapter;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Models.History;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.PreferenceHelper;
import com.alqaswa.user.Utils.RecyclerLongPressClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 1/20/2017.
 */

public class HistoryActivity extends AppCompatActivity implements AsyncTaskCompleteListener {
    private Toolbar historymainToolbar;
    private ArrayList<History> historylst;
    private HistoryAdapter historyAdapter;
    private RecyclerView ride_lv;
    private ProgressBar histroy_progress_bar;
    private ImageButton history_back;
    private TextView histroy_empty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historylst = new ArrayList<History>();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_history);
        historymainToolbar = (Toolbar) findViewById(R.id.toolbar_history);


        setSupportActionBar(historymainToolbar);
        getSupportActionBar().setTitle(null);
        ride_lv = (RecyclerView) findViewById(R.id.ride_lv);
        histroy_progress_bar = (ProgressBar) findViewById(R.id.histroy_progress_bar);
        history_back = (ImageButton) findViewById(R.id.history_back);
        histroy_empty = (TextView)findViewById(R.id.histroy_empty);

        history_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ride_lv.addOnItemTouchListener(new RecyclerLongPressClickListener(this, ride_lv, new RecyclerLongPressClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showDetailedHistroy(historylst.get(position));

            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        getHistory();

    }


    private void getHistory() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            return;
        }
        AndyUtils.showSimpleProgressDialog(this,"",false);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_HISTORY);
        map.put(Const.Params.ID, new PreferenceHelper(this).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(this).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(this, Const.POST, map, Const.ServiceCode.GET_HISTORY,
                this);
    }


    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {
            case Const.ServiceCode.GET_HISTORY:
                AndyUtils.removeProgressDialog();
                Log.d("mahi", "res his" + response);
                if (response != null) {
                    try {
                        JSONObject hisobj = new JSONObject(response);
                        if (hisobj.getString("success").equals("true")) {
                            //histroy_progress_bar.setVisibility(View.GONE);
                            historylst.clear();
                            JSONArray hisArray = hisobj.getJSONArray("requests");
                            if (hisArray.length() > 0) {
                                for (int i = 0; i < hisArray.length(); i++) {
                                    JSONObject obj = hisArray.getJSONObject(i);
                                    History his = new History();
                                    his.setHistory_id(obj.getString("request_id"));
                                    his.setHistory_Dadd(obj.getString("d_address"));
                                    his.setHistory_Sadd(obj.getString("s_address"));
                                    his.setHistory_date(obj.getString("date"));
                                    his.setProvider_name(obj.getString("provider_name"));
                                    his.setHistory_type(obj.getString("taxi_name"));
                                    his.setHistory_total(obj.getString("total"));
                                    his.setHistory_picture(obj.getString("picture"));
                                    his.setMap_image(obj.getString("map_image"));
                                    his.setBase_price(obj.getString("base_price"));
                                    his.setDistance_travel(obj.getString("distance_travel"));
                                    his.setTotal_time(obj.getString("total_time"));
                                    his.setTax_price(obj.getString("tax_price"));
                                    his.setTime_price(obj.getString("time_price"));
                                    his.setDistance_price(obj.getString("distance_price"));
                                    his.setMin_price(obj.getString("min_price"));
                                    his.setBooking_fee(obj.getString("booking_fee"));
                                    his.setCurrnecy_unit(obj.getString("currency"));
                                    his.setDistance_unit(obj.optString("distance_unit"));
                                    historylst.add(his);
                                }

                                if (historylst != null) {
                                    historyAdapter = new HistoryAdapter(this, historylst);
                                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                    ride_lv.setLayoutManager(mLayoutManager);
                                    ride_lv.setItemAnimator(new DefaultItemAnimator());
                                    ride_lv.setAdapter(historyAdapter);
                                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this,getResources().getIdentifier("layout_animation_from_left","anim",getPackageName()));
                                    ride_lv.setLayoutAnimation(animation);
                                    historyAdapter.notifyDataSetChanged();
                                    ride_lv.scheduleLayoutAnimation();
                                }

                            } else {
                                histroy_empty.setVisibility(View.VISIBLE);
                            }

                        } else {
                            histroy_progress_bar.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }

    private void showDetailedHistroy(History history) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMM, dd, yyyy hh:mm a");
        SimpleDateFormat inputformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String hitory_Date = history.getHistory_date();
        try {

            Date date = inputformat.parse(hitory_Date);
            hitory_Date = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Dialog detailedbill = new Dialog(this, R.style.DialogSlideAnim_leftright_Fullscreen);
        detailedbill.requestWindowFeature(Window.FEATURE_NO_TITLE);
        detailedbill.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent_black)));
        detailedbill.setCancelable(true);
        detailedbill.setContentView(R.layout.history_detail_view);
        ImageView btn_close_history = (ImageView) detailedbill.findViewById(R.id.btn_close_history);
        ImageView iv_trip_map = (ImageView) detailedbill.findViewById(R.id.iv_trip_map);
        CircleImageView trip_driver_pic = (CircleImageView) detailedbill.findViewById(R.id.trip_driver_pic);
        TextView trip_driver_name = (TextView) detailedbill.findViewById(R.id.trip_driver_name);
        TextView trip_car_type = (TextView) detailedbill.findViewById(R.id.trip_car_type);
        TextView trip_taxi_type = (TextView) detailedbill.findViewById(R.id.trip_taxi_type);
        TextView trip_source_address = (TextView) detailedbill.findViewById(R.id.trip_source_address);
        TextView trip_destination_address = (TextView) detailedbill.findViewById(R.id.trip_destination_address);
        TextView tv_total = (TextView) detailedbill.findViewById(R.id.tv_total);
        TextView trip_date = (TextView) detailedbill.findViewById(R.id.trip_date);
        TextView trip_amount = (TextView) detailedbill.findViewById(R.id.trip_amount);
        TextView tv_booking_price = (TextView) detailedbill.findViewById(R.id.tv_booking_price);

        TextView tv_base_fare = (TextView) detailedbill.findViewById(R.id.tv_base_fare);
        TextView tv_min_fare = (TextView) detailedbill.findViewById(R.id.tv_min_fare);
        TextView tv_mile_price = (TextView) detailedbill.findViewById(R.id.tv_mile_price);
        TextView tv_minute = (TextView) detailedbill.findViewById(R.id.tv_minute);
        TextView tv_service_tax_price = (TextView) detailedbill.findViewById(R.id.tv_service_tax_price);

        tv_base_fare.setText(history.getCurrnecy_unit() + history.getBase_price());
        tv_min_fare.setText(history.getCurrnecy_unit() + history.getMin_price());
        tv_booking_price.setText(history.getCurrnecy_unit() + history.getBooking_fee());
        tv_mile_price.setText(history.getCurrnecy_unit() + history.getDistance_price()+" "+ "/" + " " + history.getDistance_travel() + " " + history.getDistance_unit());
        tv_minute.setText(history.getCurrnecy_unit() + history.getTime_price() +" "+ "/" + " " + history.getTotal_time() + " " + "mins");
        tv_service_tax_price.setText(history.getCurrnecy_unit() + history.getTax_price());
        Glide.with(this).load(history.getHistory_picture()).error(R.drawable.defult_user).into(trip_driver_pic);
        trip_driver_name.setText(getResources().getString(R.string.txt_ride_with)+" " + history.getProvider_name());
        trip_car_type.setText(history.getHistory_type() + " " + getResources().getString(R.string.reciept));
        trip_taxi_type.setText(getResources().getString(R.string.car_type) + " " + history.getHistory_type());
        trip_source_address.setText(history.getHistory_Sadd());
        trip_destination_address.setText(history.getHistory_Dadd());
        tv_total.setText(history.getCurrnecy_unit() + history.getHistory_total());
        trip_amount.setText(history.getCurrnecy_unit() + history.getHistory_total());
        trip_date.setText(hitory_Date);
        btn_close_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailedbill.dismiss();
            }
        });

        Glide.with(this).load(history.getMap_image()).into(iv_trip_map);

        detailedbill.show();

    }
}
