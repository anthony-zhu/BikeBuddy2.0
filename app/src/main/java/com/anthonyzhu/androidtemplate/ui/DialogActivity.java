package com.anthonyzhu.androidtemplate.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonyzhu.androidtemplate.R;

public class DialogActivity extends Activity {
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

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
     * @param v
     */
    public void dismissDialog(View v) {
        timer.cancel();
        DialogActivity.this.finish();
    }

    /**
     * Callback method defined by the View
     * @param v
     */
    public void confirmDialog(View v) {
        timer.cancel();
        executeEmergencyMessage();
        DialogActivity.this.finish();
    }
}
