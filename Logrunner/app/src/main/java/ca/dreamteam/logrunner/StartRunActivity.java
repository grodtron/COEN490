package ca.dreamteam.logrunner;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import ca.concordia.sensortag.SensorTagListener;
import ca.concordia.sensortag.SensorTagLoggerListener;
import ca.concordia.sensortag.SensorTagManager;
import ca.concordia.sensortag.SensorTagManager.ErrorType;
import ca.concordia.sensortag.SensorTagManager.StatusType;
import ti.android.ble.sensortag.DeviceSelectActivity;
import ti.android.ble.sensortag.Sensor;

public class StartRunActivity extends Activity {

    final String TAG = StartRunActivity.this.getClass().getSimpleName();

    static protected final int UPDATE_TEMP_BARO_PERIOD_MS  = 30000;
    static protected final int UPDATE_HUMIDITY_PERIOD_MS = 900000;

    private final static DecimalFormat tempFormat = new DecimalFormat("0.0;-0.0");
    private final static DecimalFormat humiFormat = new DecimalFormat("00");
    private final static DecimalFormat baroFormat = new DecimalFormat("00.0");

    private TextView mTemperatureView;
    private TextView mBarometerView;
    private TextView mHumidityView;

    private double mAvgTemperature = Double.NaN;
    private double mAvgHumidity = Double.NaN;
    private double mAvgPressure = Double.NaN;
    private int mCounter = 1;

    // Bluetooth communication with the SensorTag
    private BluetoothDevice mBtDevice;
    private SensorTagManager mStManager;
    private SensorTagListener mStListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_run);

        Button runButton = (Button) findViewById(R.id.runButton);
        final Chronometer chronometer = (Chronometer) findViewById(R.id.mChronometer);

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

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textButton = (TextView) findViewById(R.id.textButton);
                Button tempButton = (Button) findViewById(R.id.runButton);

                // Based on the textButton value change between Run, Stop & Save actions
                if (((String)textButton.getText()).compareTo("START RUN") == 0) {
                    // Get references to the GUI text box objects
                    mTemperatureView = (TextView) findViewById(R.id.value_temp);
                    mBarometerView = (TextView) findViewById(R.id.value_baro);
                    mHumidityView = (TextView) findViewById(R.id.value_humi);

                    textButton.setText("STOP");
                    tempButton.setBackgroundColor(android.graphics.Color.RED); // Blue

                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    mStManager.addListener(mStListener);
                    mStManager.initServices();
                    if (!mStManager.isServicesReady()) { // initServices failed or took too long
                        android.util.Log.e(TAG, "Discover failed - exiting");
                        finish();
                        return;
                    }

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
                        res = res && mStManager.enableSensor(Sensor.HUMIDITY, UPDATE_HUMIDITY_PERIOD_MS);
                    } else {
                        res = res && mStManager.enableSensor(Sensor.HUMIDITY);
                    }

                    // If any of the enableSensor() calls failed, show/log an error and exit.
                    if (!res) {
                        android.util.Log.e(TAG, "Sensor configuration failed - exiting");
                        Toast.makeText(getApplicationContext(),
                                "Sensor configuration failed - exiting",
                                        Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else if (((String)textButton.getText()).compareTo("STOP") == 0) {
                    textButton.setText("SAVE");
                    tempButton.setBackgroundColor(android.graphics.Color.parseColor("#33B5E5"));
                    chronometer.stop();
                    mStManager.disableUpdates();
                } else if (((String)textButton.getText()).compareTo("SAVE") == 0) {
                    textButton.setText("SAVE COMPLETE!");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mStManager != null) mStManager.enableUpdates();
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

    //TODO: add units string and change it based on settings static values
    public class ManagerListener extends SensorTagLoggerListener implements SensorTagListener {
        @Override
        public void onUpdateAmbientTemperature(SensorTagManager mgr, double temp) {
            super.onUpdateAmbientTemperature(mgr, temp);

            // convertTemperatureUnit(temp);
            final String tempText = tempFormat.format(temp);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTemperatureView.setText(tempText + "°C");
                }
            });
        }

        @Override
        public void onUpdateBarometer(SensorTagManager mgr, double pressure, double height) {
            super.onUpdateBarometer(mgr, pressure, height);

            // convertBarometerUnit(pressure);
            final String baroText = baroFormat.format(pressure);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBarometerView.setText(baroText + "kPa");
                }
            });
        }

        @Override
        public void onUpdateHumidity(SensorTagManager mgr, double rh) {
            super.onUpdateHumidity(mgr, rh);
            final String humiText = humiFormat.format(rh);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHumidityView.setText(humiText + "%");
                }
            });
        }

        // have to deal with errors based on Use cases document
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

}
