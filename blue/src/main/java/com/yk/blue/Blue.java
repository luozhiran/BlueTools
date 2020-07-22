package com.yk.blue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

public class Blue {
    private final static int AUTO_PAIR_FAIL = 1;
    private final static int DELAY_TIME = 10 * 1000;
    private BluetoothAdapter mBluetoothAdapter;
    private BlueBroadcast mBlueBroadcast;
    private boolean mRegister;
    private ObserverBlueResult observerBlueResult;
    private BluetoothDevice mPareBluetoothDevice;

    private Pair mPair;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case AUTO_PAIR_FAIL:

                    break;
            }
            return true;
        }
    });


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
                abortBroadcast();
                if (mPareBluetoothDevice != null && mPareBluetoothDevice.getAddress().equals(device.getAddress())) {
                    try {
                        mPair.pair(device, "1234");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.e("blue", "开始配对");
            }
        }
    };

    public Blue() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBlueBroadcast = new BlueBroadcast();
        mPair = new Pair();
    }

    public void register(Context context) {
        if (mRegister) return;
        mRegister = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mBlueBroadcast, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        context.registerReceiver(broadcastReceiver, intentFilter);

        mBlueBroadcast.setBlueCallback(new BlueCallback() {
            @Override
            public void getBlueDevice(BluetoothDevice bluetoothDevice) {
                if (observerBlueResult != null) {
                    observerBlueResult.discoveryDevice(bluetoothDevice);
                }
            }

            @Override
            public void startScan() {
                if (observerBlueResult != null) {
                    observerBlueResult.startDiscovery();
                }

            }

            @Override
            public void stopScan() {
                if (observerBlueResult != null) {
                    observerBlueResult.endDiscovery();
                }
            }

            @Override
            public void blueOff() {
                Log.e("blue", "蓝牙关闭");
                Blue.this.stopScan();
            }

            @Override
            public void blueOn() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startBlue("");
                    }
                }, 1000);

                Log.e("blue", "蓝牙打开");
            }
        });
        mPair.setOnOBDListener(new OnOBDListener() {
            @Override
            public void connected() {
                if (observerBlueResult != null) {
                    observerBlueResult.connected();
                }
            }

            @Override
            public void disConnected() {
                if (observerBlueResult != null) {
                    observerBlueResult.disConnected();
                }
            }

            @Override
            public void error(String msg) {
                if (observerBlueResult != null) {
                    observerBlueResult.error(msg);
                }
            }

            @Override
            public void receive(String msg) {
                if (observerBlueResult != null) {
                    observerBlueResult.receive(msg);
                }
            }

            @Override
            public void obdFail() {
                if (observerBlueResult != null) {
                    observerBlueResult.obdFail();
                }
            }
        });
    }

    public void unRegister(Context context) {
        if (!mRegister) return;
        mRegister = false;
        context.unregisterReceiver(mBlueBroadcast);
        context.unregisterReceiver(broadcastReceiver);
    }

    /**
     * 释放线程资源
     */
    public void release() {
        mPair.release();
    }

    //"00:1D:A5:68:98:8B"
    @SuppressLint("MissingPermission")
    public void startBlue(String address) {
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            } else {
                if (TextUtils.isEmpty(address)) {
                    if (!mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.startDiscovery();
                    }
                } else {
                    try {
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                        if (device != null) {
                            mPair.pair(device, "1234");
                        } else {
                            if (!mBluetoothAdapter.isDiscovering()) {
                                mBluetoothAdapter.startDiscovery();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        }
    }


    /**
     * 华为手机必须开启定位
     *
     * @param context
     */
    public boolean needOpenLocation(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!isOPen(context)) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivityForResult(intent, requestCode); // 设置完成后返回到原来的界面
                return true;
            }
        }
        return false;
    }


    public final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
//        if (mBlueBroadcast != null) {
//            mBlueBroadcast.abortBroadcast();
//        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @SuppressLint("MissingPermission")
    public void connect(BluetoothDevice bluetoothDevice, String pin) {
        if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_NONE) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
            try {
                if (device != null) {
                    mPair.pair(device, "1234");
                }
            } catch (Exception e) {
                Log.e("Blue", "remote device is not bonded (paired)");
                e.printStackTrace();
            }
        } else {
            stopScan();
            try {
                mPareBluetoothDevice = bluetoothDevice;
                ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice);
//                mHandler.removeMessages(AUTO_PAIR_FAIL);
//                mHandler.sendEmptyMessageDelayed(DELAY_TIME, AUTO_PAIR_FAIL);
            } catch (Exception e) {
                e.printStackTrace();
                mPareBluetoothDevice = null;
            }
        }
    }


    public void sendMsg(byte[] bytes) {
        mPair.sendMsg(bytes);
    }


    public void setObserverBlueResult(ObserverBlueResult observerBlueResult) {
        this.observerBlueResult = observerBlueResult;
    }


}
