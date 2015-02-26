package ca.dreamteam.logrunner.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.widget.Toast;

/**
 * Created by gordon on 25/02/2015.
 */
public class LeGattCallback extends BluetoothGattCallback {

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        String intentAction;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            // do nothing (TODO)

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // do nothing (TODO)
        }

    }
}
