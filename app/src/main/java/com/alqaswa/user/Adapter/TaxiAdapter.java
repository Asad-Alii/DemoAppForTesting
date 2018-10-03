package com.alqaswa.user.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.alqaswa.user.AdapterCallback;
import com.alqaswa.user.Models.TaxiTypes;
import com.alqaswa.user.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by user on 10/5/2016.
 */
public class TaxiAdapter extends RecyclerView.Adapter<TaxiAdapter.typesViewHolder> {

    private Activity mContext;
    private AdapterCallback mAdapterCallback;
    private List<TaxiTypes> taxiTypesList;
    public int pos;
    DecimalFormat format = new DecimalFormat("0.00");

    public TaxiAdapter(Activity context, List<TaxiTypes> taxiTypesList, AdapterCallback mAdapterCallback) {
        mContext = context;
        this.taxiTypesList = taxiTypesList;
        this.mAdapterCallback = mAdapterCallback;

    }

    @Override
    public TaxiAdapter.typesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_type_item,parent,false);
        typesViewHolder holder = new typesViewHolder(view);

        return holder;
    }



    @Override
    public void onBindViewHolder(final TaxiAdapter.typesViewHolder holder, int position) {
        final TaxiTypes list_types = taxiTypesList.get(position);
        holder.tv_type_name.setText(list_types.getTaxitype());
        if (null!=list_types.getTaxi_cost()&&!list_types.getTaxi_cost().equals("")) {
            holder.tv_type_cost.setText(list_types.getCurrencey_unit()+format.format(Double.valueOf(list_types.getTaxi_cost())));
        } else {
            holder.tv_type_cost.setText(list_types.getCurrencey_unit()+list_types.getTaxi_cost());
        }

        Glide.with(mContext).load(list_types.getTaxiimage()).error(R.drawable.frontal_taxi_cab).into(holder.type_picutre);
        holder.btn_fare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapterCallback.onMethodCallback(list_types.getId(),list_types.getTaxitype(),list_types.getTaxi_price_distance(),list_types.getTaxi_price_min(),list_types.getTaxiimage(),list_types.getTaxi_seats(),list_types.getBasefare());
            }
        });
        holder.tv_type_cost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapterCallback.onMethodCallback(list_types.getId(), list_types.getTaxitype(), list_types.getTaxi_price_distance(), list_types.getTaxi_price_min(), list_types.getTaxiimage(), list_types.getTaxi_seats(), list_types.getBasefare());
            }
        });
     /*
        holder.type_picutre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.type_picutre.setBorderColor(mContext.getResources().getColor(R.color.ripple_blue));
            }
        });*/

        if (pos == position) {

            holder.view_select.setVisibility(View.VISIBLE);
        } else {
            holder.view_select.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return taxiTypesList.size();
    }

    public class typesViewHolder extends RecyclerView.ViewHolder {
        private ImageView type_picutre,btn_fare;
        private TextView tv_type_name, tv_type_cost;
        private View view_select;

        public typesViewHolder(View itemView) {
            super(itemView);
            type_picutre = (ImageView) itemView.findViewById(R.id.type_picutre);
            btn_fare = (ImageView)itemView.findViewById(R.id.btn_fare);
            tv_type_name = (TextView) itemView.findViewById(R.id.tv_type_name);
            tv_type_cost = (TextView) itemView.findViewById(R.id.tv_type_cost);
            view_select = (View)itemView.findViewById(R.id.view_select);


        }
    }

    public void OnItemClicked(int position) {
        pos = position;
        //Log.d("mahi", "pos" + pos);
    }



}
