package com.alqaswa.user.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alqaswa.user.Models.TaxiTypes;
import com.alqaswa.user.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Mahesh on 4/20/2017.
 */

public class BoltTypesAdapter extends RecyclerView.Adapter<BoltTypesAdapter.typesViewHolder> {

    private Activity mContext;
    private List<TaxiTypes> taxiTypesList;
    public int pos;
    DecimalFormat format = new DecimalFormat("0.00");

    public BoltTypesAdapter(Activity context, List<TaxiTypes> taxiTypesList) {
        mContext = context;
        this.taxiTypesList = taxiTypesList;

    }

    @Override
    public BoltTypesAdapter.typesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.type_item, null);
        BoltTypesAdapter.typesViewHolder holder = new BoltTypesAdapter.typesViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final BoltTypesAdapter.typesViewHolder holder, int position) {
        TaxiTypes list_types = taxiTypesList.get(position);
        holder.tv_type_name.setText(list_types.getTaxitype());



    }

    @Override
    public int getItemCount() {
        return taxiTypesList.size();
    }

    public class typesViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_type_name;

        public typesViewHolder(View itemView) {
            super(itemView);

            tv_type_name = (TextView) itemView.findViewById(R.id.tv_type_name);

        }
    }

    public void OnItemClicked(int position) {
        pos = position;
        Log.d("mahi", "pos" + pos);
    }
}