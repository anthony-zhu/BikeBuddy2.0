package com.anthonyzhu.androidtemplate.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonyzhu.androidtemplate.R;
import com.anthonyzhu.androidtemplate.ui.base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import app.akexorcist.bluetoothspp.library.BluetoothSPP;
import app.akexorcist.bluetoothspp.library.BluetoothSPP.AutoConnectionListener;
import app.akexorcist.bluetoothspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetoothspp.library.BluetoothSPP.BluetoothStateListener;
import app.akexorcist.bluetoothspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetoothspp.library.BluetoothState;
import app.akexorcist.bluetoothspp.library.DeviceList;
import butterknife.ButterKnife;

/**
 * Activity demonstrates some GUI functionalities from the Android support library.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class MainActivity extends BaseActivity {
    private static BluetoothSPP bt;
    private double accel_x, accel_y, accel_z;
    private static double max_accel_x, max_accel_y, max_accel_z; // For testing
    private static TextView mEdisonStatus;
    private static TextView mStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupToolbar();

        // Edison Status line
        mEdisonStatus = (TextView)findViewById(R.id.main_subhead);

        // Set initial max accelerations (For testing)
        max_accel_x = 0;
        max_accel_y = 0;
        max_accel_z = 0;

        // Use Contact 'Me' to build main_status_view
        mStatusView = (TextView)findViewById(R.id.main_title);
        Cursor c = this.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        c.moveToFirst();
        int position = c.getPosition();
        if (count == 1 && position == 0) mStatusView.setText("Hello " + c.getString(c.getColumnIndex("DISPLAY_NAME")));
        c.close();

        // Check if preferences have been set
        SharedPreferences settings = getSharedPreferences(EmergencyContact.STORE_DATA, MODE_PRIVATE);
        String number = settings.getString(EmergencyContact.PHONE_NUMBER, "");
        if (number.equals("")) {
            // Open intent to Emergency Contacts
            Intent intent = new Intent(MainActivity.this, EmergencyContact.class);
            startActivity(intent);
        }

        // Initialize new BluetoothSPP (static)
        bt = new BluetoothSPP(this);

        // Check for Bluetooth availability
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        // Register BluetoothState Listener
        bt.setBluetoothStateListener(new BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                if(state == BluetoothState.STATE_CONNECTED) {
                    Log.i("Check", "State : Connected");
                    mEdisonStatus.setText("Edison is connected");
                }
                else if(state == BluetoothState.STATE_CONNECTING) {
                    Log.i("Check", "State : Connecting");
                    mEdisonStatus.setText("Edison is connecting...");
                }
                else if(state == BluetoothState.STATE_LISTEN) {
                    Log.i("Check", "State : Listen");
                    mEdisonStatus.setText("Edison is listening...");
                }
                else if(state == BluetoothState.STATE_NONE) {
                    Log.i("Check", "State : None");
                    mEdisonStatus.setText("Edison is disconnected");
                }
            }
        });

        // Register OnDataReceived Listener
        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            // JSONString: {"accel":{"x":0.847,"y":0.042,"z":-1.023}}
            public void onDataReceived(String message) {
                try {
                    // Parse incoming JSON object
                    JSONObject mainObject = new JSONObject(message);
                    JSONObject accelObject = mainObject.getJSONObject("accel");
                    accel_x = accelObject.getDouble("x");
                    accel_y = accelObject.getDouble("y");
                    accel_z = accelObject.getDouble("z");

                    // Log data
                    Log.i("Check", "x : " + accel_x + " y: " + accel_y + " z: " + accel_z);

                    // Update max acceleration values
                    if (Math.abs(accel_x) > max_accel_x)
                        max_accel_x = Math.abs(accel_x);
                    if (Math.abs(accel_y) > max_accel_y)
                        max_accel_y = Math.abs(accel_y);
                    if (Math.abs(accel_z) > max_accel_z)
                        max_accel_z = Math.abs(accel_z);

                    // Check for impact
                    if (Math.abs(accel_x) >= 4 ||
                            Math.abs(accel_y) >= 4 ||
                            Math.abs(accel_z) >= 4) {
                        // Launch dialog with countdown
                        bt.stopService();
                        createAlert();
                    }
                } catch (JSONException e) {
                    Log.e("MainActivity", "Could not parse malformed JSON");
                }
            }
        });

        // Register BluetoothConnection Listener
        bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.i("Check", "Device Connected!!");
            }

            public void onDeviceDisconnected() {
                Log.i("Check", "Device Disconnected!!");
            }

            public void onDeviceConnectionFailed() {
                Log.i("Check", "Unable to Connect!!");
            }
        });

        // Register AutoConnection Listener
        bt.setAutoConnectionListener(new AutoConnectionListener() {
            public void onNewConnection(String name, String address) {
                Log.i("Check", "New Connection - " + name + " - " + address);
            }

            public void onAutoConnectionStarted() {
                Log.i("Check", "Auto menu_connection started");
            }
        });

        // Register OnClick Listener for btnConnect
        Button btnConnect = (Button)findViewById(R.id.main_button);
        btnConnect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(MainActivity.this, DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    // TODO change to activity dialog (<activity android:theme="@android:style/Theme.Dialog">)
    // Create Alert Dialog
    public void createAlert() {
        AlertDialog.Builder alert_builder = new AlertDialog.Builder(this);
        alert_builder.setMessage("Ouch, that hurt! Are you okay?\n00:30");
        alert_builder.setCancelable(true);
        alert_builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final AlertDialog alertDialog = alert_builder.create();

        alert_builder.setTitle("Impact Alert");
        alertDialog.show();

        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                alertDialog.setMessage("Ouch, that hurt! Are you okay?\n00:"+ (millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                Log.i("Check", "Time is up!");

                SharedPreferences settings = getSharedPreferences(EmergencyContact.STORE_DATA, MODE_PRIVATE);
                String phone_number = settings.getString(EmergencyContact.PHONE_NUMBER, "");
                if (!phone_number.equals("")) {
                    Log.i("Check", "Using contact: " + phone_number);
                }

                /*
                // Build implicit phone call intent
                Uri number = Uri.parse("tel:" + phone_number);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);

                // Verify it resolves
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(callIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                // Start an activity if it's safe
                if (isIntentSafe) {
                    startActivity(callIntent);
                }
                else {
                    Toast.makeText(getApplicationContext()
                            , "Phone does not have call or text capabilities."
                            , Toast.LENGTH_SHORT).show();
                }
                */

                String message = "Test SMS message. Location details to be included later";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone_number, null, message, null, null);
                Toast.makeText(getApplicationContext(), "Text message sent.", Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }
        }.start();
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        switch (bt.getServiceState()) {
            case BluetoothState.STATE_CONNECTED:
                Log.i("Check", "State : Connected");
                mEdisonStatus.setText("Edison is connected");
                break;
            case BluetoothState.STATE_CONNECTING:
                Log.i("Check", "State : Connecting");
                mEdisonStatus.setText("Edison is connecting...");
                break;
            case BluetoothState.STATE_LISTEN:
                Log.i("Check", "State : Listen");
                mEdisonStatus.setText("Edison is listening...");
                break;
            case BluetoothState.STATE_NONE:
                Log.i("Check", "State : None");
                mEdisonStatus.setText("Edison is disconnected");
                break;
        }

        if(!bt.isBluetoothEnabled()) {
            bt.enable();
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_main;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    public void setup() { }
}
