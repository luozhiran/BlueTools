package com.yk.blue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class Pair {
    private String mUuid = "00001101-0000-1000-8000-00805F9B34FB";
    private SocketIm mSocketIm;
    private StringBuilder mResult = new StringBuilder();
    private String resultType;
    private OnOBDListener onOBDListener;
    public static final int SEND_SUC = 1;
    public static final int SEND_FAIL = 2;
    public static final int NOT_CONNECT_BLUE = 3;

    /**
     * 原本通过广播可以一次性配对，并且连接通信，华为v20不支持（没找到解决办法），只能通过弹出框进行配对
     *
     * @param device
     * @param pin
     * @param isPair
     */
    public void pair(BluetoothDevice device, String pin, boolean isPair) {
//        ClsUtils.setPairingConfirmation(device.getClass(), device, true);
        if (!isPair) {
            try {
                ClsUtils.setPin(device.getClass(), device, pin);
                connectDeviceSocket(device);
            } catch (Exception e) {
                e.printStackTrace();
                onOBDListener.connectFail();
            }
        } else {
            connectDeviceSocket(device);
        }
    }

    public void connectDeviceSocket(BluetoothDevice device) {
        BluetoothSocket bluetoothSocket = null;
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(mUuid));
            if (mSocketIm != null && !mSocketIm.isRunning()) {
                mSocketIm.close();
                mSocketIm = getSocketIm(bluetoothSocket);
                mSocketIm.start();
            } else if (mSocketIm == null) {
                mSocketIm = getSocketIm(bluetoothSocket);
                mSocketIm.start();
            } else {
                Log.e("blue", "线程执行中");
                mResult.setLength(0);
                sendMsg("atdpn\r".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
            onOBDListener.connectFail();
        }

    }


    private SocketIm getSocketIm(BluetoothSocket bluetoothSocket) {
        return new SocketIm(bluetoothSocket, new ObserverSocket() {
            @Override
            public void connected() {
                if (onOBDListener != null) {
                    onOBDListener.connected();
                }

            }

            @Override
            public void connectFail() {
                if (onOBDListener != null) {
                    onOBDListener.connectFail();
                }
            }

            @Override
            public void onError(String msg) {
                if (onOBDListener != null) {
                    onOBDListener.error(msg);
                }
            }

            @Override
            public void receiveBefore() {
                mResult.setLength(0);
                int state = sendMsg("atdpn\r".getBytes());
                switch (state) {
                    case Pair.NOT_CONNECT_BLUE:
                        Log.e("blue", "无法发送数据，请先配对");
                        break;
                    case Pair.SEND_FAIL:
                        Log.e("blue", "发送数据失败");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendMsg("atdpn\r".getBytes());//重新发送一次
                        break;
                    case Pair.SEND_SUC:
                        Log.e("blue", "数据发送成功");
                        break;
                }
            }

            @Override
            public boolean receive(byte[] data) {
                boolean result = false;
                String str = new String(data);
                mResult.append(str);
                Log.e("ddddd", new String(data));
                if (str.endsWith(">")) {
                    if (mResult.toString().startsWith("atdpn")) {
                        resultType = mResult.toString().replace(" ", "").substring(5, mResult.length() - 1);
                        onError("使用指令atdpn，获取数据失败");

                        sendMsg("0902\r".getBytes());
                        result = false;
                    } else if (resultType != null) {
                        Log.e("获取obd数据", "数据结果=" + mResult.toString() + "  resultType=" + resultType);
                        String content = toStringHex2(restVin(mResult.toString()));
                        Log.e("获取obd数据", "获得obd，转化后的字符串结果---" + content);
                        if (onOBDListener != null) {
                            onOBDListener.receive(content);
                        }
                        result = true;
                    } else {
                        if (onOBDListener != null) {
                            onOBDListener.error(mResult.toString());
                        }
                        result = true;
                    }
                    mResult.setLength(0);
                }
                return result;
            }

            @Override
            public void disConnected() {
                if (onOBDListener != null) {
                    onOBDListener.disConnected();
                }
            }


        });
    }


    public int sendMsg(byte[] bytes) {
        int state = SEND_SUC;
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {

            state = NOT_CONNECT_BLUE;
        } else {
            if (mSocketIm != null) {
                boolean res = mSocketIm.sendMsg(bytes);
                if (!res) {
                    state = SEND_FAIL;
                }
            } else {
                state = NOT_CONNECT_BLUE;

            }
        }
        return state;
    }


    public String restVin(String data) {
        data = data.replace(" ", "").replace(">", "").replace("\r", "");
        if (data.startsWith("0902014")) {
            data = data.replace("0902014", "");
        }
        if (data.startsWith("0902")) {
            data = data.replace("0902", "");
        }
        return data.replace("0:", "").replace("1:", "").replace("2:", "").replace("490201", "").replace("490202", "").replace("490203", "").replace("490204", "").replace("490205", "");

    }


    // 转化十六进制编码为字符串
    public String toStringHex2(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
                        i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public void setOnOBDListener(OnOBDListener onOBDListener) {
        this.onOBDListener = onOBDListener;
    }

    public void release() {
        if (mSocketIm != null) {
            mSocketIm.close();
            mSocketIm = null;
        }
    }
}
