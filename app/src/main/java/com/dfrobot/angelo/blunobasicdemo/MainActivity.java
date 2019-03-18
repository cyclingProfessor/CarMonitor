package com.dfrobot.angelo.blunobasicdemo;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;
    private ImageButton slowButton;
    private ImageButton fastButton;
    private ImageButton stopButton;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton straightButton;
    private ImageButton wifiButton;
    private ImageButton infoButton;

	private TextView serialReceivedText;

    private final String Fast = String.valueOf('+');
    private final String Slow = String.valueOf('-');
    private final String Stop = String.valueOf('0');
    private final String Right = String.valueOf('R');
    private final String Left = String.valueOf('L');
    private final String Straight = String.valueOf('C');
    private final String ToggleWiFi = String.valueOf('W');
    private final String Info = String.valueOf('s');

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();

        serialBegin(115200);

        serialReceivedText=(TextView) findViewById(R.id.serialReceivedText);

        slowButton=(ImageButton) findViewById(R.id.slowButton);
        slowButton.setOnClickListener(v -> serialSend(Slow));

        fastButton=(ImageButton) findViewById(R.id.fastButton);
        fastButton.setOnClickListener(v -> serialSend(Fast));

        stopButton=(ImageButton) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(v -> serialSend(Stop));

        leftButton=(ImageButton) findViewById(R.id.leftButton);
        leftButton.setOnClickListener(v -> serialSend(Left));

        rightButton=(ImageButton) findViewById(R.id.rightButton);
        rightButton.setOnClickListener(v -> serialSend(Right));

        straightButton=(ImageButton) findViewById(R.id.straightButton);
        straightButton.setOnClickListener(v -> serialSend(Straight));

        wifiButton=(ImageButton) findViewById(R.id.wifiButton);
        wifiButton.setOnClickListener(v -> serialSend(ToggleWiFi));

        infoButton=(ImageButton) findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> serialSend(Info));

        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(v ->	buttonScanOnClickProcess());
	}

    @Override
	protected void onResume(){
		super.onResume();
		onResumeProcess();														//onResume Process by BlunoLibrary
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
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