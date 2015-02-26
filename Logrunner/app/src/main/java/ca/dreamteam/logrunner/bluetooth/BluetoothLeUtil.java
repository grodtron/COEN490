package ca.dreamteam.logrunner.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by gordon on 25/02/2015.
 */
public class BluetoothLeUtil {
    private BluetoothLeUtil(){
        throw new RuntimeException("It's a static class, why would you do this?");
    }

    public final static int REQUEST_ENABLE_BT = 0xB1007007;

    /**
     * Enable bluetooth and get the adapter object
     * @return A BluetoothAdapter instance
     */
    public static BluetoothAdapter getLeAdapter(Activity _this){
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothLeManager =
                (BluetoothManager) _this.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothLeAdapter = bluetoothLeManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothLeAdapter == null || !bluetoothLeAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            _this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return null;
        }else {
            return bluetoothLeAdapter;
        }
    }

}
