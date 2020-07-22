package com.yk.blelib;

public interface OnScanBlue {
    void start();
    void stop();
    void scanResult(BlueDevice blueDevice);
}
