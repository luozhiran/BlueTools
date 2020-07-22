package com.yk.blelib;

public interface OnConnectStateListener {
    void onConnected();
    void onReceive(byte[] bytes);
    void onDisConnected();
    void onError(String msg);
}
