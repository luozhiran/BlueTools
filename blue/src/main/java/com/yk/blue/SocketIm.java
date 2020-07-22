package com.yk.blue;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class SocketIm extends Thread {

    private final static int START = 1;
    private final static int ERROR = 2;
    private final static int FINISH = 3;
    private BluetoothSocket mBluetoothSocketDevice;
    private boolean mIsInterrupt;
    private static int STATE = 0;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ObserverSocket mOnConnectListener;
    private final Object mLock = new Object();

    public SocketIm(BluetoothSocket device, ObserverSocket onConnectListener) {
        this.mBluetoothSocketDevice = device;
        mOnConnectListener = onConnectListener;
    }

    @Override
    public void run() {
        synchronized (mBluetoothSocketDevice) {
            STATE = START;
            boolean connectResult = connect(mBluetoothSocketDevice);
            if (connectResult) {
                handleData(mBluetoothSocketDevice);
            } else {
                STATE = ERROR;
                if (mOnConnectListener != null)
                    mOnConnectListener.onError("retry connect still failed");
            }
        }
    }


    /**
     * 返回 true连接成功
     *
     * @param bluetoothSocket
     * @return
     */
    private boolean connect(BluetoothSocket bluetoothSocket) {
        try {
            bluetoothSocket.connect();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SocketIm", "connect thread occur error..........." + e.getMessage());
            Class<?> clazz = bluetoothSocket.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothSocket.getRemoteDevice(), params);
                Thread.sleep(500);
                bluetoothSocket.connect();
                Log.e("SocketIm", "retry connect success....");
                return true;
            } catch (Exception e1) {
                e1.printStackTrace();
                Log.e("SocketIm", "retry connect still failed.......");
                return false;
            }
        }
    }


    private void handleData(BluetoothSocket bluetoothSocket) {
        if (mOnConnectListener == null) return;
        mOnConnectListener.connected();
        boolean connect = true;
        if (inputStream == null && outputStream == null) {
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                connect = false;
                STATE = ERROR;
                e.printStackTrace();
            }
        }
        if (connect && mOnConnectListener != null) {
            mOnConnectListener.receiveBefore();
        }
        int bytes;
        byte[] buffer = new byte[1024];
        while (connect) {
            try {
                //读取数据
                mIsInterrupt = true;
                bytes = inputStream.read(buffer);
                if (bytes != -1) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);
                    if (mOnConnectListener.receive(data)) {
                        throw new Exception("释放资源");
                    }
                }
            } catch (Exception e) {
                Log.e("SocketIm", "read data occur error........" + e.getMessage());
                mIsInterrupt = false;
                release();
                STATE = ERROR;
                break;
            }
        }

        mIsInterrupt = false;
        STATE = FINISH;
    }


    /**
     * 发送数据，这个是我由于我的硬件要求哈
     * <p>
     * AT 指令全为大写，均以回车、换行字符结尾，所有发送给 QBD 芯片的指令必须在指令结尾附带一个
     * 回车符（0x0D）作为指令结束的标志，否则 QBD 芯片不响应该指令。（另 所有空格将被忽略。如
     * ATV 和 AT V 芯片默认为同一指令）同样 QBD61 的回传数据也是回车符（0x0D）作为结束标志 注意：
     * 在连接上车辆后,由于汽车总线速度的限制，发送给 ECU 指令的频率不能过快，特别是 K 线，建议
     * 上位机判断 QBD61 的响应再进行下一个指令的发送，
     *
     * @param msg
     */
    public boolean sendMsg(final String msg) {
        //如果硬件对指令没有特殊要求，用这个就好 byte[] bytes = msg.getBytes();
        byte[] bytes = msg.getBytes();
        try {
            if (outputStream != null) {
                outputStream.write(bytes);
                outputStream.flush();
            }
            return true;
        } catch (IOException e) {
            Log.e("SocketIm", "send msg occur error:" + e.getMessage());
            return false;
        }

    }


    public boolean sendMsg(byte[] bytes) {
        try {
            if (outputStream != null) {
                outputStream.write(bytes);
                outputStream.flush();
            }
            return true;
        } catch (IOException e) {
            Log.e("SocketIm", "send msg occur error:" + e.getMessage());
            return false;
        }
    }


    public boolean isRunning() {
        if (STATE == START) {
            return true;
        } else {
            return false;
        }
    }

    public void close() {
        if (STATE == FINISH) {
            release();
        } else {
            if (mIsInterrupt) {
                interrupt();
            }
        }
    }


    private void release() {
        if (inputStream != null) {
            try {
                inputStream.close();
                outputStream.close();
                inputStream = null;
                mBluetoothSocketDevice.close();
                mBluetoothSocketDevice = null;
                mOnConnectListener.disConnected();
                mOnConnectListener = null;
                STATE = 0;
                mIsInterrupt = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
