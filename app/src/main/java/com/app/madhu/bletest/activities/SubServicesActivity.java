package com.app.madhu.bletest.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.madhu.bletest.R;
import com.app.madhu.bletest.adapters.BleServicesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubServicesActivity extends AppCompatActivity {

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private int mConnectionState = STATE_CONNECTING;

    public static BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    String s;

    private String ble_name,ble_mac,ble_serviceUUID;
    private TextView tv_blename,tv_constatus;
    private ListView lv_serviceslist;
    private ArrayList<String> serviceslist=new ArrayList<>();
    private BleServicesAdapter bleServicesAdapter;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_services);

        init();
    }
    private void init()
    {
        serviceslist.clear();
        Intent intent=getIntent();
        ble_name=intent.getStringExtra("BLE_NAME");
        ble_mac=intent.getStringExtra("BLE_MAC_ADDRESS");
        ble_serviceUUID=intent.getStringExtra("SERVICE_ID");

        tv_blename=(TextView)findViewById(R.id.tv_blename);
        tv_blename.setText(ble_name);
        tv_constatus=(TextView)findViewById(R.id.tv_constatus);
        lv_serviceslist=(ListView)findViewById(R.id.lv_services);
        bleServicesAdapter=new BleServicesAdapter(SubServicesActivity.this,serviceslist);
        lv_serviceslist.setAdapter(bleServicesAdapter);

        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.enable();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        if(mBluetoothAdapter==null) {
            Log.e("not supported", "not supported");
            Toast.makeText(SubServicesActivity.this,"Device not supported",Toast.LENGTH_SHORT).show();
            finish();
        }

        if (dialog == null)
            dialog = new Dialog(SubServicesActivity.this,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(false);
        dialog.show();

        lv_serviceslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent1=new Intent(SubServicesActivity.this,RWActivity.class);
                intent1.putExtra("BLE_NAME",ble_name);
                intent1.putExtra("BLE_MAC_ADDRESS",ble_mac);
                intent1.putExtra("SERVICE_ID",ble_serviceUUID);
                intent1.putExtra("SUB_SERVICE_ID",serviceslist.get(i));
                startActivity(intent1);
            }
        });

        tv_constatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(tv_constatus.getText().toString().equalsIgnoreCase("CONNECTED"))
                {
                    if(mBluetoothGatt!=null)
                    {
                        mBluetoothGatt.disconnect();
                    }
                }
                else
                {
                    if(mBluetoothGatt!=null)
                    {
                        tv_constatus.setText("CONNECTING...");
                        mBluetoothGatt.connect();
                    }
                    else
                    {
                        connect(ble_mac);
                    }
                }

            }
        });

        connect(ble_mac);

    }
    public boolean connect(final String address) {

        tv_constatus.setText("CONNECTING...");
        Log.e("SSSSSSSSSSSSSSSSSS", "BluetoothAdapter not initialized or unspecified address." + address);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || address == null) {
            Log.e("null", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.e("", "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e("", "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e("", "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_constatus.setText("CONNECTED");
                    }
                });

                Log.e("STATE CONNECTED", "OK");
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                // Attempts to discover services after successful connection.
                mBluetoothGatt.discoverServices();
                Log.e("AFTER DISCOVER SERVICES", "OK");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        tv_constatus.setText("DISCONNECTED");
                    }
                });

                Log.e("STATE DISCONNECTED", "OK");
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e("disconnected", "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }

            if (mConnectionState == 123334) {

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                mBluetoothGatt = gatt;
                List<BluetoothGattService> services = gatt.getServices();
                Log.e("SWETHA", "getCharacteristics  size :: " + services.size());
                for (int i = 0; i < services.size(); i++) {
                    UUID uuid = services.get(i).getUuid();
                    if(uuid.toString().equals(ble_serviceUUID))
                    {
                        List<BluetoothGattCharacteristic> sub_services =services.get(i).getCharacteristics();
                        for(int j=0;j<sub_services.size();j++)
                        {
                            serviceslist.add(sub_services.get(j).getUuid().toString());
                        }

                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        bleServicesAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

            } else {
                Log.e("", "onServicesDiscovered received: " + status);
            }
        }
    };


    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
        Log.e("INTENT SENT", "OK");
    }


    @Override
    protected void onDestroy() {

        try {
            if(mBluetoothGatt!=null)
                mBluetoothGatt.disconnect();
            super.onDestroy();
        }catch (Exception e){
            super.onDestroy();
        }

    }

    @Override
    protected void onPause() {

        try {
            if(mBluetoothGatt!=null)
                mBluetoothGatt.disconnect();
            super.onPause();
        }catch (Exception e){
            super.onPause();
        }

    }
}
