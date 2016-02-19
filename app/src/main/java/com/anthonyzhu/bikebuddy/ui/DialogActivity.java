package com.anthonyzhu.bikebuddy.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonyzhu.bikebuddy.R;

public class DialogActivity extends Activity {
    private CountDownTimer timer;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if(alert == null) {
            Log.i("Warning", "No alarm set");

            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // I can't see this ever being null (as always have a default notification)
            // but just incase
            if (alert == null) {
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

                if (alert == null) {
                    alert = RingtoneManager.getValidRingtoneUri(this);
                }
            }
        }

        mp = MediaPlayer.create(getApplicationContext(), alert);
        mp.setLooping(true);
        mp.start();

        timer = new CountDownTimer(30000, 1000) {
            TextView mTextField = (TextView)findViewById(R.id.dialog_text);
            public void onTick(long millisUntilFinished) {
                String message = "Seconds remaining: " + (millisUntilFinished / 1000);
                mTextField.setText(message);
            }

            public void onFinish() {
                // mTextField.setText("done!");
                Log.i("Check", "Time is up!");

                executeEmergencyMessage();
                mp.stop();
                DialogActivity.this.finish();
            }
        };

        timer.start();
    }

    private void executeEmergencyMessage() {
        SharedPreferences settings = getSharedPreferences(EmergencyContact.STORE_DATA, MODE_PRIVATE);
        String phone_number = settings.getString(EmergencyContact.PHONE_NUMBER, "");
        if (!phone_number.equals("")) {
            Log.i("Check", "Using contact: " + phone_number);
        }

        Intent original_intent = getIntent();
        double latitude = original_intent.getDoubleExtra("CURRENT_LATITUDE", 0.0);
        double longitude = original_intent.getDoubleExtra("CURRENT_LONGITUDE", 0.0);

        String message = "I've been in a biking accident. I am at (" + latitude + ", " + longitude + ").";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone_number, null, message, null, null);
        Toast.makeText(getApplicationContext(), "Text message sent.", Toast.LENGTH_LONG).show();

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
    }

    /**
     * Callback method defined by the View
     * @param v view from button
     */
    public void dismissDialog(View v) {
        timer.cancel();
        mp.stop();
        DialogActivity.this.finish();
    }

    /**
     * Callback method defined by the View
     * @param v view from button
     */
    public void confirmDialog(View v) {
        timer.cancel();
        mp.stop();
        executeEmergencyMessage();
        DialogActivity.this.finish();
    }
}
