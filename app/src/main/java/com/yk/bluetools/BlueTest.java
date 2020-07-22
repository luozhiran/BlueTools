package com.yk.bluetools;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.yk.blue.Blue;
import com.yk.blue.ObserverBlueResult;

public class BlueTest {

    private Blue blue;

    public BlueTest(final ObserverBlueResult observerBlueResult) {
        this.blue = new Blue();

        blue.setObserverBlueResult(new ObserverBlueResult() {
            @Override
            public void startDiscovery() {
                observerBlueResult.startDiscovery();
                Log.e("MainActivity", "开始扫描");
            }

            @Override
            public void endDiscovery() {
                observerBlueResult.endDiscovery();
                Log.e("MainActivity", "停止扫描");
            }

            @Override
            public void discoveryDevice(BluetoothDevice device) {
                Log.e("MainActivity", "-------扫描蓝牙结果--------------" + device.getName() + "  " + device.getAddress());
                observerBlueResult.discoveryDevice(device);
            }

            @Override
            public void connected() {
                Log.e("MainActivity", "obd连接成功");
                observerBlueResult.connected();
            }

            @Override
            public void disConnected() {
                Log.e("MainActivity", "obd断开连接");
                observerBlueResult.disConnected();
            }

            @Override
            public void error(String msg) {
                Log.e("MainActivity", "obd 错误 "+msg);
                observerBlueResult.error(msg);
            }

            @Override
            public void receive(String msg) {
                Log.e("MainActivity", " "+msg);
                observerBlueResult.receive(msg);
            }

            @Override
            public void obdFail() {
                Log.e("MainActivity", " obd连接失败");
                observerBlueResult.obdFail();
            }
        });
    }


    public void start(Activity activity, int requestCode) {
        blue.register(activity);
        if (!blue.needOpenLocation(activity, requestCode)) {
            blue.startBlue("00:1D:A5:68:98:8B");
        }
    }

    public void release(Activity activity) {
        blue.unRegister(activity);
        blue.release();

    }


    public void connect(BluetoothDevice bluetoothDevice, String pin) {
        blue.connect(bluetoothDevice, pin);
    }


}
