package com.dfrobot.angelo.blunobasicdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class BleScanner {
    private final Context mainContext;
    private final static String TAG = BleScanner.class.getSimpleName();

    static final int MY_PERMISSIONS_REQUEST_LOCATION_FOR_BLE = 76543;
    private final BluetoothAdapter mBluetoothAdapter;

    private final LeDeviceListAdapter mLeDeviceListAdapter;
    private AlertDialog mScanDeviceDialog;
    private String mDeviceAddress;

    private boolean mScanning = false;

    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private final ArrayList<BluetoothDevice> mLeDevices;
        private final LayoutInflater mInflater;

        LeDeviceListAdapter(Context parent) {
            super();
            mLeDevices = new ArrayList<>();
            mInflater = ((Activity) parent).getLayoutInflater();
        }

        void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = view
                        .findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    void dismiss() {
        mScanDeviceDialog.dismiss();
    }


    void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            System.out.println("mBluetoothAdapter.startLeScan");

            if (mLeDeviceListAdapter != null) {
                mLeDeviceListAdapter.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
            }

            if (!mScanning) {
                if (ContextCompat.checkSelfPermission(mainContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) mainContext,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION_FOR_BLE);
                } else {

                    mScanning = true;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            }
            mScanDeviceDialog.show();
        } else {
            if (mScanning) {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            mLeDeviceListAdapter.clear();
        }
    }

    // Device scan callback.
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int unused,
                             byte[] scanRecord) {
            ((Activity) mainContext).runOnUiThread(() -> {
                System.out.println("mLeScanCallback onLeScan run ");
                mLeDeviceListAdapter.addDevice(device);
                mLeDeviceListAdapter.notifyDataSetChanged();
            });
        }
    };

    BleScanner(BlunoLibrary parent, BluetoothLeService leService, BluetoothAdapter adapter, Handler mHandler, Runnable mConnectingOverTimeRunnable) {
        mainContext = parent;
        mBluetoothAdapter = adapter;
        mLeDeviceListAdapter = new LeDeviceListAdapter(parent);
        mScanDeviceDialog = new android.app.AlertDialog.Builder(parent)
                .setTitle("BLE Device Scan...").setAdapter(mLeDeviceListAdapter, (dialog, which) -> {
                    final BluetoothDevice device = mLeDeviceListAdapter.getDevice(which);
                    if (device == null)
                        return;
                    scanLeDevice(false);

                    if(device.getName()==null || device.getAddress()==null)
                    {
                        parent.setState(BlunoLibrary.connectionStateEnum.isToScan);
                    }
                    else{
                        mDeviceAddress= device.getAddress();

                        if (leService.connect(mDeviceAddress)) {
                            Log.d(TAG, "Connect request success");
                            parent.setState(BlunoLibrary.connectionStateEnum.isConnecting);
                            mHandler.postDelayed(mConnectingOverTimeRunnable, 10000);
                        }
                        else {
                            Log.d(TAG, "Connect request fail");
                            parent.setState(BlunoLibrary.connectionStateEnum.isToScan);
                        }
                    }
                })
                .setOnCancelListener(arg0 -> {
                    System.out.println("mBluetoothAdapter.stopLeScan");

                    parent.setState(BlunoLibrary.connectionStateEnum.isToScan);
                    mScanDeviceDialog.dismiss();

                    scanLeDevice(false);
                }).create();

    }
}
