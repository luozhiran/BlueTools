package com.yk.blelib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlueExchange {
    //A和n的值，需要根据实际环境进行检测得出
    private static final double A_Value = 50;
    //A - 发射端和接收端相隔1米时的信号强度
    private static final double n_Value = 2.5;
    private BluetoothGatt mBluetoothGatt;
    private OnConnectListener mOnConnectListener;
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> mServicesMap = new HashMap<>();

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * 当连接状态发生改变
         * newState 0表示未连接上，2表示已连接设备
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e("BlueExchange", "接状态发生改变");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                mOnConnectListener.connected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mOnConnectListener.disConnected();
            } else {
                mOnConnectListener.disConnected();
                gatt.disconnect();
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e("BlueExchange", "发现新服务");
            if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
                    BluetoothGattService bluetoothGattService = services.get(i);
                    String serviceUUid = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> characteristicsList = bluetoothGattService.getCharacteristics();
                    BluetoothGattCharacteristic characteristic = null;
                    for (int j = 0; j < characteristicsList.size(); j++) {
                        characteristic = characteristicsList.get(j);
                        charMap.put(characteristic.getUuid().toString(), characteristic);
                    }
                    mServicesMap.put(serviceUUid, charMap);
                }
                enableAllNotification();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] arrayOfByte = characteristic.getValue();
            mOnConnectListener.receive(arrayOfByte);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            int iRssi = Math.abs(rssi);
            double power = (iRssi - A_Value) / (10 * n_Value);
            double distance = Math.pow(10, power);
            Log.e("BlueExchange", "设备距离 =" + distance);
        }
    };


    public BlueExchange(OnConnectListener onConnectListener) {
        mOnConnectListener = onConnectListener;
    }


    private void enableAllNotification() {
        for (Map.Entry<String, Map<String, BluetoothGattCharacteristic>> en : mServicesMap.entrySet()) {
            Map<String, BluetoothGattCharacteristic> map = en.getValue();
            for (Map.Entry<String, BluetoothGattCharacteristic> cha : map.entrySet()) {
                BluetoothGattCharacteristic bgh = cha.getValue();
                enableNotification(true, bgh);
            }
        }
    }


    /**
     * 设置通知
     *
     * @param enable         true为开启false为关闭
     * @param characteristic 通知特征
     * @return
     */
    private void enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {

        if (mBluetoothGatt == null || characteristic == null) {
            return;
        }
        boolean result = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        Log.e("BlueExchange", "setCharacteristicNotification =" + result);
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (clientConfig != null) {
            if (enable) {
                clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            mBluetoothGatt.writeDescriptor(clientConfig);
        }
    }


    /**
     * 根据服务UUID和特征UUID,获取一个特征{@link BluetoothGattCharacteristic}
     *
     * @param serviceUUID
     * @param characterUUID
     * @return
     */
    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = mServicesMap.get(serviceUUID);
        if (bluetoothGattCharacteristicMap == null) return null;
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();

        BluetoothGattCharacteristic bluetoothGattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            bluetoothGattCharacteristic = entry.getValue();
            break;
        }
        return bluetoothGattCharacteristic;

    }


    public void sendMsg(byte[] bytes, String serviceUuid, String characterUuid) {
        if (mBluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(serviceUuid, characterUuid);
            if (characteristic == null) {
                mOnConnectListener.error("无法获取到硬件设备上的服务");
            } else {
                characteristic.setValue(bytes);
                boolean res = mBluetoothGatt.writeCharacteristic(characteristic);
                if (!res) {
                    mOnConnectListener.error("发送数据失败");
                }
            }
        }
    }


    public void connect(BluetoothDevice device, Context context) {
        close();
        mBluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);

    }


    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }
}
