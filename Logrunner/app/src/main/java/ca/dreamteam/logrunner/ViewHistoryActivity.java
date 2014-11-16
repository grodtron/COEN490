package ca.dreamteam.logrunner;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Date;

import ca.dreamteam.logrunner.Util.SettingsActivity;
import ca.dreamteam.logrunner.data.RunningContract;
import ca.dreamteam.logrunner.data.RunningContract.RunningEntry;

public class ViewHistoryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HistoryFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String LOG_TAG = HistoryFragment.class.getSimpleName();
        private SimpleCursorAdapter mRunAdapter;
        private static final int RUN_LOADER = 0;
        private static String mRunStr = "Best run Ever #Logrunner ";

        // For the forecast view we're showing only a small subset of the stored data.
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
        // These indices are tied to RUN_COLUMNS. If RUN_COLUMNS changes, these
        // must change.
        public static final int COL_RUN_ID = 0;
        public static final int COL_RUN_DATE = 1;
        public static final int COL_RUN_DESC = 2;
        public static final int COL_RUN_TEMP = 3;
        public static final int COL_RUN_DISTANCE = 4;
        public static final int COL_RUN_PRESSURE = 5;
        public static final int COL_RUN_TIME = 6;
        public static final int COL_RUN_HUMIDITY = 7;
        public HistoryFragment() {
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // The SimpleCursorAdapter will take data from the database through the
            // Loader and use it to populate the ListView it's attached to.
            mRunAdapter = new SimpleCursorAdapter(
                    getActivity(),
                    R.layout.list_item_history,
                    null,
            // the column names to use to fill the textviews
                    new String[]{RunningEntry.COLUMN_DATETEXT,
                            RunningEntry.COLUMN_DISTANCE,
                            RunningEntry.COLUMN_TIME,
                    },
            // the textviews to fill with the data pulled from the columns above
                    new int[]{R.id.list_item_date_textview,
                            R.id.list_item_distance_textview,
                            R.id.list_item_time_textview,
                    },
                    0
            );

        /*
        mRunAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        // TODO: Check parameters, display depending on preferences
            switch (columnIndex) {
            case COL_RUN_TEMP: {
                // we have to do some formatting and possibly a conversion
                ((TextView) view).setText(cursor.getString(columnIndex));
                return true;
            }
            case COL_RUN_DESC: {
            }
            case COL_RUN_DATE: {
                String dateString = cursor.getString(columnIndex);
                TextView dateView = (TextView) view;
                dateView.setText(dateString);
                return true;
            }
        }
        return false;
        }});
        */
            View rootView = inflater.inflate(R.layout.fragment_history, container, false);
            // Get a reference to the ListView, and attach this adapter to it.
            ListView listView = (ListView) rootView.findViewById(R.id.listview_history);
            listView.setAdapter(mRunAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // String forecast = mRunAdapter.getItem(position);
                    // Expand info for selected run
                }
            });
            return rootView;
        }
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(RUN_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }
        @Override
        public void onResume() {
            super.onResume();
            getLoaderManager().restartLoader(RUN_LOADER, null, this);
        }
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            // This is called when a new Loader needs to be created. This
            // fragment only uses one loader, so we don't care about checking the id.
            // To only show current and future dates, get the String representation for today,
            // and filter the query to return weather only for dates after or including today.
            // Only return data after today.
            Log.v(LOG_TAG, "In onCreateLoader");
            String startDate = RunningContract.getDbDateString(new Date());

            // Sort order: Ascending, by date.
            String sortOrder = RunningEntry._ID + " DESC";

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    RunningEntry.CONTENT_URI,
                    RUN_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            mRunAdapter.swapCursor(data);
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mRunAdapter.swapCursor(null);
        }
    }
}