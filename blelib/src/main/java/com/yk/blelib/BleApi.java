package com.yk.blelib;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;

public class BleApi {
    //A和n的值，需要根据实际环境进行检测得出
    private static final double A_Value = 50;
    //A - 发射端和接收端相隔1米时的信号强度
    private static final double n_Value = 2.5;
    private static volatile BleApi bleApi = null;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private BlueExchange mBlueExchange;
    private BlueOpenOnBroadcast mBlueOpenOnBroadcast;//蓝牙开启关闭广播
    private OnBluetoothOpenListener mOutOnBluetoothOpenListener = null;
    private OnScanBlue mOnScanBlue;
    private Set<String> mBlueAddress = new HashSet<>();
    private OnConnectStateListener mOnConnectStateListener;
    private boolean isConnected;
    private BluetoothDevice mConnectDevice;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            return true;
        }
    });

    public static BleApi getInstance() {
        if (bleApi == null) {
            synchronized (BleApi.class) {
                if (bleApi == null) {
                    bleApi = new BleApi();
                }
            }
        }
        return bleApi;
    }

    private OnConnectListener mOnConnectListener = new OnConnectListener() {
        @Override
        public void connected() {
            isConnected = true;
            mConnectDevice = null;
            if (mOnConnectStateListener != null) {
                mOnConnectStateListener.onConnected();
            }
        }

        @Override
        public void receive(byte[] bytes) {
            if (mOnConnectStateListener != null) {
                mOnConnectStateListener.onReceive(bytes);
            }
        }

        @Override
        public void disConnected() {
            isConnected = false;
            mConnectDevice = null;
            if (mOnConnectStateListener != null) {
                mOnConnectStateListener.onDisConnected();
            }
        }

        @Override
        public void error(String msg) {
            isConnected = false;
            mConnectDevice = null;
            if (mOnConnectStateListener != null) {
                mOnConnectStateListener.onError(msg);
            }
        }
    };


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!TextUtils.isEmpty(device.getName()) && device.getName().contains("LS")) {//
                if (mBlueAddress.contains(device.getAddress())) return;
                float distance = (float) getDistance(rssi);
                BlueDevice blueDevice = new BlueDevice(distance, device);
                mBlueAddress.add(device.getAddress());
                if (mOnScanBlue != null) {
                    mOnScanBlue.scanResult(blueDevice);
                }
            }
        }
    };


    public BleApi() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBlueExchange = new BlueExchange(mOnConnectListener);
        mBlueOpenOnBroadcast = new BlueOpenOnBroadcast(new OnBluetoothOpenListener() {
            @Override
            public void openBlue() {
                if (!mScanning) {
                    start();
                }
                if (mOutOnBluetoothOpenListener != null) {
                    mOutOnBluetoothOpenListener.openBlue();
                }
            }

            @Override
            public void closeBlue() {
                if (mOutOnBluetoothOpenListener != null) {
                    mOutOnBluetoothOpenListener.closeBlue();
                }
            }
        });
    }


    public void start() {
        if (isSupportBluetooth() && isEnabled() && !mScanning) {
            mBlueAddress.clear();
            startScan();
        } else {
            openBlueAsync();
        }
    }


    public void connect(BluetoothDevice device, Context context) {
        mBlueExchange.connect(device, context);
    }

    public void sendMsg(byte[] bytes, String serviceUuid, String characterUuid) {
        if (!isSupportBluetooth() || !checkBluetoothEnable()) {
            mOnConnectListener.error("蓝牙已经关闭");
        } else {
            mBlueExchange.sendMsg(bytes, serviceUuid, characterUuid);
        }
    }


    public boolean isConnected() {
        return isConnected;
    }


    /**
     * 监听蓝牙打开或这关闭
     *
     * @param context
     */
    public void registerListenerBlue(Context context, OnBluetoothOpenListener callback) {
        mOutOnBluetoothOpenListener = callback;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mBlueOpenOnBroadcast, intentFilter);
    }


    public void setOnConnectStateListener(OnConnectStateListener onConnectStateListener) {
        this.mOnConnectStateListener = onConnectStateListener;
    }


    public void setOnScanBlue(OnScanBlue onScanBlue) {
        mOnScanBlue = onScanBlue;
    }

    public void release(Context context) {
        if (mOutOnBluetoothOpenListener != null) {
            context.unregisterReceiver(mBlueOpenOnBroadcast);
            mOutOnBluetoothOpenListener = null;
        }
        mOnConnectStateListener = null;
        mOnScanBlue = null;
    }

    public double getDistance(int rssi) {
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }


    /**
     * 检查蓝牙是否可用
     *
     * @return true 可用，false 可用
     */
    @SuppressLint("MissingPermission")
    public boolean checkBluetoothEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 是否支持蓝牙
     *
     * @return true 支持，false 不支持
     */
    final public boolean isSupportBluetooth() {
        return mBluetoothAdapter != null;
    }


    @SuppressLint("MissingPermission")
    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public void openBlueAsync() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }


    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (mScanning) {
            mScanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                if (mOnScanBlue != null) {
                    mOnScanBlue.stop();
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    public void startScan() {
        if (!mScanning) {
            mScanning = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    stopScan();
                }
            }, 15 * 1000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (mOnScanBlue != null) {
                    mOnScanBlue.start();
                }
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        }
    }

}
