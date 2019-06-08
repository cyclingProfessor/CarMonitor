package com.dfrobot.angelo.blunobasicdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public abstract  class BlunoLibrary  extends Activity{

	private final Context mainContext=this;
	
	protected abstract void onConnectionStateChange(connectionStateEnum theConnectionStateEnum);
	protected abstract void onSerialReceived(String theString);
	void serialSend(String theString){
		if (mConnectionState == connectionStateEnum.isConnected) {
			mSCharacteristic.setValue(theString);
			mBluetoothLeService.writeCharacteristic(mSCharacteristic);
		}
	}

	private BleScanner scanner;

	private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    private BluetoothLeService mBluetoothLeService;
	private BluetoothAdapter mBluetoothAdapter;
	public enum connectionStateEnum{isNull, isScanning, isToScan, isConnecting , isConnected, isDisconnecting}

	private connectionStateEnum mConnectionState = connectionStateEnum.isNull;
	private static final int REQUEST_ENABLE_BT = 1;

	private final Handler mHandler= new Handler();

    private final static String TAG = BlunoLibrary.class.getSimpleName();
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			System.out.println("mServiceConnection onServiceConnected");
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (mBluetoothLeService.notInitialized()) {
				Log.e(TAG, "Unable to notInitialized Bluetooth");
				((Activity) mainContext).finish();
			}
			scanner = new BleScanner((BlunoLibrary) mainContext, mBluetoothLeService, mBluetoothAdapter, mHandler, mConnectingOverTimeRunnable);
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			System.out.println("mServiceConnection onServiceDisconnected");
			mBluetoothLeService = null;
		}
	};

	public void setState(connectionStateEnum state) {
		mConnectionState = state;
		onConnectionStateChange(state);
	}

	// We could fail to connect - so check after a time.
    private final Runnable mConnectingOverTimeRunnable=new Runnable(){
		@Override
		public void run() {
        	if(mConnectionState==connectionStateEnum.isConnecting) {
				mConnectionState = connectionStateEnum.isToScan;
				onConnectionStateChange(mConnectionState);
				mBluetoothLeService.close();
			}
		}};

	// We could fail to disconnect - so check after a time.
    private final Runnable mDisconnectingOverTimeRunnable =new Runnable(){
		@Override
		public void run() {
        	if(mConnectionState==connectionStateEnum.isDisconnecting) {
				mConnectionState = connectionStateEnum.isToScan;
				onConnectionStateChange(mConnectionState);
				mBluetoothLeService.close();
			}
		}};

	private static final String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
	private static final String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
    private static final String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";
	
    void onCreateProcess()
    {
    	if(!initiate())
		{
			Toast.makeText(mainContext, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			((Activity) mainContext).finish();
		}

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    void onResumeProcess() {
    	System.out.println("BlUNOActivity onResume");
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	    mainContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

	}

	// Pause does not necessarily want to kill the bluetooth connection
	void onPauseProcess() {
    	System.out.println("BLUNOActivity onPause");
		scanner.scanLeDevice(false);
		mainContext.unregisterReceiver(mGattUpdateReceiver);
    	mConnectionState=connectionStateEnum.isToScan;
    	onConnectionStateChange(mConnectionState);
		scanner.dismiss();
		if(mBluetoothLeService!=null)
		{
			mBluetoothLeService.disconnect();
            mHandler.postDelayed(mDisconnectingOverTimeRunnable, 10000);
		}
		mSCharacteristic=null;

	}

	void onStopProcess() {
		System.out.println("MiUnoActivity onStop");
		if(mBluetoothLeService!=null)
		{
        	mHandler.removeCallbacks(mDisconnectingOverTimeRunnable);
			mBluetoothLeService.close();
		}
		mSCharacteristic=null;
	}

	void onDestroyProcess() {
        mainContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
	}
	
	void onActivityResultProcess(int requestCode, int resultCode) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			((Activity) mainContext).finish();
		}
	}

	private boolean initiate()
	{
		if (!mainContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}
		
		final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
		assert bluetoothManager != null;
		mBluetoothAdapter = bluetoothManager.getAdapter();
	
		return (mBluetoothAdapter != null);
	}
	
	 // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
		@Override
        public void onReceive(Context context, Intent intent) {
        	final String action = intent.getAction();
            System.out.println("mGattUpdateReceiver->onReceive->action="+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnectionState = connectionStateEnum.isConnected;
            	mHandler.removeCallbacks(mConnectingOverTimeRunnable);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnectionState = connectionStateEnum.isToScan;
                onConnectionStateChange(mConnectionState);
            	mHandler.removeCallbacks(mDisconnectingOverTimeRunnable);
            	mBluetoothLeService.close();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            	for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
            		System.out.println("ACTION_GATT_SERVICES_DISCOVERED  "+
            				gattService.getUuid().toString());
            	}
            	getGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	if(mSCharacteristic==mModelNumberCharacteristic)
            	{
            		if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
						mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, false);
						mSCharacteristic=mCommandCharacteristic;
						String mPassword = "AT+PASSWOR=DFRobot\r\n";
						mSCharacteristic.setValue(mPassword);
						mBluetoothLeService.writeCharacteristic(mSCharacteristic);
						//set the default baud rate to 115200
						int mBaudrate = 115200;
						String mBaudrateBuffer = "AT+CURRUART=" + mBaudrate + "\r\n";
						mSCharacteristic.setValue(mBaudrateBuffer);
						mBluetoothLeService.writeCharacteristic(mSCharacteristic);
						mSCharacteristic=mSerialPortCharacteristic;
						mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
						mConnectionState = connectionStateEnum.isConnected;
						onConnectionStateChange(mConnectionState);
						
					}
            		else {
            			Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
                        mConnectionState = connectionStateEnum.isToScan;
                        onConnectionStateChange(mConnectionState);
					}
            	}
            	else if (mSCharacteristic==mSerialPortCharacteristic) {
            		onSerialReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				}
            }
        }
    };
	
    void buttonScanOnClickProcess()
    {
    	switch (mConnectionState) {
		case isNull:
			mConnectionState=connectionStateEnum.isScanning;
			onConnectionStateChange(mConnectionState);
			scanner.scanLeDevice(true);
			break;
		case isToScan:
			mConnectionState=connectionStateEnum.isScanning;
			onConnectionStateChange(mConnectionState);
			scanner.scanLeDevice(true);
			break;
		case isScanning:
			break;

		case isConnecting:
			break;
		case isConnected:
			mBluetoothLeService.disconnect();
            mHandler.postDelayed(mDisconnectingOverTimeRunnable, 10000);
            mConnectionState=connectionStateEnum.isDisconnecting;
			onConnectionStateChange(mConnectionState);
			break;
		case isDisconnecting:
			break;

		default:
			break;
		}
    }

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions, @NonNull int[] grantResults) {
		// If request is cancelled, the result arrays are empty.
		if (requestCode == BleScanner.MY_PERMISSIONS_REQUEST_LOCATION_FOR_BLE) {
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				scanner.scanLeDevice(true);
			} else {
				Toast.makeText(mainContext, R.string.require_location_for_ble,
						Toast.LENGTH_SHORT).show();
				((Activity) mainContext).finish();
			}
		}
	}


	private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        mModelNumberCharacteristic=null;
        mSerialPortCharacteristic=null;
        mCommandCharacteristic=null;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
				switch (uuid) {
					case ModelNumberStringUUID:
						mModelNumberCharacteristic = gattCharacteristic;
						break;
					case SerialPortUUID:
						mSerialPortCharacteristic = gattCharacteristic;
						break;
					case CommandUUID:
						mCommandCharacteristic = gattCharacteristic;
						break;
				}
            }
        }
        
        if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
			Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
            mConnectionState = connectionStateEnum.isToScan;
            onConnectionStateChange(mConnectionState);
		}
        else {
        	mSCharacteristic=mModelNumberCharacteristic;
        	mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
        	mBluetoothLeService.readCharacteristic(mSCharacteristic);
		}
        
    }
    
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
