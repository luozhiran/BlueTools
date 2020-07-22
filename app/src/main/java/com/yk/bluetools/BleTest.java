package com.yk.bluetools;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.yk.blelib.BleApi;
import com.yk.blelib.BlueDevice;
import com.yk.blelib.OnBluetoothOpenListener;
import com.yk.blelib.OnScanBlue;
import com.yk.blue.Blue;

public class BleTest {


    public void start(Activity activity) {
        BleApi.getInstance().registerListenerBlue(activity, new OnBluetoothOpenListener() {
            @Override
            public void openBlue() {

                Log.e("MainActivity", "开启蓝牙");
            }

            @Override
            public void closeBlue() {
                Log.e("MainActivity", "关闭蓝牙");
            }
        });

        BleApi.getInstance().setOnScanBlue(new OnScanBlue() {
            @Override
            public void start() {
                Log.e("MainActivity", "开始扫描");

            }

            @Override
            public void stop() {
                Log.e("MainActivity", "停止扫描");
            }


            @Override
            public void scanResult(BlueDevice blue) {
                Log.e("MainActivity", "-------扫描蓝牙结果--------------" + blue.getDevice().getName() + "  " + blue.getDevice().getAddress());
            }
        });
        BleApi.getInstance().start();
    }


    public static class Decode {
        static final char[] hexArray = "0123456789ABCDEF".toCharArray();

        public static String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }


        public static String[] splitTwoCharArray(String str) {
            if (TextUtils.isEmpty(str)) return null;
            char[] chars = str.toCharArray();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                if (i % 2 == 0) {
                    stringBuilder.append(chars[i]);
                } else {
                    stringBuilder.append(chars[i]);
                    if (i != chars.length - 1) {
                        stringBuilder.append(",");
                    }

                }
            }

            return stringBuilder.toString().split(",");
        }
    }
}
