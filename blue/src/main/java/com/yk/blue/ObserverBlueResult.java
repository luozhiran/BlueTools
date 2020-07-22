package com.yk.blue;

import android.bluetooth.BluetoothDevice;

public interface ObserverBlueResult {
    void startDiscovery();

    void endDiscovery();

    void discoveryDevice(BluetoothDevice device);

    void connected();

    void disConnected();

    void error(String msg);

    void receive(String msg);

    void obdFail();
}