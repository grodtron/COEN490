package ca.dreamteam.logrunner.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import static ca.dreamteam.logrunner.data.RunningContract.RunningEntry;
import static ca.dreamteam.logrunner.data.RunningContract.LocationEntry;

public class RunningDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "run.db";
    private static final String LOG_TAG = RunningDbHelper.class.getSimpleName();

    public RunningDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations. A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING +") ON CONFLICT IGNORE"+
                " );";
        final String SQL_CREATE_RUN_TABLE = "CREATE TABLE " + RunningEntry.TABLE_NAME + " (" +
                RunningEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                // the ID of the location entry associated with this run data
                RunningEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                RunningEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                RunningEntry.COLUMN_COMMENT + " TEXT NOT NULL, " +
                RunningEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                RunningEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                RunningEntry.COLUMN_TEMP + " REAL NOT NULL, " +
                RunningEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                RunningEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                RunningEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +
                RunningEntry.COLUMN_START_TIME + " TEXT NOT NULL, " +
                RunningEntry.COLUMN_RATING + " REAL NOT NULL, " +
                RunningEntry.COLUMN_DURATION + " TEXT NOT NULL, " +
                RunningEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                RunningEntry.COLUMN_IMAGE + " BLOB, " +

                // Set up the location column as a foreign key to location table.
                "FOREIGN KEY (" + RunningEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "));";

        Log.d(LOG_TAG, SQL_CREATE_LOCATION_TABLE);
        Log.d(LOG_TAG, SQL_CREATE_RUN_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RUN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RunningEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public static long addRunInfo(
            String title, String date, String comment, String duration, String start_time,
            double temperature, double pressure, double humidity, double distance, double rating,
            ContentResolver cr) {

        ContentValues runValues = new ContentValues();

        runValues.put(RunningEntry.COLUMN_TITLE, title);
        runValues.put(RunningEntry.COLUMN_DATETEXT, date);
        runValues.put(RunningEntry.COLUMN_COMMENT, comment);
        runValues.put(RunningEntry.COLUMN_DURATION, duration);
        runValues.put(RunningEntry.COLUMN_START_TIME, start_time);
        runValues.put(RunningEntry.COLUMN_TEMP, temperature);
        runValues.put(RunningEntry.COLUMN_HUMIDITY, humidity);
        runValues.put(RunningEntry.COLUMN_DISTANCE, distance);
        runValues.put(RunningEntry.COLUMN_PRESSURE, pressure);
        runValues.put(RunningEntry.COLUMN_RATING, rating);

        runValues.put(RunningEntry.COLUMN_MAX_TEMP, 75);
        runValues.put(RunningEntry.COLUMN_MIN_TEMP, 65);
        runValues.put(RunningEntry.COLUMN_LOC_KEY, 0);

        Uri runInsertUri = cr
                .insert(RunningEntry.CONTENT_URI, runValues);

        // Notify new entry in the table
        cr.notifyChange(RunningEntry.CONTENT_URI, null);
        return ContentUris.parseId(runInsertUri);
    }
}
