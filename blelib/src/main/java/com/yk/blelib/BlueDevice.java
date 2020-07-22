package com.yk.blelib;

import android.bluetooth.BluetoothDevice;

public class BlueDevice {
    private BluetoothDevice device;
    private float distance;
    private int extra;

    public BlueDevice() {
    }

    public BlueDevice(float distance, BluetoothDevice device) {

        this.distance = distance;
        this.device = device;
    }



    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }


    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }
}
