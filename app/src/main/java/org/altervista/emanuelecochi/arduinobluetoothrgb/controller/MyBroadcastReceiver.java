package org.altervista.emanuelecochi.arduinobluetoothrgb.controller;

import org.altervista.emanuelecochi.arduinobluetoothrgb.MainActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // when a new device is discovered, add it to the ListView
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            MainActivity.adapter.add(device.getName());
        }

        // if the scanning process has finished, enable the button
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Toast.makeText(context,"Scan finish!",Toast.LENGTH_SHORT).show();
        }
    }
}
