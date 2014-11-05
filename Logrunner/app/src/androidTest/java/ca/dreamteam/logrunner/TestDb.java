package ca.dreamteam.logrunner;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import static ca.dreamteam.logrunner.data.RunningContract.RunningEntry;
import static ca.dreamteam.logrunner.data.RunningContract.LocationEntry;
import ca.dreamteam.logrunner.data.RunningDbHelper;

public class TestDb extends AndroidTestCase {

    static final String TEST_LOCATION = "99705";
    static final String TEST_DATE = "20141205";
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(RunningDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new RunningDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        RunningDbHelper dbHelper = new RunningDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createNorthPoleLocationValues();
        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);
        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);
        // Data's inserted. IN THEORY. Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME, // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);

        ContentValues runningValues = createWeatherValues(locationRowId);

        long runningRowId = db.insert(RunningEntry.TABLE_NAME, null, runningValues);
        assertTrue(runningRowId != -1);

        Cursor weatherCursor = db.query(
                RunningEntry.TABLE_NAME, // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(weatherCursor, runningValues);

        dbHelper.close();
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(RunningEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(RunningEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(RunningEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(RunningEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(RunningEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(RunningEntry.COLUMN_TEMP, 75.2);
        weatherValues.put(RunningEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(RunningEntry.COLUMN_COMMENT, "Best run");
        weatherValues.put(RunningEntry.COLUMN_DISTANCE, 5.5);
        weatherValues.put(RunningEntry.COLUMN_TIME, 321);
        weatherValues.put(RunningEntry.COLUMN_START_TIME, 321);
        return weatherValues;
    }


    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);
        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
