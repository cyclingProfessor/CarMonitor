package cyclingProfessor.CarMonitor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import static android.bluetooth.le.ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;

class BleScanner {
    private final Context mainContext;
    private final static String TAG = BleScanner.class.getSimpleName();

    static final int MY_PERMISSIONS_REQUEST_LOCATION_FOR_BLE = 76543;

    private final Handler mHandler;
    private AlertDialog mScanDeviceDialog;
    private String mDeviceAddress;
    private BluetoothLeService mLeService;
    private BlunoLibrary mParent;
    private Runnable connectingOverTimeRunnable;
    private final BluetoothLeScanner bluetoothLeScanner;
    private boolean mScanning = false;


    void scanLeDevice(final boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            if (!mScanning) {
                if (ContextCompat.checkSelfPermission(mainContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) mainContext,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION_FOR_BLE);
                } else {
                    mHandler.postDelayed(() -> {
                            if (mScanning) {
                                mScanning = false;

                                bluetoothLeScanner.stopScan(mLeScanCallback);
                                Log.d(TAG, "Scan Timed Out");
                                mParent.setState(BlunoLibrary.connectionStateEnum.isToScan);
                            }
                    }, 5000);
                    mScanning = true;
                    ScanSettings.Builder settings = new ScanSettings.Builder()
                    .setNumOfMatches(MATCH_NUM_ONE_ADVERTISEMENT)
                            .setScanMode(SCAN_MODE_LOW_LATENCY);
                    String BlunoAdd = "BC:6A:29:36:4B:E2";
                    List<ScanFilter> filters = new ArrayList<>();
                    filters.add(new ScanFilter.Builder().setDeviceAddress(BlunoAdd).build());
                    bluetoothLeScanner.startScan(filters, settings.build(), mLeScanCallback);
                }

            }
        } else {
            if (mScanning) {
                mScanning = false;
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }
        }

    }

    // Device scan callback.
    private final ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mScanning = false;

            if (mLeService.connect(result.getDevice().getAddress())) {
                Log.d(TAG, "Connect request success");
                bluetoothLeScanner.stopScan(mLeScanCallback);
                mParent.setState(BlunoLibrary.connectionStateEnum.isConnecting);
                mHandler.postDelayed(connectingOverTimeRunnable, 10000);
            }
            else {
                Log.d(TAG, "Connect request fail");
                mParent.setState(BlunoLibrary.connectionStateEnum.isToScan);
            }


        }
    };

    BleScanner(BlunoLibrary parent, BluetoothLeService leService, BluetoothAdapter adapter, Handler mHandler, Runnable mConnectingOverTimeRunnable) {
        mainContext = parent;
        this.mHandler = mHandler; 
        mLeService = leService;
        bluetoothLeScanner = adapter.getBluetoothLeScanner();
        connectingOverTimeRunnable = mConnectingOverTimeRunnable;
        mParent = parent;
    }
}
