package ca.dreamteam.logrunner;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import static ca.dreamteam.logrunner.data.RunningContract.LocationEntry;
import static ca.dreamteam.logrunner.data.RunningContract.RunningEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                RunningEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );
        Cursor cursor = mContext.getContentResolver().query(
                RunningEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertReadProvider() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ContentValues testValues = TestDb.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted. IN THEORY. Now pull some out to stare at it and verify it made
        // the round trip.
        // A cursor is your primary interface to the query results.
        Cursor cursor =  mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI, // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, testValues);


        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        ContentValues runningValues = TestDb.createWeatherValues(locationRowId);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(RunningEntry.CONTENT_URI, runningValues);
        assertTrue(weatherInsertUri != null);

        Cursor runningCursor = mContext.getContentResolver().query(
                RunningEntry.CONTENT_URI, // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestDb.validateCursor(runningCursor, runningValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        addAllContentValues(runningValues, testValues);

        // Get the joined Weather and Location data
        runningCursor = mContext.getContentResolver().query(
                RunningEntry.buildRunLocation(TestDb.TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        TestDb.validateCursor(runningCursor, runningValues);

        // Get the joined Weather and Location data with a start date
        runningCursor = mContext.getContentResolver().query(
                RunningEntry.buildRunLocationWithStartDate(
                        TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        TestDb.validateCursor(runningCursor, runningValues);

        // Get the joined Weather data for a specific date
        runningCursor = mContext.getContentResolver().query(
                RunningEntry.buildRunLocationWithDate(TestDb.TEST_LOCATION, TestDb.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(runningCursor, runningValues);

    }

    public void testGetType() {
        // content://ca.dreamteam.logrunner/weather/
        String type = mContext.getContentResolver().getType(RunningEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(RunningEntry.CONTENT_TYPE, type);
        String testLocation = "94074";
        // content://ca.dreamteam.logrunner/weather/94074
        type = mContext.getContentResolver().getType(
                RunningEntry.buildRunLocation(testLocation));
        // vnd.android.cursor.dir/ca.dreamteam.logrunner/weather
        assertEquals(RunningEntry.CONTENT_TYPE, type);
        String testDate = "20140612";
        // content://ca.dreamteam.logrunner/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                RunningEntry.buildRunLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/ca.dreamteam.logrunner/weather
        assertEquals(RunningEntry.CONTENT_ITEM_TYPE, type);
        // content://com.example.ca.dreamteam.logrunner/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.ca.dreamteam.logrunner/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);
        // content://com.example.ca.dreamteam.logrunner/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/ca.dreamteam.logrunner/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDb.createNorthPoleLocationValues();
        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);
        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);
        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");
        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(count, 1);
        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, updatedValues);
    }
    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }

}
