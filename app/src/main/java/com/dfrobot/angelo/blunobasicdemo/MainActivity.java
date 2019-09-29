package com.dfrobot.angelo.blunobasicdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

public class MainActivity  extends BlunoLibrary {
    private final static String TAG = SettingsActivity.class.getSimpleName();

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
    private final String SetFollow = "R{A1}";
    private final String SetNoFollow = "R{A0}";
    private boolean stoppingForPreferences = false;
    private ToggleButton followButton;
    private TextView frontRight, frontLeft, rear;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();

        frontRight = findViewById(R.id.frontRightDistance);
        frontLeft = findViewById((R.id.frontLeftDistance));
        rear = findViewById((R.id.rearDistance));

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

        followButton = findViewById(R.id.followButton);
        followButton.setOnClickListener(v -> serialSend(followButton.isChecked() ? SetFollow : SetNoFollow));

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
            serialSend("!"); // Notify the Car that we have just connected and need a status message
            serialSend(Stop);
            serialSend(SetNoFollow);
            followButton.setChecked(false);
            serialReceivedText.setText("");

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            StringBuffer parameterString = new StringBuffer(String.format(Locale.UK, "{CR%03d%03d%03d%03d%03d%03d}",
                    sharedPref.getInt("L_Min", 0), sharedPref.getInt("L_Max", 100),
                    2 * sharedPref.getInt("A_Min", 0), 2 * sharedPref.getInt("A_Max", 100),
                    2 * sharedPref.getInt("B_Min", 0), 2 * sharedPref.getInt("B_Max", 100)));


            serialSend(String.valueOf(parameterString));

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


	private boolean inStatusResponse = false;
    private StringBuilder status_message;

    /**********************************************************************
     *
     * @param theString the string to process
     *
     *                  The string may contain the special characters [ or ] which contain a command for the application.
     *                  Anything not between these characters is just appended to the scroll buffer.
     *                  It is possible for any single command to span several calls
     *                  It is also possible for multiple commands to be contained in single call (hence the recursion)
     */
    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        Log.d(TAG, "Received: " + theString);
        continueSerialReceived(theString, 0);
    }

    public void continueSerialReceived(String theString, int start) {							//Once connection data received, this function will be called
        Log.d(TAG, "Received: " + theString);

        if (inStatusResponse) {
            int posn = theString.indexOf(']', start);
            if (posn >= 0) {
                Log.d(TAG, "Found end of message at: " + theString);

                inStatusResponse = false;
                status_message.append(theString.substring(start, posn));
                process_message(status_message);
                continueSerialReceived(theString, posn + 1);                            //append the text into the EditText
            } else {
                status_message.append(theString.substring(start));
            }
        } else {
            int posn = theString.indexOf('[', start);
            if (posn >= 0) {
                Log.d(TAG, "Found start of message at: " + theString);
                inStatusResponse = true;
                if (posn > 0) { // There are some non-status characters
                    serialReceivedText.append(theString.substring(start, posn));                            //append the text into the EditText
                }
                status_message = new StringBuilder();
                continueSerialReceived(theString,  posn + 1);                            //append the text into the EditText
            } else {
                serialReceivedText.append(theString.substring(start));                            //append the text into the EditText
            }
        }
//        serialReceivedText.append(theString);                            //append the text into the EditText
        ((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
    }

    private void process_message(StringBuilder msg) {
        switch (msg.charAt(0)) {
            case 'E': // E is for Echo
                int dist = 10000;
                try {
                    dist = Integer.parseInt(msg.substring(2));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Bad distance: " + msg);
                }
                TextView widget = rear;
                switch (msg.charAt(1)) {
                    case 'R':
                        widget = frontRight;
                        break;
                    case 'L':
                        widget = frontLeft;
                        break;
                    case 'B':
                        widget = rear;
                        break;
                    default:
                        Log.e(TAG, "Bad Sensor name in Echo Message: " + msg);
                        break;
                }
                widget.setText(dist > 2400 ? "Inf" : msg.substring(2));
            default:
                Log.e(TAG, "Bad status message: " + msg);

                break;
        }
    }
}