package ca.dreamteam.logrunner.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import static java.util.UUID.fromString;


public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    // Important connection info!!

    public final static String NAME = "SensorTag";

    public final static UUID
            SERVICE = fromString("f000aa10-0451-4000-b000-000000000000"),
            DATA    = fromString("f000aa11-0451-4000-b000-000000000000"),
            CONF_ENABLE     = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1:
            CONF_PERIOD     = fromString("f000aa13-0451-4000-b000-000000000000"); // Period in tens of milliseconds
    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

    BluetoothGattCharacteristic mEnableCharacteristic;
    BluetoothGattCharacteristic mPeriodCharacteristic;
    BluetoothGattCharacteristic mDataCharacteristic;



    /**
     * Determine if a device is actually one we are interested in!
     */
    public static boolean deviceMatches(BluetoothDevice dev){
        try {
            return dev.getName().equals(NAME);
        }catch(NullPointerException e){
            return false;
        }
    }


    private BluetoothDevice mBluetoothLeDevice;
    private BluetoothGatt   mBluetoothLeGatt;

    public BluetoothLeService(){
        mListeners = new WeakHashMap<BluetoothLeServiceGattListener, Object>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public void discoverServices() {
        if (mBluetoothLeGatt != null) mBluetoothLeGatt.discoverServices();
        Log.i(this.getClass().getSimpleName(), "dicoverServices() " + Thread.currentThread().getName());
    }

    public void readRemoteRssi() {
        if (mBluetoothLeGatt != null) mBluetoothLeGatt.readRemoteRssi();
    }

    public List<BluetoothGattService> getServices() {
        if (mBluetoothLeGatt != null) return mBluetoothLeGatt.getServices();
        else return new ArrayList<BluetoothGattService>();
    }

    public boolean connected() {
        return mConnectionState == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean activateSensor() {
        if (mBluetoothLeGatt == null){
            Log.w(TAG, "mBluetoothLeGatt == null, aborting");
            return false;
        }

        BluetoothGattService serv = mBluetoothLeGatt.getService(SERVICE);

        if(serv == null){
            Log.w(TAG, "serv == null, aborting");
            return false;
        }

        mEnableCharacteristic = serv.getCharacteristic(CONF_ENABLE);
        mPeriodCharacteristic = serv.getCharacteristic(CONF_PERIOD);
        mDataCharacteristic   = serv.getCharacteristic(DATA);

        if(mEnableCharacteristic == null){
            Log.w(TAG, "mEnableCharacteristic == null, aborting");
            return false;
        }

        if(mPeriodCharacteristic == null){
            Log.w(TAG, "mPeriodCharacteristic == null, aborting");
            return false;
        }

        if(mDataCharacteristic == null){
            Log.w(TAG, "mDataCharacteristic == null, aborting");
            return false;
        }

        mPeriodCharacteristic.setValue(new byte[]{10});
        mEnableCharacteristic.setValue(new byte[]{1});

       Log.i(TAG, "setting period to 10");
        mBluetoothLeGatt.writeCharacteristic(mPeriodCharacteristic);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "setting enabled to 1");
        mBluetoothLeGatt.writeCharacteristic(mEnableCharacteristic);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "enabling notifications");
        mBluetoothLeGatt.setCharacteristicNotification(mDataCharacteristic, true);

        BluetoothGattDescriptor clientConfig = mDataCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

        if(clientConfig == null){
            Log.w(TAG, "clientConfig == null, aborting");
            return false;
        }

        Log.i(TAG, "enable notification");
        clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        return mBluetoothLeGatt.writeDescriptor(clientConfig);
    }

    public void deactivateSensor() {
        if(mBluetoothLeGatt == null || mEnableCharacteristic == null || mDataCharacteristic == null) return;

        mBluetoothLeGatt.setCharacteristicNotification(mDataCharacteristic, false);

        mEnableCharacteristic.setValue(new byte[]{0});
        mBluetoothLeGatt.writeCharacteristic(mEnableCharacteristic);

    }

    // We also need to be able to bind with and interact with the service!
    public class BluetoothLeBinder extends Binder {
        public BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent){
        return new BluetoothLeBinder();
    }

    @Override
    public void onDestroy() {
        if(mBluetoothLeGatt != null){
            mBluetoothLeGatt.disconnect();
        }
    }

    /**
     * Public interface to allow application components to register as callbacks (just proxying
     * real callbacks through this Service).
     */
    public static abstract class BluetoothLeServiceGattListener {
        public void onConnectionStateChange(BluetoothLeService service, int status, int newState) {}
        public void onServicesDiscovered(BluetoothLeService service, int status) {}
        public void onCharacteristicRead(BluetoothLeService service, BluetoothGattCharacteristic characteristic, int status) {}
        public void onCharacteristicWrite(BluetoothLeService service, BluetoothGattCharacteristic characteristic, int status) {}
        public void onCharacteristicChanged(BluetoothLeService service, BluetoothGattCharacteristic characteristic) {}
        public void onDescriptorRead(BluetoothLeService service, BluetoothGattDescriptor descriptor, int status) {}
        public void onDescriptorWrite(BluetoothLeService service, BluetoothGattDescriptor descriptor, int status) {}
        public void onReliableWriteCompleted(BluetoothLeService service, int status) {}
        public void onReadRemoteRssi(BluetoothLeService service, int rssi, int status) {}
    }

    private WeakHashMap<BluetoothLeServiceGattListener, Object> mListeners;

    private class BluetoothLeServiceGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            try {
                super.onConnectionStateChange(gatt, status, newState);
                mConnectionState = newState;
                if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    disconnect();
                }
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onConnectionStateChange(BluetoothLeService.this, status, newState);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onConnectionStateChange", e);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            try {
                super.onServicesDiscovered(gatt, status);
                Log.i(this.getClass().getSimpleName(), "onServicesDiscovered() " + Thread.currentThread().getName());
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onServicesDiscovered(BluetoothLeService.this, status);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onServicesDiscovered", e);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic c, int s) {
            try {
                super.onCharacteristicRead(gatt, c, s);
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onCharacteristicRead(BluetoothLeService.this, c, s);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onCharacteristicRead", e);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic c, int s) {
            try {
                super.onCharacteristicWrite(gatt, c, s);
                Log.i(TAG, "onCharacteristicWrite: " + c.getUuid() + " => " + s);
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onCharacteristicWrite(BluetoothLeService.this, c, s);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onCharacteristicWrite", e);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic c) {
            try {
                super.onCharacteristicChanged(gatt, c);
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onCharacteristicChanged(BluetoothLeService.this, c);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onCharacteristicChanged", e);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor d, int s) {
            try {
                super.onDescriptorRead(gatt, d, s);
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onDescriptorRead(BluetoothLeService.this, d, s);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onDescriptorRead", e);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor d, int s) {
            try {
                super.onDescriptorWrite(gatt, d, s);
                Log.i(TAG, "onDescriptorWrite => " + s);
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onDescriptorWrite(BluetoothLeService.this, d, s);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onDescriptorWrite", e);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            try {
                super.onReliableWriteCompleted(gatt, status);
                for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                    listener.onReliableWriteCompleted(BluetoothLeService.this, status);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onReliableWriteCompleted", e);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            try {
                super.onReadRemoteRssi(gatt, rssi, status);
                for (BluetoothLeServiceGattListener listener : mListeners.keySet()) {
                    listener.onReadRemoteRssi(BluetoothLeService.this, rssi, status);
                }
            }catch(Exception e){
                Log.e(TAG, "Unhandled Exception in onReadRemoteRssi", e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // Public API

    public boolean connectToDevice(BluetoothDevice device){
        if(mBluetoothLeGatt == null){
            mBluetoothLeGatt = device.connectGatt(this, true, new BluetoothLeServiceGattCallback());
            return true;
        }else{
            return false;
        }
    }

    public void disconnect(){
        if(mBluetoothLeGatt != null){
            mBluetoothLeGatt.disconnect();
            mBluetoothLeGatt = null;
            mListeners.clear();
        }
    }

    public void addListener(BluetoothLeServiceGattListener listener){
        mListeners.put(listener, null);
    }

    public boolean setCharacteristicNotification(UUID s, UUID c, boolean enable){
        if (mBluetoothLeGatt != null){
            BluetoothGattService service = mBluetoothLeGatt.getService(s);
            if(service != null){
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(c);

                return mBluetoothLeGatt.setCharacteristicNotification(characteristic, enable);
            }
        }

        return false;
    }

}
