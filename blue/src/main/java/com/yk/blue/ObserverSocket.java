package com.yk.blue;

public interface ObserverSocket {
    void connected();
    void onError(String msg);
    void receiveBefore();
    boolean receive(byte[] data);
    void disConnected();
    void connectFail();
}
