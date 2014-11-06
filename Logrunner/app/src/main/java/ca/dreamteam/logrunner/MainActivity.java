package ca.dreamteam.logrunner;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

import ca.dreamteam.logrunner.data.RunningContract;
import ca.dreamteam.logrunner.data.RunningContract.RunningEntry;
import ti.android.ble.sensortag.DeviceSelectActivity;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int RUN_LOADER = 0;
    final String TAG = MainActivity.this.getClass().getSimpleName();

    // Mock DB data
    static final String TEST_DATE = "20141205";
    static final String COMMENT = "wow such run";
    static final double TEMP = 25.3;
    static final double PRESSURE = 100.1;
    static final String TIME = "00:01";
    static final int START_TIME = 15;
    static final double HUMIDITY = 30;
    static final double DISTANCE = 3.5;

    // Specify the columns we need.
    private static final String[] RUN_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying. On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            RunningEntry.TABLE_NAME + "." + RunningEntry._ID,
            RunningEntry.COLUMN_DATETEXT,
            RunningEntry.COLUMN_COMMENT,
            RunningEntry.COLUMN_TEMP,
            RunningEntry.COLUMN_DISTANCE,
            RunningEntry.COLUMN_PRESSURE,
            RunningEntry.COLUMN_TIME,
            RunningEntry.COLUMN_HUMIDITY,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find ImageView-elements
        ImageView startRunButton = (ImageView) findViewById(R.id.StartRun);
        startRunButton.setDrawingCacheEnabled(true);
        ImageView viewHistoryButton = (ImageView) findViewById(R.id.ViewHistory);
        viewHistoryButton.setDrawingCacheEnabled(true);

        startRunButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
                ImageView startRunButtonImageView = (ImageView) v;
                int color = 0;
                try {
                    color = bmp.getPixel((int) event.getX(), (int) event.getY());
                } catch(Exception e) {
                    android.util.Log.e(TAG,"getting the Bitmap" +
                            " Pixel touched for startRunButton threw an exception");
                }
                if(color == Color.TRANSPARENT) return false;
                else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            startRunButtonImageView.setImageResource(R.drawable.startrun_pressed);
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(getApplicationContext(), StartRunActivity.class);
                            intent.putExtra(DeviceSelectActivity.EXTRA_DEVICE,
                                    getIntent().getParcelableExtra(DeviceSelectActivity.EXTRA_DEVICE));
                            startActivity(intent);
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });

        viewHistoryButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
                ImageView viewHistoryButtonImageView = (ImageView) v;
                int color = 0;
                try {
                    color = bmp.getPixel((int) event.getX(), (int) event.getY());
                } catch(Exception e) {
                    android.util.Log.e(TAG,"getting the Bitmap" +
                            " Pixel touched for viewHistoryButton threw an exception");
                }
                if(color == Color.TRANSPARENT) return false;
                else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            viewHistoryButtonImageView.setImageResource(R.drawable.viewhistory_pressed);
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(getApplicationContext(), ViewHistoryActivity.class);
                            startActivity(intent);
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });
//        addRunInfo(TEST_DATE, COMMENT, TEMP, PRESSURE, START_TIME, START_TIME, HUMIDITY, DISTANCE);
        getLoaderManager().initLoader(RUN_LOADER, null, this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        ImageView startRunButton = (ImageView) findViewById(R.id.StartRun);
        startRunButton.setImageResource(R.drawable.startrun);

        ImageView viewHistoryButton = (ImageView) findViewById(R.id.ViewHistory);
        viewHistoryButton.setImageResource(R.drawable.viewhistory);
        getLoaderManager().restartLoader(RUN_LOADER, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();
        if (id == R.id.action_settings) { // This should be disabled if START RUN is clicked
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. This
        // fragment only uses one loader, so we don't care about checking the id.
        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        Log.v(TAG, "In onCreateLoader");
        String startDate = RunningContract.getDbDateString(new Date());
        // Sort order: Ascending, by date.
        String sortOrder = RunningContract.RunningEntry._ID + " DESC";
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                this,
                RunningContract.RunningEntry.CONTENT_URI,
                RUN_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                int distanceIndex =
                        data.getColumnIndex(RunningEntry.COLUMN_DISTANCE);
                int durationIndex =
                        data.getColumnIndex(RunningEntry.COLUMN_TIME);
                int tempIndex =
                        data.getColumnIndex(RunningEntry.COLUMN_TEMP);
                int humidityIndex =
                        data.getColumnIndex(RunningEntry.COLUMN_HUMIDITY);
                int pressureIndex =
                        data.getColumnIndex(RunningEntry.COLUMN_PRESSURE);

                String distance = data.getString(distanceIndex);
                String duration = data.getString(durationIndex);
                String temp = data.getString(tempIndex);
                String humidity = data.getString(humidityIndex);
                String pressure = data.getString(pressureIndex);

                ((TextView)findViewById(R.id.value_dist)).
                        setText(distance);
                ((TextView)findViewById(R.id.mChronometer)).
                        setText(TIME);
                ((TextView)findViewById(R.id.value_temp)).
                        setText(temp);
                ((TextView)findViewById(R.id.value_humi)).
                        setText(humidity);
                ((TextView)findViewById(R.id.value_baro)).
                        setText(pressure);
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * @param date The location string
     * @param comment description of the run
     * @param temperature temperature measured by sensor
     * @param pressure pressure measured by sensor
     * @param time duration of the run
     * @param start_time time at which the run was started
     * @param distance total distance travelled
     * @return the row ID of the added run.
     */
    private long addRunInfo(
            String date, String comment, double temperature, double pressure,
            int time, int start_time, double humidity, double distance) {

        ContentValues runValues = new ContentValues();

        runValues.put(RunningEntry.COLUMN_DATETEXT, date);
        runValues.put(RunningEntry.COLUMN_COMMENT, comment);
        runValues.put(RunningEntry.COLUMN_TEMP, temperature);
        runValues.put(RunningEntry.COLUMN_TIME, time);
        runValues.put(RunningEntry.COLUMN_START_TIME, start_time);
        runValues.put(RunningEntry.COLUMN_HUMIDITY, humidity);
        runValues.put(RunningEntry.COLUMN_DISTANCE, distance);
        runValues.put(RunningEntry.COLUMN_PRESSURE, pressure);

        // Not displayed info
        runValues.put(RunningEntry.COLUMN_MAX_TEMP, 75);
        runValues.put(RunningEntry.COLUMN_MIN_TEMP, 65);
        runValues.put(RunningEntry.COLUMN_LOC_KEY, 0);

        Uri runInsertUri = getContentResolver()
                .insert(RunningEntry.CONTENT_URI, runValues);

        // Notify new entry in the table
        getContentResolver().notifyChange(RunningEntry.CONTENT_URI, null);

        return ContentUris.parseId(runInsertUri);
    }

}