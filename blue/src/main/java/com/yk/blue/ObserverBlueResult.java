package com.yk.blue;

import android.bluetooth.BluetoothDevice;

public interface ObserverBlueResult {
    void startDiscovery();

    void endDiscovery();

    void discoveryDevice(BluetoothDevice device);

    void connected();

    void connectFail();

    void disConnected();

    void error(String msg);

    void receive(String msg);


    void blueOpen();

    void blueClose();

    void pairStart(BluetoothDevice device);
    void pairSuccess(BluetoothDevice device);
    void pairFail(BluetoothDevice device);

}
