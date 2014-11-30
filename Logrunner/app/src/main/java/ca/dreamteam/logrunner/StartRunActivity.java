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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.io.ByteArrayOutputStream;

import ca.concordia.sensortag.SensorTagListener;
import ca.concordia.sensortag.SensorTagLoggerListener;
import ca.concordia.sensortag.SensorTagManager;
import ca.concordia.sensortag.SensorTagManager.ErrorType;
import ca.concordia.sensortag.SensorTagManager.StatusType;
import ca.dreamteam.logrunner.Util.Utilities;
import ti.android.ble.sensortag.DeviceSelectActivity;
import ti.android.ble.sensortag.Sensor;

public class StartRunActivity extends Activity {

    final String TAG = "ca.dreamteam.logrunner.StartRunActivity";

    static protected final int UPDATE_TEMP_BARO_PERIOD_MS  = 30000;
    static protected final int UPDATE_HUMIDITY_PERIOD_MS = 900000;

    private TextView mTemperatureView;
    private TextView mBarometerView;
    private TextView mHumidityView;
    private TextView mDistanceView;
    private static double mAvgTemperature, mAvgHumidity, mAvgPressure, mDistance;
    double latitude;
    double longitude;
    private String provider;
    private LocationManager locationManager;

    // Bluetooth communication with the SensorTag
    private BluetoothDevice mBtDevice;
    private SensorTagManager mStManager;
    private SensorTagListener mStListener;
    private GoogleMap map;
    private byte mByteArray[];
    TextView textButton;
    private LocationListener locationListener = new LocationListener() {

        int counter = 0;

        @Override
        public void onLocationChanged(Location location) {
            double previous_latitude = latitude;
            double previous_longitude = longitude;
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            android.util.Log.v(TAG, "lat = " + latitude + " & lng = " + longitude);
            counter++;
            mDistance += Utilities.distance(previous_latitude,
                                            previous_longitude,
                                            latitude,
                                            longitude);
            if (counter > 3) {
                map.addPolyline(
                        new PolylineOptions().
                                add(new LatLng(previous_latitude, previous_longitude),
                                        new LatLng(latitude, longitude)).width(5)
                                .color(android.graphics.Color.RED).geodesic(true));
            }
            LatLng updateToLocation = new LatLng (latitude,longitude);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(updateToLocation, 16));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_run);
        mDistance = 0;
        Button runButton = (Button) findViewById(R.id.runButton);

