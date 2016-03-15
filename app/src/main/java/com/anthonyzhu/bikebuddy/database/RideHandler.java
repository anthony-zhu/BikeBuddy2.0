package com.anthonyzhu.bikebuddy.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by anthonyzhu on 2/10/16.
 *
 * Wrapper function to handle calls to SQLite database
 */
public class RideHandler extends SQLiteOpenHelper {

    private static RideHandler singleton;

    public static RideHandler getInstance(final Context context) {
        if (singleton == null) {
            singleton = new RideHandler(context.getApplicationContext());
        }
        return singleton;
    }

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "bikebuddy";
    private final Context context;

    public RideHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        Log.i("Check", "DATABASE created");
        db.execSQL(Ride.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        Log.i("Check", "DATABASE upgraded from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + Ride.TABLE_NAME);
        onCreate(db);
    }

    public void executeQuery(String query) {
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(query);
        } catch (Exception e) {
            System.out.println("DATABASE ERROR " + e);
        }

    }

    public Cursor selectQuery(String query) {
        Cursor c1 = null;
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            c1 = db.rawQuery(query, null);
        } catch (Exception e) {
            System.out.println("DATABASE ERROR " + e);
        }
        return c1;
    }

    public synchronized boolean putRide(final Ride ride) {
        boolean success = false;

        final SQLiteDatabase db = this.getWritableDatabase();

        // Update failed or wasn't possible, insert instead
        final long id = db.insert(Ride.TABLE_NAME, null,
                ride.getContent());

        if (id > -1) {
            ride.id = id;
            success = true;
        }

        return success;
    }

    public synchronized Ride getRide(final long id) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(Ride.TABLE_NAME,
                Ride.FIELDS, Ride.COL_ID + " IS ?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor == null || cursor.isAfterLast()) {
            return null;
        }

        Ride item = null;
        if (cursor.moveToFirst()) {
            item = new Ride(cursor);
        }
        cursor.close();

        return item;
    }
}