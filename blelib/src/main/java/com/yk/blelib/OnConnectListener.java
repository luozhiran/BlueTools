package com.yk.blelib;

public interface OnConnectListener {
    void connected();
    void receive(byte[] bytes);
    void disConnected();
    void error(String msg);
}
