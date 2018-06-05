package com.app.madhu.bletest.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.madhu.bletest.R;

import java.util.List;
import java.util.UUID;

public class RWActivity extends AppCompatActivity {

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

    public static BluetoothGattCharacteristic characteristic;
    public static BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    String s;

    private String ble_name,ble_mac,ble_serviceUUID,ble_subserviceUUID;
    private TextView tv_blename,tv_constatus,tv_byteresponse;
    private Dialog dialog;
    private EditText et_write,et_read;
    private Button btn_write,btn_read;
    private CheckBox ch_notify;
    private String bytedata="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rw);

        init();
    }

    private void init()
    {
        Intent intent=getIntent();
        ble_name=intent.getStringExtra("BLE_NAME");
        ble_mac=intent.getStringExtra("BLE_MAC_ADDRESS");
        ble_serviceUUID=intent.getStringExtra("SERVICE_ID");
        ble_subserviceUUID=intent.getStringExtra("SUB_SERVICE_ID");

        tv_blename=(TextView)findViewById(R.id.tv_blename);
        tv_blename.setText(ble_name);
        tv_constatus=(TextView)findViewById(R.id.tv_constatus);
        tv_byteresponse=(TextView)findViewById(R.id.tv_byteresponse);

        et_write=(EditText)findViewById(R.id.et_write);
        et_read=(EditText)findViewById(R.id.et_read);
        btn_write=(Button)findViewById(R.id.btn_send);
        btn_read=(Button)findViewById(R.id.btn_read);
        ch_notify=(CheckBox)findViewById(R.id.ch_notification);

        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.enable();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        if(mBluetoothAdapter==null) {
            Log.e("not supported", "not supported");
            Toast.makeText(RWActivity.this,"Device not supported",Toast.LENGTH_SHORT).show();
            finish();
        }


        if (dialog == null)
            dialog = new Dialog(RWActivity.this,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(false);
        dialog.show();

        connect(ble_mac);

        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mBluetoothGatt!=null)
                  readCustomCharacteristic(ble_serviceUUID,ble_subserviceUUID);
            }
        });

        btn_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String write_data=et_write.getText().toString();
                if(mBluetoothGatt!=null && characteristic!=null)
                {
                    if(write_data.trim().length()>0)
                    {
                         bytedata="";
                        characteristic.setValue(write_data.getBytes());
                        mBluetoothGatt.writeCharacteristic(characteristic);
                    }
                }
            }
        });

        ch_notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    notificationEnable(b);
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
                            if(sub_services.get(j).getUuid().toString().equals(ble_subserviceUUID))
                            {
                                characteristic = services.get(i).getCharacteristics().get(j);
                                Log.e("Main service ", "uuid is " + ble_serviceUUID);
                                Log.e("Sub service ", "uuid is " + characteristic.getUuid().toString());
                            }
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });

            } else {
                Log.e("", "onServicesDiscovered received: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("onCharacteristicRead", "onCharacteristicRead: " + status);

                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e("onCharacteristicChanged", "onCharacteristicChanged: " + characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        sendBroadcast(intent);
        Log.e("INTENT SENT", "OK");
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Log.e("broadcast update", "broadcastUpdate() "+characteristic.getUuid().toString());
        final Intent intent = new Intent(action);
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        bytedata="size is "+data.length+"\n";
        bytedata=bytedata+bytesToHex(data);

        Log.e("length ", "data.length: " + data.length);
        s = new String(data);
        Log.e("data", "data is: " + s);
        intent.putExtra(EXTRA_DATA, new String(data) + "\n" + s.toString());


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    et_read.setText(s.toString());
                    tv_byteresponse.setText(bytedata);
                    if (s.toString().equalsIgnoreCase("y"))
                    {

                    }
                    else
                    {

                    }

                }
            });

        sendBroadcast(intent);
    }

    public void readCustomCharacteristic(String serviceUuid,String charUuid) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e("not initialised", "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(serviceUuid));
        if(mCustomService == null){
            Log.e("BLE Service not found", "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(charUuid));
        if(mBluetoothGatt.readCharacteristic(mReadCharacteristic) == false){
            Log.e("Failed", "Failed to read characteristic");
        }
        else {
            Log.e("read value", ""+new String(mReadCharacteristic.getValue()));
        }
    }

    public void writeCustomCharacteristic(String serviceUuid,String charUuid,String value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e("not initialized", "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(serviceUuid));
        if(mCustomService == null){
            Log.e("Service not found", "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(charUuid));
        //mWriteCharacteristic.setValue(value,android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0);
        mWriteCharacteristic.setValue(value.getBytes());
        if(mBluetoothGatt.writeCharacteristic(mWriteCharacteristic) == false){
            Log.e("Failed", "Failed to write characteristic");
        }
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

    public void notificationEnable(boolean enableornot)
    {
        mBluetoothGatt.setCharacteristicNotification(characteristic, enableornot);
        if(enableornot)
        {
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
        else
        {
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                descriptor.setValue( BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }

    }



    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
