package com.alqaswa.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alqaswa.user.Adapter.LaterAdapter;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Models.Later;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 2/3/2017.
 */

public class LaterRequestsActivity  extends AppCompatActivity implements AsyncTaskCompleteListener {

    private Toolbar laterToolbar;
    private ArrayList<Later> laterlst;
    private LaterAdapter laterAdapter;
    private RecyclerView later_lv;
    private ProgressBar later_progress_bar;
    private ImageButton later_back;
    private TextView later_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        laterlst = new ArrayList<Later>();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.later_request_layout);
        laterToolbar = (Toolbar) findViewById(R.id.toolbar_later);


        setSupportActionBar(laterToolbar);
        getSupportActionBar().setTitle(null);
        later_lv = (RecyclerView) findViewById(R.id.ride_lv);
        later_progress_bar = (ProgressBar) findViewById(R.id.ride_progress_bar);
        later_back = (ImageButton) findViewById(R.id.later_back);
        later_empty = (TextView)findViewById(R.id.later_empty);

        later_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        getLater();

    }

    private void getLater() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            return;
        }
        later_progress_bar.setVisibility(View.VISIBLE);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_LATER);
        map.put(Const.Params.ID, new PreferenceHelper(this).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(this).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(this, Const.POST, map, Const.ServiceCode.GET_LATER,
                this);
    }


    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {
            case Const.ServiceCode.GET_LATER:
                Log.d("mahi", "later his" + response);
                if (response != null) {
                    try {
                        JSONObject laterobj = new JSONObject(response);
                        if (laterobj.getString("success").equals("true")) {
                            later_progress_bar.setVisibility(View.GONE);
                            laterlst.clear();
                            JSONArray latArray = laterobj.getJSONArray("data");
                            if (latArray.length() > 0) {
                                for (int i = 0; i < latArray.length(); i++) {
                                    JSONObject obj = latArray.getJSONObject(i);
                                    Later lat = new Later();
                                    lat.setReq_id(obj.getString("request_id"));
                                    lat .setReq_date(obj.getString("requested_time"));
                                    lat.setReq_type(obj.getString("service_type_name"));
                                    lat.setReq_pic(obj.getString("type_picture"));
                                    lat.setD_address(obj.getString("d_address"));
                                    lat.setS_address(obj.getString("s_address"));
                                    laterlst.add(lat);
                                }

                                if (laterlst != null) {
                                    laterAdapter = new LaterAdapter(this, laterlst);
                                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                    later_lv.setLayoutManager(mLayoutManager);
                                    later_lv.setItemAnimator(new DefaultItemAnimator());
                                    later_lv.setAdapter(laterAdapter);
                                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this,getResources().getIdentifier("layout_animation_from_left","anim",getPackageName()));
                                    later_lv.setLayoutAnimation(animation);
                                    laterAdapter.notifyDataSetChanged();
                                    later_lv.scheduleLayoutAnimation();
                                    Log.d("mahi","size"+laterlst.size());
                                    if(laterlst.size()==0){
                                        later_empty.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    later_empty.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            later_progress_bar.setVisibility(View.GONE);
                            later_empty.setVisibility(View.VISIBLE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(laterAdapter!=null){
                            laterAdapter.notifyDataSetChanged();
                        }
                        later_empty.setVisibility(View.VISIBLE);
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
}
