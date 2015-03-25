package ca.dreamteam.logrunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import org.achartengine.GraphicalView;

import ca.dreamteam.logrunner.bluetooth.BluetoothLeScanActivity;
import ca.dreamteam.logrunner.shoetag.BluetoothLeShoetagManager;
import ca.dreamteam.logrunner.shoetag.ShoetagManager;

public class StartRunActivity extends Activity {

    final String TAG = "ca.dreamteam.logrunner.StartRunActivity";

    private ShoetagManager mStManager;

    private GraphicalView mChartView;
    private byte mByteArray[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_run);
        final Button runButton = (Button) findViewById(R.id.runButton);
        final Button stopButton = (Button) findViewById(R.id.stop_button);
        final Button saveButton = (Button) findViewById(R.id.save_button);
        final Button discardButton = (Button) findViewById(R.id.discard_button);
        final Button pauseButton = (Button) findViewById(R.id.pause_button);
        final Button resumeButton = (Button) findViewById(R.id.resume_button);

        mStManager = new BluetoothLeShoetagManager(this);

        LinearLayout chartContainer = (LinearLayout) findViewById(
                R.id.chartview);

        GraphView gt= new GraphView();
        mStManager.addListener(gt);
        mChartView = gt.getGraphView(getApplicationContext());
        gt.setTextFields(
                (TextView)findViewById(R.id.ground_contact_time),
                (TextView)findViewById(R.id.strides_per_min));

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
                if (mStManager.connected()) {
                    runButton.setVisibility(View.GONE);
                    stopButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                    mStManager.start();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(StartRunActivity.this);

                    builder.setTitle(R.string.ble_not_connected_dialog_title);
                    builder.setMessage(R.string.ble_not_connected_dialog_content);

                    builder.setPositiveButton(R.string.ble_not_connected_dialog_positive,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(
                                            new Intent(
                                                    StartRunActivity.this,
                                                    BluetoothLeScanActivity.class));
                                    dialog.dismiss();
                                }
                            });

                    // Simply dismiss the dialog, we're done
                    builder.setNegativeButton(R.string.ble_not_connected_dialog_negative,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int w) {
                                    d.dismiss();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        StartRunActivity.this);

                alert.setTitle("Stop");
                alert.setMessage("Are you sure want to stop the current run?");
                alert.setPositiveButton("STOP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mStManager.stop();
                        stopButton.setVisibility(View.GONE);
                        pauseButton.setVisibility(View.GONE);
                        resumeButton.setVisibility(View.GONE);
                        saveButton.setVisibility(View.VISIBLE);
                        discardButton.setVisibility(View.VISIBLE);
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


        pauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mStManager.pause();
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mStManager.resume();
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
            }
        });

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

                        saveButton.setVisibility(View.GONE);
                        discardButton.setVisibility(View.GONE);
                        runButton.setVisibility(View.VISIBLE);

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
    protected void onDestroy() {
        super.onDestroy();
        if (mStManager != null) {
            mStManager.onDestroy();
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


//        if(   ((String)textButton.getText()).compareTo("STOP") == 0) {
//            AlertDialog.Builder alert = new AlertDialog.Builder(
//                    StartRunActivity.this);
//            alert.setTitle("Stop");
//            alert.setMessage("Are you sure want to stop the current run without saving and go back?");
//            alert.setPositiveButton("STOP", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    StartRunActivity.this.finish();
//                }
//            });
//            alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            alert.show();
//        }
//        else {
            StartRunActivity.this.finish();
//        }
    }

    public void confirmDialog() {

    }
}
