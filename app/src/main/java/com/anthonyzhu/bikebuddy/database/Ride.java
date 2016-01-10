package com.anthonyzhu.bikebuddy.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by anthony zhu on 1/9/16.
 */
public class Ride {
    // SQL convention says Table name should be "singular", so not Rides
    public static final String TABLE_NAME = "Ride";
    // Naming the id column with an underscore is good to be consistent
    // with other Android things. This is ALWAYS needed
    public static final String COL_ID = "_id";

    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";
    public static final String COL_TOTAL_DISTANCE = "distance";
    public static final String COL_TOP_SPEED = "topSpeed";
    public static final String COL_AVERAGE_SPEED = "averageSpeed";
    public static final String COL_RIDE_TIME = "totalTime";
    public static final String COL_ELEVATION_CHANGE = "elevationChange";
    public static final String COL_GOOD_STOPS = "goodStops";
    public static final String COL_BAD_STOPS = "badStops";
    public static final String COL_RATING = "rating";

    // For database projection so order is consistent
    public static final String[] FIELDS = { COL_ID, COL_DATE, COL_TIME, COL_TOTAL_DISTANCE,
            COL_TOP_SPEED, COL_AVERAGE_SPEED, COL_RIDE_TIME, COL_ELEVATION_CHANGE,
            COL_GOOD_STOPS, COL_BAD_STOPS, COL_RATING };

    /*
     * The SQL code that creates a Table for storing Persons in.
     * Note that the last row does NOT end in a comma like the others.
     * This is a common source of error.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_DATE + " TEXT NOT NULL DEFAULT '',"
                    + COL_TIME + " TEXT NOT NULL DEFAULT '',"
                    + COL_TOTAL_DISTANCE + " INTEGER DEFAULT 0,"
                    + COL_TOP_SPEED  + " REAL DEFAULT 0.0,"
                    + COL_AVERAGE_SPEED + "REAL DEFAULT 0.0,"
                    + COL_RIDE_TIME + " TEXT NOT NULL DEFAULT '',"
                    + COL_ELEVATION_CHANGE + " REAL DEFAULT 0.0,"
                    + COL_GOOD_STOPS + " REAL DEFAULT 0.0,"
                    + COL_BAD_STOPS + " INTEGER DEFAULT 0,"
                    + COL_RATING + " INTEGER DEFAULT 5"
                    + ")";

    // Fields corresponding to database columns
    public long id = -1;
    public String date = "";
    public String time = "";
    public float distance = 0;
    public float topSpeed = 0;
    public float avSpeed = 0;
    public String rideTime = "";
    public float elevationChange = 0;
    public float goodStops = 0;
    public int badStops = 0;
    public int rating = 0;

    /**
     * No need to do anything, fields are already set to default values above
     */
    public Ride() {
    }

    /**
     * Convert information from the database into a Ride object.
     */
    public Ride(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.date = cursor.getString(1);
        this.time = cursor.getString(2);
        this.distance = cursor.getFloat(3);
        this.topSpeed = cursor.getFloat(4);
        this.avSpeed = cursor.getFloat(5);
        this.rideTime = cursor.getString(6);
        this.elevationChange = cursor.getFloat(7);
        this.goodStops = cursor.getFloat(8);
        this.badStops = cursor.getInt(9);
        this.rating = cursor.getInt(10);
    }

    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(COL_DATE, date);
        values.put(COL_TIME, time);
        values.put(COL_TOTAL_DISTANCE, distance);
        values.put(COL_TOP_SPEED, topSpeed);
        values.put(COL_AVERAGE_SPEED, avSpeed);
        values.put(COL_RIDE_TIME, rideTime);
        values.put(COL_ELEVATION_CHANGE, elevationChange);
        values.put(COL_GOOD_STOPS, goodStops);
        values.put(COL_BAD_STOPS, badStops);
        values.put(COL_RATING, rating);

        return values;
    }
}
