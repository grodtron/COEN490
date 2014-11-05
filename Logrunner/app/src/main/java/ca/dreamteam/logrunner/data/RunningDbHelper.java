package ca.dreamteam.logrunner.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static ca.dreamteam.logrunner.data.RunningContract.RunningEntry;
import static ca.dreamteam.logrunner.data.RunningContract.LocationEntry;

public class RunningDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
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
                RunningEntry.COLUMN_START_TIME + " INTEGER NOT NULL, " +
                RunningEntry.COLUMN_TIME + " INTEGER NOT NULL, " +
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

    }
}
