package com.yk.blelib;

import android.text.TextUtils;

public class Decode {

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
