package ca.dreamteam.logrunner.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.view.View;
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
    private TextView mConnectionStatusText;
    private TextView mDeviceNameText;
    private TextView mDeviceAddressText;
    private TextView mRssiText;
    private BluetoothDevice mLeDevice;
    private BluetoothLeService.BluetoothLeServiceGattListener mBluetoothLeServiceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device);

        mConnectionStatusText = (TextView) findViewById(R.id.connectionStatusText);
        mDeviceNameText = (TextView) findViewById(R.id.deviceNameText);
        mDeviceAddressText = (TextView) findViewById(R.id.deviceAddressText);
        mRssiText = (TextView) findViewById(R.id.rssiText);

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

        mLeCharacteristicList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int j, long l) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BluetoothLeDeviceActivity.this, "child clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                BluetoothGattCharacteristic c = (BluetoothGattCharacteristic)
                        mLeServiceListAdapter.getChild(i, j);

                Intent intent = new Intent(
                        BluetoothLeDeviceActivity.this, BluetoothLeCharacteristicActivity.class);

                intent.putExtra(getString(R.string.bluetooth_le_gatt_characteristic_extra), c.getUuid());
                intent.putExtra(getString(R.string.bluetooth_le_gatt_service_extra), c.getService().getUuid());

                startActivity(intent);

                return false;
            }
        });

        mLeCharacteristicList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener(){

            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BluetoothLeDeviceActivity.this, "group clicked", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });

        // TODO ......
        startService(new Intent(this, BluetoothLeService.class));
        bindService(new Intent(this, BluetoothLeService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serviceConnected(((BluetoothLeService.BluetoothLeBinder)iBinder).getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }, 0);
    }

    private class DeviceActivityListener extends BluetoothLeService.BluetoothLeServiceGattListener {
        @Override
        public void onConnectionStateChange(BluetoothLeService service, int status, int newState) {
            super.onConnectionStateChange(service, status, newState);

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
                    service.discoverServices();
                    service.readRemoteRssi();
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
        public void onServicesDiscovered(BluetoothLeService service, int status) {
            super.onServicesDiscovered(service, status);
            // update list of services
            final List<BluetoothGattService> services = service.getServices();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeServiceListAdapter.setServiceList(services);
                }
            });
        }

        @Override
        public void onReadRemoteRssi(final BluetoothLeService service, final int rssi, int status) {
            super.onReadRemoteRssi(service, rssi, status);
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
                        service.readRemoteRssi();
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
    }

    private void serviceConnected(BluetoothLeService service) {
        mBluetoothLeServiceListener = new DeviceActivityListener();
        service.addListener(mBluetoothLeServiceListener);
        service.connectToDevice(mLeDevice);
    }
}
