package com.app.madhu.bletest.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.app.madhu.bletest.R;

import java.util.ArrayList;

/**
 * Created by madhu on 3/23/2018.
 */

public class BleServicesAdapter extends BaseAdapter {
    Activity cxt;
    ArrayList<String> list;
    public BleServicesAdapter(Activity cxt, ArrayList<String> list) {
        this.list=list;
        this.cxt=cxt;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        LayoutInflater mInflater = (LayoutInflater)
                cxt.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.bleservicemodel, null);
            holder = new ViewHolder();
            holder.tv_bleservice=(TextView)convertView.findViewById(R.id.tv_bleservice);

            convertView.setTag(holder);
        }
        else
        {
            holder=(ViewHolder)convertView.getTag();
        }
        Log.d("CONSTANTS",""+list.get(position).toString());

        Log.d("CONSTANTS3",""+list.get(position).toString());
        holder.tv_bleservice.setText("Ble Address: "+list.get(position));

        return convertView;
    }
    public class  ViewHolder
    {
        TextView tv_bleservice;
    }
}

