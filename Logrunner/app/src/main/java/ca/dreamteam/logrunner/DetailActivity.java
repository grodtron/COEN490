package ca.dreamteam.logrunner;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import ca.dreamteam.logrunner.Util.DeviceSelect;
import ca.dreamteam.logrunner.Util.SettingsActivity;
import ca.dreamteam.logrunner.Util.Utilities;
import ca.dreamteam.logrunner.data.RunningContract;
import ca.dreamteam.logrunner.data.RunningContract.RunningEntry;

public class DetailActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int RUN_LOADER = 0;
    public static final String ID_KEY = "run_id";
    final String TAG = DetailActivity.this.getClass().getSimpleName();
    private static final String SHARE_HASHTAG = " #Logrunner";
    private String mRunStr;
    private ShareActionProvider mShareActionProvider;
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

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
        setContentView(R.layout.activity_detail);
        getLoaderManager().initLoader(RUN_LOADER, null, this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getLoaderManager().restartLoader(RUN_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();

        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareRunIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

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

    private Intent createShareRunIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mRunStr + SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "In onCreateLoader");
        Intent intent = this.getIntent();
        if (intent == null || !intent.hasExtra(ID_KEY)) {
            return null;
        }
        String runId = intent.getStringExtra(ID_KEY);
        // Define 'where' part of query.
        String selection = RunningEntry._ID + " LIKE ?";
        // Specify arguments in placeholder order.

        String[] selectionArgs = { String.valueOf(runId) };

        String sortOrder = RunningEntry._ID + " ASC";
        return new CursorLoader(
                this,
                RunningEntry.CONTENT_URI,
                RUN_COLUMNS,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                int dateIndex =
                        data.getColumnIndex(RunningEntry.COLUMN_DATETEXT);
                int commentIndex =
                        data.getColumnIndex(RunningEntry.COLUMN_COMMENT);
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
                String comment = data.getString(commentIndex);
                String date = data.getString(dateIndex);

                double temp = data.getDouble(tempIndex);
                double humidity = data.getDouble(humidityIndex);
                double pressure = data.getDouble(pressureIndex);

                final String humidityText = Utilities.humiFormat.format(humidity) + "%";

                setTitle(date);
                ((TextView)findViewById(R.id.value_dist)).
                        setText(Utilities.convertDist(distance,
                                (TextView) findViewById(R.id.value_dist),
                                DetailActivity.this));
                ((TextView)findViewById(R.id.mChronometer)).
                        setText(duration);
                ((TextView)findViewById(R.id.value_temp)).
                        setText(Utilities.convertTemp(temp,
                                (TextView)findViewById(R.id.value_temp),
                                        DetailActivity.this));
                ((TextView)findViewById(R.id.value_humi)).
                        setText(humidityText);
                ((TextView)findViewById(R.id.value_baro)).
                        setText(Utilities.convertBaro(pressure,
                                (TextView)findViewById(R.id.value_baro),
                                        DetailActivity.this));

                mRunStr = String.format("I just ran %s in %s", Utilities.convertDist(distance,(TextView) findViewById(R.id.value_dist),DetailActivity.this), duration);
            }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}