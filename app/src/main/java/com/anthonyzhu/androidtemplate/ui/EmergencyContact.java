package com.anthonyzhu.androidtemplate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.anthonyzhu.androidtemplate.R;
import com.anthonyzhu.androidtemplate.ui.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity demonstrates some GUI functionalities from the Android support library.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class EmergencyContact extends BaseActivity {

    static final int PICK_CONTACT_REQUEST = 1;  // The request code
    public static final String STORE_DATA = "com.anthonyzhu.androidtemplate.STORE_DATA";
    public static final String PHONE_NUMBER = "com.anthonyzhu.androidtemplate.PHONE_NUMBER";
    public static EditText mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);
        mEdit = (EditText)findViewById(R.id.emergency_contact_text_phonenumber);
        ButterKnife.bind(this);
        setupToolbar();

        // Restore preferences and set editText
        SharedPreferences settings = getSharedPreferences(STORE_DATA, MODE_PRIVATE);
        String phonenumber = settings.getString(PHONE_NUMBER, "");
        if (!phonenumber.equals("")) {
            mEdit.setText(phonenumber);
            Log.i("Check", "Restored contact: " + phonenumber);
        }
    }

    // When From Contacts button is pressed, set up intent for activity
    @OnClick(R.id.emergency_button)
    public void onFromContactsClicked(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));

        // Only show contacts with phone numbers
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    // Click fab to save changes
    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(STORE_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        String phonenumber = mEdit.getText().toString();
        if (!phonenumber.equals("")) {
            editor.putString(PHONE_NUMBER, phonenumber);
            Log.i("Check", "Saving contact: " + phonenumber);
        }

        // Commit the edits!
        editor.apply();
        EmergencyContact.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);

                // Set phone number field
                mEdit.setText(number);
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
        return R.id.nav_emergency_contact;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }
}
