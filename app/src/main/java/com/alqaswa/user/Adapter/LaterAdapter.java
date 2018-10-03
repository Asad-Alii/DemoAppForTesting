package com.alqaswa.user.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.alqaswa.user.HttpRequester.AsyncTaskCompleteListener;
import com.alqaswa.user.HttpRequester.VollyRequester;
import com.alqaswa.user.Models.Later;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;
import com.alqaswa.user.Utils.Commonutils;
import com.alqaswa.user.Utils.Const;
import com.alqaswa.user.Utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 2/3/2017.
 */

public class LaterAdapter extends RecyclerView.Adapter<LaterAdapter.typesViewHolder> implements AsyncTaskCompleteListener {

    private Activity mContext;
    private List<Later> itemshistroyList;
    SimpleDateFormat simpleDateFormat;
    SimpleDateFormat inputformat;

    public LaterAdapter(Activity context, List<Later> itemshistroyList) {
        mContext = context;
        simpleDateFormat = new SimpleDateFormat("E, MMM, dd, yyyy hh:mm a");
        inputformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      /*  simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        inputformat.setTimeZone(TimeZone.getTimeZone("GMT"));*/

        this.itemshistroyList = itemshistroyList;

    }

    @Override
    public LaterAdapter.typesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.later_item, null);
        LaterAdapter.typesViewHolder holder = new LaterAdapter.typesViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final LaterAdapter.typesViewHolder holder, int position) {
        final Later later_itme = itemshistroyList.get(position);

        if (later_itme != null) {
            String later_Date = "";
            try {
                later_Date = later_itme.getReq_date();
                Date date = inputformat.parse(later_Date);
                later_Date = simpleDateFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tv_later_service_type.setText(later_itme.getReq_type());

            holder.tv_later_date.setText(later_Date);
            Glide.with(mContext).load(later_itme.getReq_pic()).into(holder.later_car);
            holder.tv_later_source.setText(later_itme.getS_address());
            if(!later_itme.getD_address().equals("")){
                holder.tv_later_destination.setText(later_itme.getD_address());
            } else {
                holder.tv_later_destination.setText(mContext.getResources().getString(R.string.not_available));
            }

            holder.cancel_later.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelLater(later_itme.getReq_id());
                }
            });
        }

    }

    private void cancelLater(String req_id) {
        if (!AndyUtils.isNetworkAvailable(mContext)) {

            return;
        }
        Commonutils.progressdialog_show(mContext, "Canceling...");
        HashMap<String, String> map = new HashMap<>();
        map.put(Const.Params.URL, Const.ServiceType.CANCEL_LATER_RIDE + Const.Params.ID + "="
                + new PreferenceHelper(mContext).getUserId() + "&" + Const.Params.TOKEN + "="
                +  new PreferenceHelper(mContext).getSessionToken()+"&"+Const.Params.REQUEST_ID + "="+req_id);
        Log.d("mahi", "cancel_reg" + map.toString());
        new VollyRequester(mContext, Const.GET, map, Const.ServiceCode.CANCEL_LATER_RIDE,
                this);
    }

    @Override
    public int getItemCount() {
        return itemshistroyList.size();
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        switch (serviceCode) {

            case Const.ServiceCode.CANCEL_LATER_RIDE:
                Log.e("mahi", "cancel later" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("success").equals("true")) {
                        Commonutils.progressdialog_hide();
                        getLater();
                        Commonutils.showtoast(mContext.getResources().getString(R.string.txt_cancel_schedule), mContext);
                    } else {
                        Commonutils.progressdialog_hide();
                        String error = jsonObject.getString("error");
                        Commonutils.showtoast(error, mContext);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Const.ServiceCode.GET_LATER:
                Log.d("mahi", "later his" + response);
                if (response != null) {
                    try {
                        JSONObject laterobj = new JSONObject(response);
                        if (laterobj.getString("success").equals("true")) {
                            itemshistroyList.clear();
                            JSONArray latArray = laterobj.getJSONArray("data");
                            if (latArray.length() > 0) {
                                for (int i = 0; i < latArray.length(); i++) {
                                    JSONObject obj = latArray.getJSONObject(i);
                                    Later lat = new Later();
                                    lat.setReq_id(obj.getString("request_id"));
                                    lat.setReq_date(obj.getString("requested_time"));
                                    lat.setReq_type(obj.getString("service_type_name"));
                                    lat.setReq_pic(obj.getString("type_picture"));
                                    lat.setD_address(obj.getString("d_address"));
                                    lat.setS_address(obj.getString("s_address"));

                                    itemshistroyList.add(lat);
                                }

                                if (itemshistroyList != null) {
                                    notifyDataSetChanged();
                                }
                            }
                        } else {
                            itemshistroyList.clear();
                            notifyDataSetChanged();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        itemshistroyList.clear();
                        notifyDataSetChanged();
                    }

                }
                break;
            default:
                break;
        }
    }

    private void getLater() {
        if (!AndyUtils.isNetworkAvailable(mContext)) {
            return;
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.Params.URL, Const.ServiceType.GET_LATER);
        map.put(Const.Params.ID, new PreferenceHelper(mContext).getUserId());
        map.put(Const.Params.TOKEN, new PreferenceHelper(mContext).getSessionToken());

        Log.d("mahi", map.toString());
        new VollyRequester(mContext, Const.POST, map, Const.ServiceCode.GET_LATER,
                this);
    }

    public class typesViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_later_service_type, tv_later_date,tv_later_source,tv_later_destination;
        private ImageButton cancel_later;
        private CircleImageView later_car;

        public typesViewHolder(View itemView) {
            super(itemView);
            later_car = (CircleImageView) itemView.findViewById(R.id.later_car);
            tv_later_service_type = (TextView) itemView.findViewById(R.id.tv_later_service_type);
            tv_later_date = (TextView) itemView.findViewById(R.id.tv_later_date);
            cancel_later = (ImageButton) itemView.findViewById(R.id.cancel_later);
            tv_later_source = (TextView)itemView.findViewById(R.id.tv_later_source);
            tv_later_destination = (TextView)itemView.findViewById(R.id.tv_later_destination);
        }
    }


}


