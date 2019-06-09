package com.dfrobot.angelo.blunobasicdemo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SettingsActivity extends PreferenceActivity implements SettingsFragment.Sender {
    private BluetoothLeService mBluetoothLeService;
    private final static String TAG = SettingsActivity.class.getSimpleName();
    private static BluetoothGattCharacteristic mSerialPortCharacteristic;
    private static final String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        SharedPreferences prefs = this.getSharedPreferences("settings", 0);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);


    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(TAG, "Settings Activity Bound to Le Service");

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mSerialPortCharacteristic=null;
            List< BluetoothGattService > gattServices = mBluetoothLeService.getSupportedGattServices();
            if (gattServices == null) return;
            String uuid;

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.equals(SerialPortUUID)) {
                            mSerialPortCharacteristic = gattCharacteristic;
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    public void serialSend(String theString){
        mSerialPortCharacteristic.setValue(theString);
        mBluetoothLeService.writeCharacteristic(mSerialPortCharacteristic);
    }

}

