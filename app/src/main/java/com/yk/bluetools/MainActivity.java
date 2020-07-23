package com.yk.bluetools;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yk.blue.ObserverBlueResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_WRITE_STORAGE = 1;
    private String mPer[] = {Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,};

    private RecyclerView recyclerView;
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            return false;
        }
    });
    private BluetoothAdapter mBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        recyclerView = findViewById(R.id.recycler);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBlueTest();
            }
        });

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                Holder holder1 = (Holder) holder;
                holder1.address.setText(bluetoothDevices.get(position).getAddress());
                holder1.name.setText(bluetoothDevices.get(position).getName());
                holder1.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        blueTest.connect(bluetoothDevices.get(position),"1234");
                    }
                });
            }

            @Override
            public int getItemCount() {
                return bluetoothDevices.size();
            }

            class Holder extends RecyclerView.ViewHolder {
                private TextView name;
                private TextView address;

                Holder(@NonNull View itemView) {
                    super(itemView);
                    name = itemView.findViewById(R.id.name);
                    address = itemView.findViewById(R.id.address);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

            blueTest = new BlueTest(new ObserverBlueResult() {
                @Override
                public void startDiscovery() {
                    Log.e("MainActivity", "开始扫描");
                }

                @Override
                public void endDiscovery() {
                    Log.e("MainActivity", "停止扫描");
                }

                @Override
                public void discoveryDevice(BluetoothDevice device) {
                    if (set.contains(device.getAddress())) return;
                    set.add(device.getAddress());
                    bluetoothDevices.add(device);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    Log.e("MainActivity", "-------扫描蓝牙结果--------------" + device.getName() + "  " + device.getAddress());

                }

                @Override
                public void connected() {

                }

                @Override
                public void connectFail() {

                }

                @Override
                public void disConnected() {

                }

                @Override
                public void error(String msg) {

                }

                @Override
                public void receive(String msg) {

                }

                @Override
                public void blueOpen() {

                }

                @Override
                public void blueClose() {

                }

                @Override
                public void pairStart(BluetoothDevice device) {

                }

                @Override
                public void pairSuccess(BluetoothDevice device) {

                }

                @Override
                public void pairFail(BluetoothDevice device) {

                }

            });
    }

    @AfterPermissionGranted(RC_WRITE_STORAGE)
    private void requestPermissions() {
        if (!EasyPermissions.hasPermissions(this, mPer)) {
            PermissionRequest request = new PermissionRequest.Builder(this, RC_WRITE_STORAGE, mPer)
                    .setRationale("需要到你的应用信息里开启你手机的相关权限")
                    .setPositiveButtonText("确定")
                    .setNegativeButtonText("取消").build();
            EasyPermissions.requestPermissions(request);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle("提醒")
                    .setRationale("此app需要这些权限才能正常使用")
                    .build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (mPer != null) {
                if (EasyPermissions.hasPermissions(this, mPer)) {

                }
            }
        } else if (requestCode == 111) {

        }
    }


    private BlueTest blueTest;
    private Set<String> set = new HashSet<>();

    private void startBlueTest() {

        bluetoothDevices.clear();
        set.clear();
        recyclerView.getAdapter().notifyDataSetChanged();
        blueTest.start(this, 1000);
    }

    @Override
    protected void onDestroy() {
        if (blueTest == null) {
            blueTest.release(this);
        }
        super.onDestroy();
    }


}