        // Get the Bluetooth device selected by the user
        mBtDevice = (BluetoothDevice) getIntent().getParcelableExtra(DeviceSelectActivity.EXTRA_DEVICE);
        if (mBtDevice == null) {
            android.util.Log.e(TAG, "No BluetoothDevice extra [" + DeviceSelectActivity.EXTRA_DEVICE
                    + "] provided in Intent.");
            Toast.makeText(this, "No Bluetooth Device selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mStManager = new SensorTagManager(getApplicationContext(), mBtDevice);
        mStListener = new ManagerListener();
        final Chronometer chronometer = (Chronometer) findViewById(R.id.mChronometer);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

                    mAvgTemperature = mAvgHumidity = mAvgPressure = 0.0;
                    // Get references to the GUI text box objects
                    mTemperatureView = (TextView) findViewById(R.id.value_temp);
                    mTemperatureView.setVisibility(View.VISIBLE);
                    mBarometerView = (TextView) findViewById(R.id.value_baro);
                    mBarometerView.setVisibility(View.VISIBLE);
                    mHumidityView = (TextView) findViewById(R.id.value_humi);
                    mHumidityView.setVisibility(View.VISIBLE);
                    mDistanceView = (TextView) findViewById(R.id.value_dist);
                    mDistanceView.setVisibility(View.VISIBLE);
                    mDistanceView.setText(
                            Utilities.convertDist(0.00, mDistanceView, StartRunActivity.this));

                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    mStManager.addListener(mStListener);
                    mStManager.initServices();
                    if (!mStManager.isServicesReady()) { // initServices failed or took too long
                        android.util.Log.e(TAG, "Discover failed - exiting");
                        finish();
                        return;
                    }
                    mStManager.enableUpdates();

                    boolean res = true;
                    if (mStManager.isPeriodSupported(Sensor.IR_TEMPERATURE)) {
                        res = res && mStManager.enableSensor(Sensor.IR_TEMPERATURE,
                                UPDATE_TEMP_BARO_PERIOD_MS);
                    } else {
                        res = res && mStManager.enableSensor(Sensor.IR_TEMPERATURE);
                    }
                    if (mStManager.isPeriodSupported(Sensor.BAROMETER)) {
                        res = res && mStManager.enableSensor(Sensor.BAROMETER,
                                UPDATE_TEMP_BARO_PERIOD_MS);
                    } else {
                        res = res && mStManager.enableSensor(Sensor.BAROMETER);
                    }
                    if (mStManager.isPeriodSupported(Sensor.HUMIDITY)) {
                        res = res && mStManager.enableSensor(Sensor.HUMIDITY,
                                UPDATE_HUMIDITY_PERIOD_MS);
                    } else {
                        res = res && mStManager.enableSensor(Sensor.HUMIDITY);
                    }
                    if (!res) {
                        android.util.Log.e(TAG, "Sensor configuration failed - exiting");
                        Toast.makeText(getApplicationContext(),
                                "Sensor configuration failed - exiting",
                                        Toast.LENGTH_LONG).show();
                        finish();
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                           0,
                                                           0,
                                                           locationListener);
                } else if (((String)textButton.getText()).compareTo("STOP") == 0) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(
                            StartRunActivity.this);

                    alert.setTitle("Stop");
                    alert.setMessage("Are you sure want to stop the current run?");
                    alert.setPositiveButton("STOP", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            chronometer.stop();
                            mStManager.disableUpdates();
                            textButton.setVisibility(View.GONE);
                            tempButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.VISIBLE);
                            discardButton.setVisibility(View.VISIBLE);

                            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                @Override
                                public void onMapLoaded() {
                                    map.snapshot(new GoogleMap.SnapshotReadyCallback() {
                                        public void onSnapshotReady(Bitmap bitmap) {
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            mByteArray = stream.toByteArray();
                                        }
                                    });
                                }
                            });
                            locationManager.removeUpdates(locationListener);
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

        // For testing
        int statusCode = com.google.android.gms.common.GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(StartRunActivity.this);
        switch (statusCode)
        {
            case ConnectionResult.SUCCESS:
                Toast.makeText(StartRunActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_MISSING:
                Toast.makeText(StartRunActivity.this, "SERVICE MISSING", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(StartRunActivity.this, "UPDATE REQUIRED", Toast.LENGTH_SHORT).show();
                break;
            // for error code translation
            default: Toast.makeText(StartRunActivity.this,
                    "Play Service result " + statusCode, Toast.LENGTH_SHORT).show();
        }

        if (map ==  null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapview)).getMap();
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.setMyLocationEnabled(true);
        }

        Button saveButton = (Button) findViewById(R.id.save_button);
        Button discardButton = (Button) findViewById(R.id.discard_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveDialogFragment saveDialog =
                        SaveDialogFragment.newInstance(chronometer.getText().toString(),
                                mAvgTemperature,
                                mAvgPressure,
                                mAvgHumidity,
                                0,
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
                        chronometer.setText("00:00");
                        mTemperatureView.setVisibility(View.GONE);
                        mBarometerView.setVisibility(View.GONE);
                        mHumidityView.setVisibility(View.GONE);
                        mDistanceView.setVisibility(View.GONE);

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
        if (mStManager != null) mStManager.enableUpdates();
        if (map ==  null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapview)).getMap();
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStManager != null) mStManager.enableUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStManager != null) {
            mStManager.disableUpdates();
            mStManager.close();
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
        if(((String)textButton.getText()).compareTo("STOP") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(
                    StartRunActivity.this);

            alert.setTitle("Stop");
            alert.setMessage("Are you sure want to stop the current run and go back?");
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

    public class ManagerListener extends SensorTagLoggerListener implements SensorTagListener {

        @Override
        public void onUpdateAmbientTemperature(SensorTagManager mgr, double temp) {
            super.onUpdateAmbientTemperature(mgr, temp);

            if (mAvgTemperature != 0) {
                mAvgTemperature = (mAvgTemperature + temp) / 2;
            } else {
                mAvgTemperature = temp;
            }

            final double tempUI = temp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTemperatureView.setText(
                            Utilities.convertTemp(tempUI, mTemperatureView, StartRunActivity.this));
                }
            });
        }

        @Override
        public void onUpdateBarometer(SensorTagManager mgr, double pressure, double height) {
            super.onUpdateBarometer(mgr, pressure, height);

            if (mAvgPressure != 0) {
                mAvgPressure = (mAvgPressure + pressure) / 2;
            } else {
                mAvgPressure = pressure;
            }

            final double pressureUI = pressure;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBarometerView.setText(
                            Utilities.convertBaro(pressureUI, mBarometerView, StartRunActivity.this));
                }
            });
        }

        @Override
        public void onUpdateHumidity(SensorTagManager mgr, double rh) {
            super.onUpdateHumidity(mgr, rh);

            if (mAvgHumidity != 0) {
                mAvgHumidity = (mAvgHumidity + rh) / 2;
            } else {
                mAvgHumidity = rh;
            }

            final String humiText = Utilities.humiFormat.format(rh) + "%";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHumidityView.setText(humiText);
                }
            });
        }

        // deal with errors based on Use cases document
        @Override
        public void onError(SensorTagManager mgr, ErrorType type, String msg) {
            super.onError(mgr, type, msg);

            String text = null;
            switch (type) {
                case GATT_REQUEST_FAILED:
                    text = "Error: Request failed: " + msg;
                    break;
                case GATT_UNKNOWN_MESSAGE:
                    text = "Error: Unknown GATT message (Programmer error): " + msg;
                    break;
                case SENSOR_CONFIG_FAILED:
                    text = "Error: Failed to configure sensor: " + msg;
                    break;
                case SERVICE_DISCOVERY_FAILED:
                    text = "Error: Failed to discover sensors: " + msg;
                    break;
                case UNDEFINED:
                    text = "Error: Unknown error: " + msg;
                    break;
                default:
                    break;
            }
            if (text != null)
                Toast.makeText(StartRunActivity.this, text, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatus(SensorTagManager mgr, StatusType type, String msg) {
            super.onStatus(mgr, type, msg);

            String text = null;
            switch (type) {
                case SERVICE_DISCOVERY_STARTED:
                    text = "Preparing SensorTag";
                    break;
                case UNDEFINED:
                    text = "Unknown status";
                    break;
                default:
                    break;
            }
            if (text != null)
                Toast.makeText(StartRunActivity.this, text, Toast.LENGTH_SHORT).show();
        }
    }

    public void confirmDialog() {

    }
}
