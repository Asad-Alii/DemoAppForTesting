package com.alqaswa.user.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alqaswa.user.Models.UserSettings;
import com.alqaswa.user.R;
import com.alqaswa.user.Utils.AndyUtils;

import java.util.List;


/**
 * Created by getit on 8/11/2016.
 */
public class UserSettingsAdapter extends BaseAdapter
{
    private Context mContext;
    private List<UserSettings> userSettingsList;
    public UserSettingsAdapter(Context context, List<UserSettings> userSettingsList)
    {
        mContext=context;
        this.userSettingsList=userSettingsList;
    }
    @Override
    public int getCount() {
        return userSettingsList.size();
    }

    @Override
    public Object getItem(int position) {
        return userSettingsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null)
       {
           convertView= LayoutInflater.from(mContext).inflate(R.layout.adpter_user_settings,null);
           holder=new ViewHolder();
           holder.userSettingsIcon= (ImageView) convertView.findViewById(R.id.iv_user_settings_icon);
           holder.userSettingsTitle= (TextView) convertView.findViewById(R.id.tv_user_settings_title);
           convertView.setTag(holder);
       }
        holder= (ViewHolder) convertView.getTag();
        AndyUtils.appLog("UserSettingsAdapter","Position" +position);
        holder.userSettingsTitle.setText(userSettingsList.get(position).getUserSettingsText());
        holder.userSettingsIcon.setImageResource(userSettingsList.get(position).getUserSettingsIcon());

        return convertView;
    }

    class ViewHolder
    {
        ImageView userSettingsIcon;
        TextView userSettingsTitle;
    }
}
