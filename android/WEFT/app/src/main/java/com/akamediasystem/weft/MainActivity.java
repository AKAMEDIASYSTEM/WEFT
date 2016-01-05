package com.akamediasystem.weft;

import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.v4.app.NotificationCompat;

import java.util.UUID;

public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    // State machine
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;
    private final static String TAG = MainActivity.class.getSimpleName();

    private int state;

    private boolean scanStarted;
    private boolean scanning;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private RFduinoService rfduinoService;

    private Button enableBluetoothButton;
    private TextView scanStatusText;
    private Button scanButton;
    private TextView deviceInfoText;
    private TextView connectionStatusText;
    private Button connectButton;
    private Button clearButton;
    private LinearLayout dataLayout;

    // notif stuff
    private NotificationReceiver nReceiver;
    private TextView txtView;

    class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("notification_event") + "\n" + txtView.getText();
            txtView.setText(temp);
        }
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (state == BluetoothAdapter.STATE_ON) {
                upgradeState(STATE_DISCONNECTED);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                downgradeState(STATE_BLUETOOTH_OFF);
            }
        }
    };

    private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanning = (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);
            scanStarted &= scanning;
            updateUi();
        }
    };

    private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            rfduinoService = ((RFduinoService.LocalBinder) service).getService();
            if (rfduinoService.initialize()) {
                if (rfduinoService.connect(bluetoothDevice.getAddress())) {
                    upgradeState(STATE_CONNECTING);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rfduinoService = null;
            downgradeState(STATE_DISCONNECTED);
        }
    };

    private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
                upgradeState(STATE_CONNECTED);
            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
                downgradeState(STATE_DISCONNECTED);
            } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
                addData(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView) findViewById(R.id.textView);
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.akamediasystem.weft.NOTIFICATION_LISTENER_EXAMPLE");
        registerReceiver(nReceiver,filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // this was jsut to satisfy some curiosity about Bonded devices (seems like BLE doesn't facilitate bonding)
        Log.i(TAG, "bonded device list "+ bluetoothAdapter.getBondedDevices().toString());

                // Bluetooth
        enableBluetoothButton = (Button) findViewById(R.id.enableBluetooth);
        enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableBluetoothButton.setEnabled(false);
                enableBluetoothButton.setText(
                        bluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!");
            }
        });

        // Find Device
        scanStatusText = (TextView) findViewById(R.id.scanStatus);

        scanButton = (Button) findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanStarted = true;
                bluetoothAdapter.startLeScan(
                        new UUID[]{ RFduinoService.UUID_SERVICE },
                        MainActivity.this);
            }
        });

        // Device Info
        deviceInfoText = (TextView) findViewById(R.id.deviceInfo);

        // Connect Device
        connectionStatusText = (TextView) findViewById(R.id.connectionStatus);

        connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                connectionStatusText.setText("Connecting...");
                Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
                // AKA NOTE this may need to be "startService" in order for svc to persist
                // once activity is destroyed (ie, for svc to persist forever)
                bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);
            }
        });

        // Receive
        clearButton = (Button) findViewById(R.id.clearData);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataLayout.removeAllViews();
            }
        });

        dataLayout = (LinearLayout) findViewById(R.id.dataLayout);
        SeekBar seekBarFreq = (SeekBar)findViewById(R.id.seekBarFreq);
        final TextView seekBarValue = (TextView)findViewById(R.id.textViewFreq);
        seekBarFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText("Frequency "+ String.valueOf(progress));
                rfduinoService.send(HexAsciiHelper.hexToBytes("00" + String.valueOf(progress)));
                Log.d(TAG, "changed freq bar");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        SeekBar seekBarAmp = (SeekBar)findViewById(R.id.seekBarAmp);
        final TextView seekBarAmpValue = (TextView)findViewById(R.id.textViewAmplitude);
        seekBarAmp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText("Amplitude "+ String.valueOf(progress));
                rfduinoService.send(HexAsciiHelper.hexToBytes("01" + String.valueOf(progress)));
                Log.d(TAG, "changed amp bar");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        SeekBar seekBarDuty = (SeekBar)findViewById(R.id.seekBarDuty);
        final TextView seekBarDutyValue = (TextView)findViewById(R.id.textViewDuty);
        seekBarDuty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText("Duty Cycle "+ String.valueOf(progress));
                rfduinoService.send(HexAsciiHelper.hexToBytes("03" + String.valueOf(progress)));
                Log.d(TAG, "changed duty bar");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

        updateState(bluetoothAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
    }

    @Override
    protected void onStop() {
        super.onStop();

        bluetoothAdapter.stopLeScan(this);

        unregisterReceiver(scanModeReceiver);
        unregisterReceiver(bluetoothStateReceiver);
        unregisterReceiver(rfduinoReceiver);
    }

    protected void onDestroy(){
        super.onDestroy();
//        if(!rfduinoServiceConnection.equals(null)) {
//            unbindService(rfduinoServiceConnection);
//        }
        unregisterReceiver(nReceiver);
    }
    private void upgradeState(int newState) {
        if (newState > state) {
            updateState(newState);
        }
    }

    private void downgradeState(int newState) {
        if (newState < state) {
            updateState(newState);
        }
    }

    private void updateState(int newState) {
        state = newState;
        updateUi();
    }

    private void updateUi() {
        // Enable Bluetooth
        boolean on = state > STATE_BLUETOOTH_OFF;
        enableBluetoothButton.setEnabled(!on);
        enableBluetoothButton.setText(on ? "Bluetooth enabled" : "Enable Bluetooth");
        scanButton.setEnabled(on);

        // Scan
        if (scanStarted && scanning) {
            scanStatusText.setText("Scanning...");
            scanButton.setText("Stop Scan");
            scanButton.setEnabled(true);
        } else if (scanStarted) {
            scanStatusText.setText("Scan started...");
            scanButton.setEnabled(false);
        } else {
            scanStatusText.setText("");
            scanButton.setText("Scan");
            scanButton.setEnabled(true);
        }

        // Connect
        boolean connected = false;
        String connectionText = "Disconnected";
        if (state == STATE_CONNECTING) {
            connectionText = "Connecting...";
        } else if (state == STATE_CONNECTED) {
            connected = true;
            connectionText = "Connected";
        }
        connectionStatusText.setText(connectionText);
        connectButton.setEnabled(bluetoothDevice != null && state == STATE_DISCONNECTED);

        // Send
//        sendZeroButton.setEnabled(connected);
//        sendValueButton.setEnabled(connected);
    }

    private void addData(byte[] data) {
        View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, dataLayout, false);

        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        text1.setText(HexAsciiHelper.bytesToHex(data));

        String ascii = HexAsciiHelper.bytesToAsciiMaybe(data);
        if (ascii != null) {
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text2.setText(ascii);
        }

        dataLayout.addView(
                view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        bluetoothAdapter.stopLeScan(this);
        bluetoothDevice = device;

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInfoText.setText(
                        BluetoothHelper.getDeviceInfoText(bluetoothDevice, rssi, scanRecord));
                updateUi();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_sine:
                if (checked)
                    rfduinoService.send(HexAsciiHelper.hexToBytes("0200"));
                break;
            case R.id.radio_square:
                if (checked)
                    rfduinoService.send(HexAsciiHelper.hexToBytes("0201"));
                break;
            case R.id.radio_saw_a:
                if (checked)
                    rfduinoService.send(HexAsciiHelper.hexToBytes("0202"));
                break;
            case R.id.radio_saw_d:
                if (checked)
                    rfduinoService.send(HexAsciiHelper.hexToBytes("0203"));
                break;
            case R.id.radio_noise:
                if (checked)
                    rfduinoService.send(HexAsciiHelper.hexToBytes("0204"));
                break;
        }
    }

    public void buttonClicked(View v){

        if(v.getId() == R.id.btnCreateNotify){
            NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
            ncomp.setContentTitle("WEFT notification");
            ncomp.setContentText("Time is "+System.currentTimeMillis());
            ncomp.setTicker("Notification Listener Service Example");
            ncomp.setSmallIcon(R.mipmap.ic_launcher);
            ncomp.setAutoCancel(true);
            nManager.notify((int)System.currentTimeMillis(),ncomp.build());
        }
        else if(v.getId() == R.id.btnClearNotify){
            Intent i = new Intent("com.akamediasystem.weft.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
            i.putExtra("command","clearall");
            sendBroadcast(i);
        }
        else if(v.getId() == R.id.btnListNotify){
            Intent i = new Intent("com.akamediasystem.weft.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
            i.putExtra("command","list");
            sendBroadcast(i);
        }


    }
}

