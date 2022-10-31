package com.example.callback;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceActivity extends AppCompatActivity {
    public static final int bluetoothrequestcode = 11;
    boolean bluetoothPermissionGranted;
//    public static final String ACTION_REQUEST_ENABLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("SelectDeviceActivity", "onCreate: SelectDeviceActivity");
        setContentView(R.layout.activity_select_device);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);


    // Create a BroadcastReceiver for ACTION_FOUND.
     // Bluetooth Setup
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();

            finish();
        }
        else{

            afterconnection();
        }

    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
    @SuppressLint("MissingPermission")
    public void afterconnection(){

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
//        BluetoothAdapter bluetoothAdapter = getDefaultAdapter();
        Log.i("SelectDeviceActivity", String.format(" created adapter %s", bluetoothAdapter));
        // Get List of Paired Bluetooth Device
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, bluetoothrequestcode);
        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
////
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, bluetoothrequestcode);
//            }
//            // here to request the missing permissions, and then overriding
////               public void onRequestPermissionsResult(int requestCode, String[] { },
////                                                      int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            Log.i("SelectDeviceActivity"," Requestss" );
//
//        }else {
//            Log.i("Requesting updates", "Permissions are already givens ");

//            bluetoothPermissionGranted = true;
////            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        }
        Log.i("SelectDeviceActivity"," almost" );
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<Object> deviceList = new ArrayList<>();
        Log.i("SelectDeviceActivity"," yeeeeha" );
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName,deviceHardwareAddress);
                deviceList.add(deviceInfoModel);
            }
            // Display paired device using recyclerView
            RecyclerView recyclerView = findViewById(R.id.recyclerViewDevice);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this, deviceList);
            recyclerView.setAdapter(deviceListAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        else {
            View view = findViewById(R.id.recyclerViewDevice);
            Snackbar snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", view1 -> { });
            snackbar.show();
        }

    }

    public void startBluetoothSetup(View view) {
        Intent i1 = new Intent(this, BluetoothSetup.class);
        startActivity(i1);
    }
}