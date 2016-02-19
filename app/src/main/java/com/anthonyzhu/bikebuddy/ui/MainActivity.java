package com.anthonyzhu.bikebuddy.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonyzhu.bikebuddy.R;
import com.anthonyzhu.bikebuddy.database.Ride;
import com.anthonyzhu.bikebuddy.database.RideHandler;
import com.anthonyzhu.bikebuddy.ui.base.BaseActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
public class MainActivity extends BaseActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private double accel_x, accel_y, accel_z;

    private static TextView mEdisonStatus;
    private static TextView mLatitudeView;
    private static TextView mLongitudeView;
    private static TextView mLocationNear;
    private static TextView mTotalDistanceView;
    private static TextView mAverageSpeedView;
    private static Chronometer mChronometer;

    private static BluetoothSPP bt;
    private Location accLoc;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected float mTotalDistance;
    protected float mAverageSpeed;
    int mRideTime;
    protected Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates;
    protected Boolean isRunning;
    boolean hasConnectedWifi;
    boolean hasConnectedMobile;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String DISTANCE_KEY = "distance-key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupToolbar();

        // Set up Location Services
        mRequestingLocationUpdates = false;

        // Set up accumulative distance and average speed
        mTotalDistance = 0;
        mAverageSpeed = 0;
        mRideTime = 0;
        // TODO: Set savedInstanceState?

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        createLocationRequest();

        // Edison Status line
        mEdisonStatus = (TextView) findViewById(R.id.main_subhead);
        mLatitudeView = (TextView) findViewById(R.id.main_latitude);
        mLongitudeView = (TextView) findViewById(R.id.main_longitude);
        mLocationNear = (TextView) findViewById(R.id.main_speed);
        mTotalDistanceView = (TextView) findViewById(R.id.main_total_distance);
        mAverageSpeedView = (TextView) findViewById(R.id.main_average_speed);
        mChronometer = (Chronometer) findViewById(R.id.main_timer);


        // Use Contact 'Me' to build main_status_view
        Cursor c = this.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        if (c != null) {
            int count = c.getCount();
            c.moveToFirst();
            int position = c.getPosition();
            if (count == 1 && position == 0) {
                String user_greeting = "Hello " + c.getString(c.getColumnIndex("DISPLAY_NAME"));
                TextView mStatusView = (TextView) findViewById(R.id.main_title);
                mStatusView.setText(user_greeting);
            }
            c.close();
        }

        // Check if preferences have been set
        SharedPreferences settings = getSharedPreferences(EmergencyContact.STORE_DATA, MODE_PRIVATE);
        String number = settings.getString(EmergencyContact.PHONE_NUMBER, "");
        if (number.equals("")) {
            // Open intent to Emergency Contacts
            Intent intent = new Intent(MainActivity.this, EmergencyContact.class);
            startActivity(intent);
        }


        // Initialize new BluetoothSPP
        if (bt == null) {
            bt = new BluetoothSPP(this);
        }

        // Check for Bluetooth availability
        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        // Register BluetoothState Listener
        bt.setBluetoothStateListener(new BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                switch (state) {
                    case BluetoothState.STATE_CONNECTED:
                        Log.i("Check", "State : Connected");
                        mEdisonStatus.setText(getResources().getString(R.string.edison_connected));
                        break;
                    case BluetoothState.STATE_CONNECTING:
                        Log.i("Check", "State : Connecting");
                        mEdisonStatus.setText(getResources().getString(R.string.edison_connecting));
                        break;
                    case BluetoothState.STATE_LISTEN:
                        Log.i("Check", "State : Listen");
                        mEdisonStatus.setText(getResources().getString(R.string.edison_listening));
                        break;
                    case BluetoothState.STATE_NONE:
                        Log.i("Check", "State : None");
                        mEdisonStatus.setText(getResources().getString(R.string.edison_none));
                        break;
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

                    Log.i("Check", "x: " + accel_x + ", y: " + accel_y + ", z: " + accel_z);
                    // Check for impact
                    if (Math.sqrt(Math.pow(accel_x, 2) + Math.pow(accel_z, 2)) >= 8.0) {

                        // Turn off Bluetooth service
                        bt.disconnect();

                        // Disconnect Location service
                        if (mGoogleApiClient.isConnected()) {
                            stopLocationUpdates();
                        }
                        mRequestingLocationUpdates = false;

                        // Save ride
                        saveRide();

                        // Launch Dialog Activity
                        Intent intent = new Intent(MainActivity.this, DialogActivity.class);
                        if (mCurrentLocation != null) {
                            intent.putExtra("CURRENT_LATITUDE", mCurrentLocation.getLatitude());
                            intent.putExtra("CURRENT_LONGITUDE", mCurrentLocation.getLongitude());
                        }
                        startActivity(intent);
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
                Toast.makeText(getApplicationContext()
                        , "Device Disconnected"
                        , Toast.LENGTH_SHORT).show();
                Log.i("Check", "Device Disconnected!!");
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Device Connection Failed"
                        , Toast.LENGTH_SHORT).show();
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

        // Register OnClick Listener for btnMain
        Button btnMain = (Button) findViewById(R.id.main_button);
        btnMain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    isRunning = false;

                    // Turn off Bluetooth service
                    bt.disconnect();

                    // Disconnect Location service
                    if (mGoogleApiClient.isConnected()) {
                        stopLocationUpdates();
                    }
                    mRequestingLocationUpdates = false;

                    // Save ride
                    saveRide();
                } else {
                    isRunning = true;
                    Intent intent = new Intent(MainActivity.this, DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i("MainActivity", "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mTotalDistance from the Bundle and update the UI to show the
            // correct distance.
            if (savedInstanceState.keySet().contains(DISTANCE_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mTotalDistance = savedInstanceState.getFloat(DISTANCE_KEY);
            }

            // TODO updateUI() on orientation change
        }
    }

    protected synchronized void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
        }
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Alternatively:
        // mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    // The final argument to {@code requestLocationUpdates()} is a LocationListener
    // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("MainActivity", "Permission not granted");
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        int stoppedMilliseconds = 0;
        String chronoText = mChronometer.getText().toString();
        String array[] = chronoText.split(":");
        if (array.length == 2) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
                    + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000
                    + Integer.parseInt(array[1]) * 60 * 1000
                    + Integer.parseInt(array[2]) * 1000;
        }

        mChronometer.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);
        mChronometer.start();
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        mChronometer.stop();

        int elapsed = 0;
        String chronoText = mChronometer.getText().toString();
        String array[] = chronoText.split(":");
        if (array.length == 2) {
            elapsed = Integer.parseInt(array[0]) * 60 * 1000
                    + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
            elapsed = Integer.parseInt(array[0]) * 60 * 60 * 1000
                    + Integer.parseInt(array[1]) * 60 * 1000
                    + Integer.parseInt(array[2]) * 1000;
        }
        mRideTime = elapsed;
        mAverageSpeed = 1000 * 3600 * mTotalDistance / elapsed;
        String average_speed = "Your average speed was " + mAverageSpeed + " mph";
        mAverageSpeedView.setText(average_speed);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("MainActivity", "Connected to GoogleApiClient");
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            //double temp_distance = 0;
            double acc = location.getAccuracy();

            hasConnectedWifi = false;
            hasConnectedMobile = false;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        hasConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        hasConnectedMobile = true;
            }

            if (hasConnectedWifi || hasConnectedMobile) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                Address address;
                List<Address> list = null;
                try {
                    list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    address = list.get(0);
                    String locationNearString = "Your address is near " + address.getAddressLine(0) + ", " + address.getLocality();
                    mLocationNear.setText(locationNearString);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            if (isRunning) {
                if (accLoc == null) {
                    accLoc = location;
                }
                if (accLoc.distanceTo(location) > 2.5 * acc) {
                    double temp_adistance = accLoc.distanceTo(location);
                    Log.i("Check", "Latitude: " + location.getLatitude() + ", Longitude: " +
                            location.getLongitude() + ", Distance: " + temp_adistance);
                    mTotalDistance += temp_adistance / 1609.344;
                    accLoc = location;
                }
            }

            if (!isRunning) {
                float acc2 = location.getAccuracy();
                if (accLoc.distanceTo(location) > 1.2 * acc2) {
                    double temp_adistance = accLoc.distanceTo(location);
                    Log.i("Check", "Latitude: " + location.getLatitude() + ", Longitude: " +
                            location.getLongitude() + ", Distance: " + temp_adistance);
                    mTotalDistance += temp_adistance / 1609.344;
                    accLoc = location;
                }
            }

            String currentLatitudeText = "Current latitude is " + location.getLatitude();
            String currentLongitudeText = "Current longitude is " + location.getLongitude();
            mLatitudeView.setText(currentLatitudeText);
            mLongitudeView.setText(currentLongitudeText);
        }
        //double temp_speed = location.getSpeed() * 3600 / 1609.344;

        // Update UI
        //mLocationNear.setText("Current speed is " + temp_speed + " mph");
        String totalDistanceText = "Total distance is " + mTotalDistance + " miles";
        mTotalDistanceView.setText(totalDistanceText);

        mCurrentLocation = location;

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Log.d("MainActivity", "Connection : Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the 'Handle Connection Failures' sectioon
        Log.d("MainActivity", "Connection : Failed");
    }

    public void onDestroy() {
        mRequestingLocationUpdates = false;
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        super.onDestroy();
        // Turn off Bluetooth service
        bt.disconnect();

        // Disconnect Location service
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        mGoogleApiClient.disconnect();
        mRequestingLocationUpdates = false;

        // Save ride
        saveRide();
    }

    public void onStart() {
        super.onStart();
        switch (bt.getServiceState()) {
            case BluetoothState.STATE_CONNECTED:
                Log.i("Check", "State : Connected");
                mEdisonStatus.setText(getResources().getString(R.string.edison_connected));
                break;
            case BluetoothState.STATE_CONNECTING:
                Log.i("Check", "State : Connecting");
                mEdisonStatus.setText(getResources().getString(R.string.edison_connecting));
                break;
            case BluetoothState.STATE_LISTEN:
                Log.i("Check", "State : Listen");
                mEdisonStatus.setText(getResources().getString(R.string.edison_listening));
                break;
            case BluetoothState.STATE_NONE:
                Log.i("Check", "State : None");
                mEdisonStatus.setText(getResources().getString(R.string.edison_none));
                break;
        }
        mGoogleApiClient.connect();

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
            if(resultCode == Activity.RESULT_OK) {
                mRequestingLocationUpdates = true;
                startLocationUpdates();
                bt.connect(data);
            }
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

    public void saveRide() {
        // Create a new ride.
        Ride ride = new Ride();

        // Set date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss aa", Locale.US);
        Calendar c = Calendar.getInstance();
        Date presentTime = c.getTime();
        String currentDate = dateFormat.format(presentTime);
        String currentTime = timeFormat.format(presentTime);

        ride.date = currentDate;
        ride.time = currentTime;

        // Set distance
        ride.distance = mTotalDistance;

        // Set average speed
        ride.averageSpeed = mAverageSpeed;

        // Set ride time
        int rideInSeconds = mRideTime / 1000;
        ride.rideTime = rideInSeconds / 3600 + ":" + (rideInSeconds % 3600) / 60 + ":" + rideInSeconds % 60;

        // Set good stops (not useful?)
        ride.goodStops = 140;

        // Set bad stops (not useful?)
        ride.badStops = 5;

        // Set rating (not useful?)
        ride.rating = 4;

        if (RideHandler.getInstance(this).putRide(ride)) {
            Toast.makeText(getApplicationContext()
                    , "Ride saved!"
                    , Toast.LENGTH_LONG).show();
        }

    }
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putFloat(DISTANCE_KEY, mTotalDistance);
        super.onSaveInstanceState(savedInstanceState);
    }
}
