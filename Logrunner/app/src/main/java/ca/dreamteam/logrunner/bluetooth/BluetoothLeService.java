package ca.dreamteam.logrunner.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;


public class BluetoothLeService extends Service {


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
    }

    public void readRemoteRssi() {
        if (mBluetoothLeGatt != null) mBluetoothLeGatt.readRemoteRssi();
    }

    public List<BluetoothGattService> getServices() {
        if (mBluetoothLeGatt != null) return mBluetoothLeGatt.getServices();
        else return new ArrayList<BluetoothGattService>();
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
            super.onConnectionStateChange(gatt, status, newState);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onConnectionStateChange(BluetoothLeService.this, status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onServicesDiscovered(BluetoothLeService.this, status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic c, int s) {
            super.onCharacteristicRead(gatt, c, s);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onCharacteristicRead(BluetoothLeService.this, c, s);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic c, int s) {
            super.onCharacteristicWrite(gatt, c, s);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onCharacteristicWrite(BluetoothLeService.this, c, s);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic c) {
            super.onCharacteristicChanged(gatt, c);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onCharacteristicChanged(BluetoothLeService.this, c);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor d, int s) {
            super.onDescriptorRead(gatt, d, s);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onDescriptorRead(BluetoothLeService.this, d, s);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor d, int s) {
            super.onDescriptorWrite(gatt, d, s);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onDescriptorWrite(BluetoothLeService.this, d, s);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onReliableWriteCompleted(BluetoothLeService.this, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            for(BluetoothLeServiceGattListener listener : mListeners.keySet()){
                listener.onReadRemoteRssi(BluetoothLeService.this, rssi, status);
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
