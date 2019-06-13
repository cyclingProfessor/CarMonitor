package com.dfrobot.angelo.blunobasicdemo;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;

    private TextView serialReceivedText;

    private final String Fast = String.valueOf('+');
    private final String Slow = String.valueOf('-');
    private final String Stop = String.valueOf('0');
    private final String Right = String.valueOf('R');
    private final String Left = String.valueOf('L');
    private final String Straight = String.valueOf('C');
    private final String ToggleWiFi = String.valueOf('W');
    private final String Info = String.valueOf('s');
    private boolean stoppingForPreferences = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();

        serialReceivedText= findViewById(R.id.serialReceivedText);

        ImageButton slowButton = findViewById(R.id.slowButton);
        slowButton.setOnClickListener(v -> serialSend(Slow));

        ImageButton fastButton = findViewById(R.id.fastButton);
        fastButton.setOnClickListener(v -> serialSend(Fast));

        ImageButton stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(v -> serialSend(Stop));

        ImageButton leftButton = findViewById(R.id.leftButton);
        leftButton.setOnClickListener(v -> serialSend(Left));

        ImageButton rightButton = findViewById(R.id.rightButton);
        rightButton.setOnClickListener(v -> serialSend(Right));

        ImageButton straightButton = findViewById(R.id.straightButton);
        straightButton.setOnClickListener(v -> serialSend(Straight));

        ImageButton wifiButton = findViewById(R.id.wifiButton);
        wifiButton.setOnClickListener(v -> serialSend(ToggleWiFi));

        ImageButton infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> serialSend(Info));

        Button balanceButton = findViewById(R.id.balanceButton);
        balanceButton.setOnClickListener(v -> serialSend("{L@}"));

        ToggleButton followButton = findViewById(R.id.followButton);
        followButton.setOnClickListener(v -> serialSend("{R@}"));

        Button preferencesButton = findViewById(R.id.prefButton);
        preferencesButton.setOnClickListener(v -> {
            if (getState() == connectionStateEnum.isConnected) {
                Intent intent = new Intent(this, SettingsActivity.class);
                stoppingForPreferences = true;
                serialSend(Straight);
                serialSend(Stop);
                startActivity(intent);
            }
        });

        buttonScan = findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(v ->	buttonScanOnClickProcess());
	}

    @Override
	protected void onResume(){
		super.onResume();
		stoppingForPreferences = false;
		onResumeProcess();														//onResume Process by BlunoLibrary
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		if (!stoppingForPreferences) {
            onStopProcess();                                                        //onStop Process by BlunoLibrary
        }
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConnectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}


    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        serialReceivedText.append(theString);							//append the text into the EditText
        ((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
    }
}