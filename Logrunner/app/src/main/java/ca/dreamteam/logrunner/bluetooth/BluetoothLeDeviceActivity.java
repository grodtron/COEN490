package ca.dreamteam.logrunner.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ca.dreamteam.logrunner.R;

public class BluetoothLeDeviceActivity extends Activity {

    private ExpandableListView mLeCharacteristicList;
    private LeServiceListAdapter mLeServiceListAdapter;
    private BluetoothGatt mBtLeGatt;
    private TextView mConnectionStatusText;
    private TextView mDeviceNameText;
    private TextView mDeviceAddressText;
    private TextView mRssiText;
    private BluetoothDevice mLeDevice;

    @Override
    protected void onStop(){
        super.onStop();
        mBtLeGatt.disconnect();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BluetoothLeDeviceActivity.this,
                        "device disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device);

        mConnectionStatusText = (TextView) findViewById(R.id.connectionStatusText);
        mDeviceNameText       = (TextView) findViewById(R.id.deviceNameText);
        mDeviceAddressText    = (TextView) findViewById(R.id.deviceAddressText);
        mRssiText             = (TextView) findViewById(R.id.rssiText);

        mLeCharacteristicList = (ExpandableListView) findViewById(R.id.characteristicListView);
        mLeServiceListAdapter = new LeServiceListAdapter();
        mLeCharacteristicList.setAdapter(mLeServiceListAdapter);

        Intent intent = getIntent();

        mLeDevice = (BluetoothDevice)
                intent.getParcelableExtra(getString(R.string.bluetooth_le_device_extra));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceNameText.setText(mLeDevice.getName());
                mDeviceAddressText.setText(mLeDevice.getAddress());
            }
        });

        mBtLeGatt = mLeDevice.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mConnectionStatusText.setText("connected");
                                mConnectionStatusText.setTextColor(getResources().getColor(
                                        R.color.bluetooth_connected));
                            }
                        });

                        // Once we connect to the device, then we discover the services that it
                        // makes available
                        gatt.discoverServices();
                        gatt.readRemoteRssi();
                        break;

                    case BluetoothProfile.STATE_DISCONNECTED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mConnectionStatusText.setText("disconnected");
                                mConnectionStatusText.setTextColor(getResources().getColor(
                                        R.color.bluetooth_disconnected));

                                // We are disconnected, so this list of services is no longer valid
                                mLeServiceListAdapter.clear();
                            }
                        });

                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                // update list of services
                final List<BluetoothGattService> services = gatt.getServices();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLeServiceListAdapter.setServiceList(services);
                    }
                });
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
            }

            @Override
            public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if(status == BluetoothGatt.GATT_SUCCESS) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRssiText.setText(Integer.toString(rssi) + " dBm");
                        }
                    });

                    // re-read the RSSI after a brief delay
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            gatt.readRemoteRssi();
                        }
                    }, 100);
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRssiText.setText("");
                        }
                    });
                }
            }
        });

    }
}
