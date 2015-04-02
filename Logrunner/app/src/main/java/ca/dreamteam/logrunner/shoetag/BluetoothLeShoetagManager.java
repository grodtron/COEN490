package ca.dreamteam.logrunner.shoetag;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import ca.dreamteam.logrunner.bluetooth.BluetoothLeService;

public class BluetoothLeShoetagManager extends ShoetagManager {

    private final static String TAG = BluetoothLeShoetagManager.class.getSimpleName();
    private final ServiceConnection mServiceConnection;
    private final Context mContext;

    private AtomicInteger mBytesRead;

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

        mBytesRead = new AtomicInteger();

        new Timer().scheduleAtFixedRate( new TimerTask(){
            @Override
            public void run() {
                int bytes = mBytesRead.getAndSet(0);
                Log.i("BANDWIDTH", "avg Bytes per second: " + bytes/5);
            }
        }, 5000, 5000);

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

        public void onConnectionStateChange(BluetoothLeService service, int status, int newState) {
            if(newState == BluetoothGatt.STATE_DISCONNECTED){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText( mContext, "disconnected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


        boolean first = true;

        public void onCharacteristicChanged(BluetoothLeService service, BluetoothGattCharacteristic characteristic) {

            if(first){
                first = false;
                byte [] value = characteristic.getValue();
                Log.i(TAG, "=======================\n=======================\n=======================\n");
                Log.i(TAG, Arrays.toString(value));
                Log.i(TAG, "=======================\n=======================\n=======================\n");
            }

            ForceReading reading = new ForceReading(System.currentTimeMillis());

            mBytesRead.addAndGet(characteristic.getValue().length);

            byte [] value = characteristic.getValue();


            int rate_of_onset[] = new int[5];
            int maximum[]       = new int[5];
            for(int i = 0; i < 5; ++i){
                maximum[i] = value[i*3 + 0] & 0xff;
                rate_of_onset[i]       = value[i*3 + 1] & 0xff;
                maximum[i] |= (value[i*3 + 2] & 0x0f) << 8;
                rate_of_onset[i]       |= (value[i*3 + 2] & 0xf0) << 4;
            }
            int ground_contact_time =  value[15] & 0xff;
                ground_contact_time |= (value[16] & 0xff) << 8;

            Log.i(TAG, Arrays.toString(value));

            Log.i(TAG, Arrays.toString(maximum));

            reading.setReading(ForceReading.Location.FRONT_LEFT,    maximum[0]);//1.35*maximum[0]);
            reading.setReading(ForceReading.Location.MIDDLE_MIDDLE, maximum[1]);//1.35*maximum[1]);
            reading.setReading(ForceReading.Location.BACK_RIGHT,    maximum[2]);//1.35*maximum[2]);
            reading.setReading(ForceReading.Location.FRONT_RIGHT,   maximum[3]);//1.35*maximum[3]);
            reading.setReading(ForceReading.Location.FRONT_LEFT,    maximum[4]);//1.35*maximum[4]);

            reading.setReading(ForceReading.Location.FRONT_LEFT_roo,    rate_of_onset[0]);//*1.35*100);
            reading.setReading(ForceReading.Location.MIDDLE_MIDDLE_roo, rate_of_onset[1]);//*1.35*100);
            reading.setReading(ForceReading.Location.BACK_RIGHT_roo,    rate_of_onset[2]);//*1.35*100);
            reading.setReading(ForceReading.Location.FRONT_RIGHT_roo,   rate_of_onset[3]);//*1.35*100);
            reading.setReading(ForceReading.Location.FRONT_LEFT_roo,    rate_of_onset[4]);//*1.35*100);

            reading.setGround_contact_time(ground_contact_time);

            updateForce(reading);
        }
    }
}
