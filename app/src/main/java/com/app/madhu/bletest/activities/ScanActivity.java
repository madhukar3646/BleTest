package com.app.madhu.bletest.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.madhu.bletest.helperclasses.ScanAnimation;
import com.app.madhu.bletest.models.Dbbean;
import com.app.madhu.bletest.adapters.ListDevicesAdapter;
import com.app.madhu.bletest.R;

import java.util.ArrayList;
import java.util.HashMap;


public class ScanActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private Dialog dialog;
    private ListView listViewdevices;
    private ArrayList<Dbbean> mDeviceList = new ArrayList<Dbbean>();
    private ArrayList<String> macslist=new ArrayList<>();
    private HashMap<String,Dbbean> device_hashlist=new HashMap<>();
    private ListDevicesAdapter devicesAdapter;
    private RelativeLayout animview;
    private ScanAnimation animation;
    private int width,height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        DisplayMetrics metrics=getResources().getDisplayMetrics();
        width=metrics.widthPixels;
        height=metrics.heightPixels;

        mDeviceList.clear();
        device_hashlist.clear();
        macslist.clear();
        listViewdevices = (ListView) findViewById(R.id.listdevices);
        devicesAdapter=new ListDevicesAdapter(ScanActivity.this,mDeviceList);
        listViewdevices.setAdapter(devicesAdapter);

        animview=(RelativeLayout)findViewById(R.id.animview);
        animview.getLayoutParams().width=width/2;
        animview.getLayoutParams().height=width/2;
        animation=new ScanAnimation(ScanActivity.this,width/2,width/2);
        animview.addView(animation);

        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.enable();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        if(mBluetoothAdapter==null) {
            Log.e("not supported", "not supported");
            finish();
        }

        if (dialog == null)
            dialog = new Dialog(ScanActivity.this,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(false);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        listViewdevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(ScanActivity.this, BleServicesActivity.class);
                intent.putExtra("BLE_NAME",mDeviceList.get(position).blename);
                intent.putExtra("BLE_MAC_ADDRESS",mDeviceList.get(position).macadress);
                startActivity(intent);
            }
        });

        animview.setVisibility(View.VISIBLE);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Dbbean bean = new Dbbean();
                if (device.getName() == null) {
                    bean.blename = "UNKNOWN";
                    Log.e("device name","Unknown");
                } else {
                    bean.blename = device.getName();
                    Log.e("device name",""+device.getName());
                }
                bean.macadress = device.getAddress();
                // mDeviceList.add(bean);

                device_hashlist.put(bean.macadress,bean);
                for (String key : device_hashlist.keySet())
                {
                    if(!macslist.contains(key)) {
                        mDeviceList.add(device_hashlist.get(key));
                        macslist.add(key);
                    }
                }
                devicesAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e("In discovery finished", "Discovery finished");
                unregisterReceiver(mReceiver);
                animview.setVisibility(View.GONE);
                if(mDeviceList.size()==0)
                    Toast.makeText(ScanActivity.this,"No devices Found",Toast.LENGTH_SHORT).show();

            }

        }
    };
    @Override
    protected void onDestroy() {

        try {
            unregisterReceiver(mReceiver);
            super.onDestroy();
        }catch (Exception e){
            super.onDestroy();
        }

    }

}
