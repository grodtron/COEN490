package ca.dreamteam.logrunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.widget.TextView;

import ca.dreamteam.logrunner.Util.SettingsActivity;
import ca.dreamteam.logrunner.Util.Utilities;
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
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String LOG_TAG = HistoryFragment.class.getSimpleName();
        private SimpleCursorAdapter mRunAdapter;
        private static final int RUN_LOADER = 0;

        // Specify the columns we need.
        private static final String[] RUN_COLUMNS = {
                RunningEntry.TABLE_NAME + "." + RunningEntry._ID,
                RunningEntry.COLUMN_DATETEXT,
                RunningEntry.COLUMN_COMMENT,
                RunningEntry.COLUMN_TEMP,
                RunningEntry.COLUMN_DISTANCE,
                RunningEntry.COLUMN_PRESSURE,
                RunningEntry.COLUMN_DURATION,
                RunningEntry.COLUMN_HUMIDITY,
                RunningEntry.COLUMN_RATING,
        };

        public static final int COL_RUN_DISTANCE = 4;

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
                            RunningEntry.COLUMN_DURATION,
                    },
                    // the textviews to fill with the data pulled from the columns above
                    new int[]{R.id.list_item_date_textview,
                            R.id.list_item_distance_textview,
                            R.id.list_item_time_textview,
                    },
                    0
            );

            mRunAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (columnIndex) {
                case COL_RUN_DISTANCE: {
                    ((TextView) view).setText(Utilities.convertDist(cursor.getDouble(columnIndex),
                            (TextView) view, getActivity()));
                    return true;
                }
            }
            return false;
            }});

            View rootView = inflater.inflate(R.layout.fragment_history, container, false);
            // Get a reference to the ListView, and attach this adapter to it.
            ListView listView = (ListView) rootView.findViewById(R.id.listview_history);
            listView.setAdapter(mRunAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Cursor cursor = mRunAdapter.getCursor();
                    int index =
                            cursor.getColumnIndex(RunningEntry._ID);
                    if (cursor != null && cursor.moveToPosition(position)) {
                        Intent intent = new Intent(getActivity(), DetailActivity.class)
                                .putExtra(DetailActivity.ID_KEY, cursor.getString(index));
                        startActivity(intent);
                    }
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                    removeItemFromList(pos);
                    return true;
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

            Log.v(LOG_TAG, "In onCreateLoader");

            // Sort order: Ascending, by date.
            String sortOrder = RunningEntry._ID + " DESC";

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

        public void removeItemFromList(int position) {

            AlertDialog.Builder alert = new AlertDialog.Builder(
                    getActivity() );

            final String selection = RunningEntry._ID + " LIKE ?";
            long rowId = mRunAdapter.getItemId(position);
            final String[] selectionArgs = { String.valueOf(rowId) };

            alert.setTitle("Delete");
            alert.setMessage("Are you sure want to delete the selected run?");
            alert.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int mValuesDeleted = getActivity().getContentResolver().delete(
                            RunningEntry.CONTENT_URI,
                            selection,
                            selectionArgs
                    );
                }
            });
            alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.show();

        }
    }
}