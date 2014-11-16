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

import ca.dreamteam.logrunner.Util.DeviceSelect;
import ca.dreamteam.logrunner.Util.SettingsActivity;
import ca.dreamteam.logrunner.Util.Utilities;
import ca.dreamteam.logrunner.data.RunningContract;
import ca.dreamteam.logrunner.data.RunningContract.RunningEntry;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int RUN_LOADER = 0;
    final String TAG = MainActivity.this.getClass().getSimpleName();

    // Specify the columns we need.
    private static final String[] RUN_COLUMNS = {
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
                            Intent intent = new Intent(getApplicationContext(), DeviceSelect.class);
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
                } catch (Exception e) {
                    android.util.Log.e(TAG, "getting the Bitmap" +
                            " Pixel touched for viewHistoryButton threw an exception");
                }
                if (color == Color.TRANSPARENT) return false;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) { // This should be disabled if START RUN is clicked
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "In onCreateLoader");
        String sortOrder = RunningContract.RunningEntry._ID + " DESC";
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

                double distance = data.getDouble(distanceIndex);
                String duration = data.getString(durationIndex);
                double temp = data.getDouble(tempIndex);
                double humidity = data.getDouble(humidityIndex);
                double pressure = data.getDouble(pressureIndex);

                final String humidityText = Utilities.humiFormat.format(humidity) + "%";
                ((TextView)findViewById(R.id.value_dist)).
                        setText(Utilities.convertTemp(distance, (TextView)findViewById(R.id.value_dist), MainActivity.this));
                ((TextView)findViewById(R.id.mChronometer)).
                        setText(duration);
                ((TextView)findViewById(R.id.value_temp)).
                        setText(Utilities.convertTemp(temp, (TextView)findViewById(R.id.value_temp), MainActivity.this));
                ((TextView)findViewById(R.id.value_humi)).
                        setText(humidityText);
                ((TextView)findViewById(R.id.value_baro)).
                        setText(Utilities.convertBaro(pressure, (TextView)findViewById(R.id.value_baro), MainActivity.this));
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}