package com.yk.blue;

import android.bluetooth.BluetoothDevice;

public interface BlueCallback {
    void getBlueDevice(BluetoothDevice bluetoothDevice);
    void startScan();
    void stopScan();
    void blueOff();
    void blueOn();

}
