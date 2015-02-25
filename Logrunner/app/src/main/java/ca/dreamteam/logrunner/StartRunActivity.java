package ca.dreamteam.logrunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ca.dreamteam.logrunner.Util.Utilities;
import ca.dreamteam.logrunner.shoetag.AccelerationReading;
import ca.dreamteam.logrunner.shoetag.DummyShoetagManager;
import ca.dreamteam.logrunner.shoetag.ForceReading;
import ca.dreamteam.logrunner.shoetag.ShoetagListener;
import ca.dreamteam.logrunner.shoetag.ShoetagManager;

public class StartRunActivity extends Activity {

    final String TAG = "ca.dreamteam.logrunner.StartRunActivity";

    private ShoetagManager mStManager;
    private ShoetagListener mStListener;

    private GraphicalView mChartView;
    private byte mByteArray[];
    TextView textButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_run);
        Button runButton = (Button) findViewById(R.id.runButton);

        mStManager = new DummyShoetagManager();
        mStManager.addListener(mStListener);

        LinearLayout chartContainer = (LinearLayout) findViewById(
                R.id.chartview);

        GraphTesting gt= new GraphTesting();
        mStManager.addListener(gt);
        mChartView = gt.getGraphView(getApplicationContext());

        chartContainer.addView(mChartView);
        mChartView.setVisibility(View.VISIBLE);
        chartContainer.setVisibility(View.VISIBLE);

        TabHost tabs = (TabHost)findViewById(R.id.tabHost);
        tabs.setup();

        TabHost.TabSpec graphTab = tabs.newTabSpec("chart tab");
        graphTab.setContent(R.id.chartTab);
        graphTab.setIndicator("graph view");
        tabs.addTab(graphTab);

        TabHost.TabSpec footTab = tabs.newTabSpec("foot tab");
        footTab.setContent(R.id.footTab);
        footTab.setIndicator("foot view");
        tabs.addTab(footTab);

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textButton = (TextView) findViewById(R.id.textButton);
                final Button tempButton = (Button) findViewById(R.id.runButton);
                final Button saveButton = (Button) findViewById(R.id.save_button);
                final Button discardButton = (Button) findViewById(R.id.discard_button);
                tempButton.setClickable(false);

                // Based on the textButton value change between Run, Stop & Save actions
                if (((String)textButton.getText()).compareTo("START RUN") == 0) {
                    tempButton.setBackgroundColor(android.graphics.Color.RED); // Blue
                    textButton.setText("STOP");

                    mStManager.start();

                } else if (((String)textButton.getText()).compareTo("STOP") == 0) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(
                            StartRunActivity.this);

                    alert.setTitle("Stop");
                    alert.setMessage("Are you sure want to stop the current run?");
                    alert.setPositiveButton("STOP", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mStManager.stop();
                            textButton.setVisibility(View.GONE);
                            tempButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.VISIBLE);
                            discardButton.setVisibility(View.VISIBLE);
                            textButton.setText("START RUN");
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
                tempButton.setClickable(true);
            }
        });

        Button saveButton = (Button) findViewById(R.id.save_button);
        Button discardButton = (Button) findViewById(R.id.discard_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveDialogFragment saveDialog =
                        SaveDialogFragment.newInstance(
                                "foobar yo",
                                "nothing+++",
                                0.0,
                                0.0,
                                0.0,
                                0.0,
                                mByteArray);
                saveDialog.show(getFragmentManager(), "dialog");
            }
        });

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(
                        StartRunActivity.this);

                alert.setTitle("Stop");
                alert.setMessage("Are you sure want to discard the current run?");
                alert.setPositiveButton("DISCARD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final TextView textButton = (TextView) findViewById(R.id.textButton);
                        final Button tempButton = (Button) findViewById(R.id.runButton);
                        final Button save_btn = (Button) findViewById(R.id.save_button);
                        final Button discard_btn = (Button) findViewById(R.id.discard_button);

                        save_btn.setVisibility(View.GONE);
                        discard_btn.setVisibility(View.GONE);
                        textButton.setVisibility(View.VISIBLE);
                        tempButton.setVisibility(View.VISIBLE);
                        textButton.setText("START RUN");
                        tempButton.setBackgroundColor(android.graphics.Color.parseColor("#33B5E5")); // Blue

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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mStManager != null) mStManager.resume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStManager != null) mStManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStManager != null) {
            mStManager.stop();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (mStManager != null) {
            mStManager.stop();
            mStManager = null;
        }

        if (textButton == null) {
            StartRunActivity.this.finish();
            return;
        }

        if(((String)textButton.getText()).compareTo("STOP") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(
                    StartRunActivity.this);
            alert.setTitle("Stop");
            alert.setMessage("Are you sure want to stop the current run without saving and go back?");
            alert.setPositiveButton("STOP", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StartRunActivity.this.finish();
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
        else {
            StartRunActivity.this.finish();
        }
    }

    public void confirmDialog() {

    }
}
