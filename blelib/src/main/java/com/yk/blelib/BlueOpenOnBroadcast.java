package com.yk.blelib;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BlueOpenOnBroadcast extends BroadcastReceiver {

    private OnBluetoothOpenListener mOnBluetoothOpenListener;

    public BlueOpenOnBroadcast(OnBluetoothOpenListener onBluetoothOpenListener) {
        mOnBluetoothOpenListener = onBluetoothOpenListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON://蓝牙正在打开
                            mOnBluetoothOpenListener.openBlue();
                            break;
                        case BluetoothAdapter.STATE_ON://蓝牙已经打开
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF://蓝牙正在关闭
                            break;
                        case BluetoothAdapter.STATE_OFF://蓝牙已经关闭
                            mOnBluetoothOpenListener.closeBlue();
                            break;

                    }
                    break;
            }
        }
    }
}
