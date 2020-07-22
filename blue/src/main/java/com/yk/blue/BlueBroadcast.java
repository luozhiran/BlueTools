package com.yk.blue;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class BlueBroadcast extends BroadcastReceiver {
    private BlueCallback blueCallback;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (!TextUtils.isEmpty(device.getName())) {
                if (blueCallback != null) {
                    blueCallback.getBlueDevice(device);
                }
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            if (blueCallback != null) {
                blueCallback.startScan();
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if (blueCallback != null) {
                blueCallback.stopScan();
            }
        }  else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (blueState == BluetoothAdapter.STATE_TURNING_ON) {
                if (blueCallback != null) {
                    blueCallback.blueOn();
                }
            } else if (blueState == BluetoothAdapter.STATE_OFF) {
                if (blueCallback != null) {
                    blueCallback.blueOff();
                }
            }
        }
    }

    public void setBlueCallback(BlueCallback blueCallback) {
        this.blueCallback = blueCallback;
    }
}
