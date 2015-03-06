package ca.dreamteam.logrunner.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import ca.dreamteam.logrunner.R;

/**
 * This activity should Scan for Bluetooth LE devices, and allow the user to make a connection to
 * one.
 *
 * Once the connection is made, it should start LeService and pass it the connection object
 */
public class BluetoothLeScanActivity extends Activity {

    private final static String TAG = BluetoothLeScanActivity.class.getSimpleName();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;
    private ListView mLeDeviceList;
    private Button mLeScanButton;
    private ProgressBar mLeProgressBar;

    private ServiceConnection mServiceConnection;

    private LeDeviceListAdapter mLeDeviceListAdapter;

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLeDeviceListAdapter.add(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
    private BluetoothLeService mLeService;
    private DeviceActivityListener mBluetoothLeServiceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        mBluetoothAdapter = BluetoothLeUtil.getLeAdapter(this);

        mLeDeviceList = (ListView) findViewById(R.id.deviceListView);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this, R.layout.device_list_text_view);
        mLeDeviceList.setAdapter(mLeDeviceListAdapter);

        mLeProgressBar = (ProgressBar) findViewById(R.id.scanProgressBar);

        mLeScanButton = (Button) findViewById(R.id.startScanButton);
        mLeScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLeDevice();
            }
        });

        Log.i(TAG, "Starting service");
        startService(new Intent(this, BluetoothLeService.class));
        Log.i(TAG, "Binding service");
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i(TAG, "Service connected");
                mLeService = ((BluetoothLeService.BluetoothLeBinder)iBinder).getService();
                mBluetoothLeServiceListener = new DeviceActivityListener();
                mLeService.addListener(mBluetoothLeServiceListener);
            }

            @Override public void onServiceDisconnected(ComponentName componentName) {}
        };
        bindService(new Intent(this, BluetoothLeService.class), mServiceConnection, 0);

        mLeDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "Device clicked");
                BluetoothDevice device = mLeDeviceListAdapter.getItem(i);
                if(BluetoothLeService.deviceMatches(device)){
                    Log.i(TAG, "Device matches, connecting!");
                    mLeService.connectToDevice(device);
                } else {
                    Log.i(TAG, "Device does not match, not connecting!");
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    private void scanLeDevice() {
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
        mLeScanButton.setEnabled(false);

        CountDownTimer timer = new CountDownTimer(SCAN_PERIOD, 200) {
            @Override
            public void onTick(long l) {
                mLeProgressBar.setProgress( 100 - (int) ((100*l) / SCAN_PERIOD) );
            }

            @Override
            public void onFinish() {
                mLeProgressBar.setProgress( 0 );
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mLeScanButton.setEnabled(true);
            }
        };

        mBluetoothAdapter.startLeScan(mLeScanCallback);
        timer.start();
    }

    private class DeviceActivityListener extends BluetoothLeService.BluetoothLeServiceGattListener {
        @Override
        public void onConnectionStateChange(BluetoothLeService service, int status, int newState) {
            super.onConnectionStateChange(service, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                service.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothLeService service, int status) {
            finish();
        }
    }
}
