package com.app.madhu.bletest.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.madhu.bletest.models.Dbbean;
import com.app.madhu.bletest.R;

import java.util.ArrayList;

/**
 * Created by swetha on 2/6/2017.
 */

public class ListDevicesAdapter extends BaseAdapter {
    Context cxt;
    ArrayList<Dbbean> list;
    public ListDevicesAdapter(Context cxt, ArrayList<Dbbean> list) {
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
        ListDevicesAdapter.ViewHolder holder = null;
        LayoutInflater mInflater = (LayoutInflater)
                cxt.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.scanlistadp, null);
            holder = new ListDevicesAdapter.ViewHolder();
            holder.tshirtnum=(TextView)convertView.findViewById(R.id.tshirtnum);
            holder.tshirtname=(TextView)convertView.findViewById(R.id.tshirtname);

            convertView.setTag(holder);

        }
        else
        {
            holder=(ListDevicesAdapter.ViewHolder)convertView.getTag();
        }
        Log.d("CONSTANTS",""+list.get(position).toString());

        Log.d("CONSTANTS3",""+list.get(position).toString());
        holder.tshirtnum.setText("Ble Address: "+list.get(position).macadress);
        holder.tshirtname.setText(""+list.get(position).blename.toString());

        return convertView;
    }
    public class  ViewHolder
    {
        TextView tshirtnum,tshirtname;
    }
}

