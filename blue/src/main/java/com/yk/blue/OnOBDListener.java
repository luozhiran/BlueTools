package com.yk.blue;

public interface OnOBDListener {
    void connected();

    void disConnected();

    void error(String msg);

    void receive(String msg);

    void connectFail();


}
