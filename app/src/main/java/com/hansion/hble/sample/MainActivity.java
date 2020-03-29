package com.hansion.hble.sample;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hansion.h_ble.BleController;
import com.hansion.h_ble.callback.ConnectCallback;
import com.hansion.h_ble.callback.ScanCallback;
import com.hansion.hble.R;
import com.hansion.hble.sample.adapter.DeviceListAdapter;

import java.util.ArrayList;
import java.util.List;

import permison.PermissonUtil;
import permison.listener.PermissionListener;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ProgressDialog progressDialog;
    private BleController mBleController;
    private static String LOGTAG = "AppCompatActivity";
    //搜索结果列表
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<BluetoothDevice>();
    private ListView mDeviceList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        getSupportActionBar().setTitle("Ble For AiThinker");

        mDeviceList = (ListView) findViewById(R.id.mDeviceList);

        requestPermission();


    }

    private void requestPermission() {

        PermissonUtil.checkPermission(this, new PermissionListener() {
            @Override
            public void havePermission() {
                initBleScan();
            }

            @Override
            public void requestPermissionFail() {
                Toast.makeText(mContext, "您拒绝了开启摄像头和读写权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION);

    }


    private void initBleScan() {
        // TODO  第一步：初始化
        mBleController = BleController.getInstance().init(this);

        // TODO  第二步：搜索设备，获取列表后进行展示
        scanDevices();

    }


    private void scanDevices() {

        showProgressDialog("Please Wait", "Searching...");
        mBleController.scanBle(0, new ScanCallback() {
            @Override
            public void onSuccess() {
                hideProgressDialog();
                if (bluetoothDevices.size() > 0) {
                    mDeviceList.setAdapter(new DeviceListAdapter(MainActivity.this, bluetoothDevices));
                    mDeviceList.setOnItemClickListener(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Search Device Lists empty！", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //过滤是否含有 Ai-Thinker 名字的蓝牙设备
                if (device.getName() != null && device.getName().contains("Ai-Thinker"))
                    if (!bluetoothDevices.contains(device)) {
                        bluetoothDevices.add(device);
                    }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }

    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, title, message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        showProgressDialog("Please Wait", "Connectting...");

        // TODO 第三步：点击条目后,获取地址，根据地址连接设备
        String address = bluetoothDevices.get(i).getAddress();
        mBleController.connect(0, address, new ConnectCallback() {
            @Override
            public void onConnSuccess() {
                hideProgressDialog();
                Toast.makeText(MainActivity.this, "connected!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SendAndReciveActivity.class));
            }

            @Override
            public void onConnFailed() {
                hideProgressDialog();
                Toast.makeText(MainActivity.this, "connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
