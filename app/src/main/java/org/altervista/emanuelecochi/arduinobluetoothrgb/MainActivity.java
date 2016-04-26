package org.altervista.emanuelecochi.arduinobluetoothrgb;

import org.altervista.emanuelecochi.arduinobluetoothrgb.controller.MyBroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Define BluetoothAdapter object that allows you to interface with the mobile phone bluetooth
    private BluetoothAdapter btAdapter;
    //Define a Set of BluetoothDevice objects that representing the remote bluetooth devices
    private Set<BluetoothDevice> devices;
    // Define BroadcastReceiver object to handle events bluetooth
    private MyBroadcastReceiver btReceiver = new MyBroadcastReceiver();
    // ID Intent BluetoothAdapter.ACTION_REQUEST_ENABLE if bluetooth disable
    private static final int REQUEST_ENABLE_BT = 1;
    // BluetoothSocket object is the communication channel between your smartphone and
    // the associated device
    private BluetoothSocket btSocket = null;
    // Define OutputStream need to send data to Arduino
    private OutputStream writer;
    // Define a color array to send the RGB values
    private byte[] color = new byte[3];
    // Define ArrayAdapter<String> that allows the graphic representation of data
    // and interaction with them
    public static ArrayAdapter<String> adapter = null;
    // List View variable
    private ListView lv;
    // SeekBar variable
    private SeekBar red,green,blue;
    // Button variables
    private Button disconnect,resetLed,findDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the ListView element
        lv = (ListView)findViewById(R.id.listViewDevice);
        adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1);
        lv.setAdapter(adapter);

        // Add a click listener
        if (lv != null) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // Get the element clicked and connect to it
                    String listElement = (String) parent.getItemAtPosition(position);
                    connect(listElement);
                }
            });
        }

        findDevice = (Button)findViewById(R.id.btnFind);
        disconnect = (Button)findViewById(R.id.btnDisconnect);
        resetLed = (Button)findViewById(R.id.btnResetLed);
        if (disconnect != null)
            disconnect.setEnabled(false);
        if (resetLed != null)
            resetLed.setEnabled(false);

        red = (SeekBar)findViewById(R.id.seekBarRed);
        green = (SeekBar)findViewById(R.id.seekBarGreen);
        blue = (SeekBar)findViewById(R.id.seekBarBlue);
        red.setEnabled(false);
        green.setEnabled(false);
        blue.setEnabled(false);

        // Add a change listener for SeekBar red
        if (red != null) {
            red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                // Send seekBarRed value to Arduino
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    color[0] = (byte)progress;
                    send(color);
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

        // Add a change listener for SeekBar green
        if (green != null) {
            green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                // Send seekBarGreen value to Arduino
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    color[1] = (byte) progress;
                    send(color);
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

        // Add a change listener for SeekBar blue
        if (blue != null) {
            blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                // Send seekBarBlue value to Arduino
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    color[2] = (byte) progress;
                    send(color);
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
    }

    public void onResume() {
        super.onResume();
        // get the bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // check if the device has bluetooth capabilities
        // if not, display a toast message and close the app
        if (btAdapter == null) {
            Toast.makeText(this, "This app requires a bluetooth capable phone", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Register the MyBroadcastReceiver for two notifications
        IntentFilter deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter discoveryFinishedfilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btReceiver, deviceFoundFilter);
        registerReceiver(btReceiver, discoveryFinishedfilter);
    }

    public void onPause() {
        super.onPause();
        // Unregister the MyBroadcastReceiver
        unregisterReceiver(btReceiver);
    }

    public void onDestroy() {
        super.onDestroy();
        if (btSocket!=null && btSocket.isConnected())
            // Disconnect the BluetoothSocket
            disconnect(getWindow().getDecorView().getRootView());
    }

    // Start scan devices
    public void scan(View v) {
        // If bluetooth is not enable send Intent for activate it, else starts to find devices
        if(!btAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn,REQUEST_ENABLE_BT);
        } else startDiscoveryDevice();
    }

    @Override
    // Manages intent results
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_ENABLE_BT && resultCode==RESULT_OK)
            startDiscoveryDevice();
    }

    // Start discovery devices
    public void startDiscoveryDevice() {
        adapter.clear();
        btAdapter.startDiscovery();
    }

    // Method that establish a serial connection to the device and send
    // data to Arduino
    private void connect(String deviceName) {
        // Define the Serial Port Profile UUID (short form 0x1101)
        UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        // Get the Bluetooth device with the given name
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;
        for(BluetoothDevice pairedDevice : pairedDevices) {
            if (pairedDevice.getName().equals(deviceName)) {
                targetDevice = pairedDevice;
                break;
            }
        }

        // If the device was not found, toast an error and return
        if(targetDevice == null) {
            Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a connection to the device with the SPP UUID
        try {
            btSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
            Toast.makeText(this, "Unable to open a serial socket with the device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Connect to the device
        try {
            btSocket.connect();
            // Empty ListView
            btAdapter.cancelDiscovery();
            // Enable SeekBars
            red.setEnabled(true);
            green.setEnabled(true);
            blue.setEnabled(true);
            // Enable Button: Disconnect,Reset Led
            disconnect.setEnabled(true);
            resetLed.setEnabled(true);
            // Disable Button Find Devices
            findDevice.setEnabled(false);
            adapter.clear();
            Toast.makeText(this, "Controll LED enable", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to connect to the device", Toast.LENGTH_SHORT).show();
        }
    }

    // Reset the values of SeekBars
    public void resetLed(View v) {
        red.setProgress(0);
        green.setProgress(0);
        blue.setProgress(0);
    }

    // Disconnect the BluetoothSocket
    public void disconnect(View v) {
        if (btSocket.isConnected()) {
            try {
                resetLed(getWindow().getDecorView().getRootView());
                if (writer!=null)
                    // Close the OutputStream
                    writer.close();
                // Close BluetoothSocket
                btSocket.close();
                // Disable Button: Disconnect, Reset Led
                disconnect.setEnabled(false);
                resetLed.setEnabled(false);
                // Enable Button Find Devices
                findDevice.setEnabled(true);
                // Disable SeekBars
                red.setEnabled(false);
                green.setEnabled(false);
                blue.setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send data RGB to Arduino
    public void send(byte[] color) {
        try {
            writer = btSocket.getOutputStream();
            writer.write(color);
            Log.d("R: ", String.valueOf(color[0]));
            Log.d("G: ", String.valueOf(color[1]));
            Log.d("B: ", String.valueOf(color[2]));
            writer.flush();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to send message to the device", Toast.LENGTH_SHORT).show();
        }
    }
}
