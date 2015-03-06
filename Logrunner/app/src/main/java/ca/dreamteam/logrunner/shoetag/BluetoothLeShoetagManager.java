package ca.dreamteam.logrunner.shoetag;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import ca.dreamteam.logrunner.bluetooth.BluetoothLeService;

public class BluetoothLeShoetagManager extends ShoetagManager {

    private final static String TAG = BluetoothLeShoetagManager.class.getSimpleName();
    private final ServiceConnection mServiceConnection;
    private final Context mContext;

    private BluetoothLeService mLeService;
    private DeviceActivityListener mBluetoothLeServiceListener;

    public BluetoothLeShoetagManager(Context context) {
        mContext = context;

        Log.i(TAG, "Starting service");
        context.startService(new Intent(context, BluetoothLeService.class));
        Log.i(TAG, "Binding service");

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i(TAG, "Service connected");
                mLeService = ((BluetoothLeService.BluetoothLeBinder) iBinder).getService();
                mBluetoothLeServiceListener = new DeviceActivityListener();
                mLeService.addListener(mBluetoothLeServiceListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };

        context.bindService(new Intent(context, BluetoothLeService.class), mServiceConnection, 0);
    }


    @Override
    public void start() {
        Log.i(TAG, "Start!!");
        if (mLeService != null) mLeService.activateSensor();
    }

    @Override
    public void stop() {
        Log.i(TAG, "Stop!!");
        if (mLeService != null) mLeService.deactivateSensor();
    }

    @Override
    public void pause() {
        Log.i(TAG, "Pause!!");
        if (mLeService != null) mLeService.deactivateSensor();
    }

    @Override
    public void resume() {
        Log.i(TAG, "Resume!!");
        if (mLeService != null) mLeService.activateSensor();
    }

    @Override
    public boolean connected() {
        return mLeService != null && mLeService.connected();
    }

    @Override
    public void onDestroy() {
        mContext.unbindService(mServiceConnection);
    }

    private class DeviceActivityListener extends BluetoothLeService.BluetoothLeServiceGattListener {
        public void onCharacteristicChanged(BluetoothLeService service, BluetoothGattCharacteristic characteristic) {
            ForceReading reading = new ForceReading(System.currentTimeMillis());

            byte [] value = characteristic.getValue();

            reading.setReading(ForceReading.Location.FRONT_LEFT, value[0]);
            reading.setReading(ForceReading.Location.MIDDLE_MIDDLE, value[1]);
            reading.setReading(ForceReading.Location.BACK_RIGHT, value[2]);

            updateForce(reading);
        }
    }
}
