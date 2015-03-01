package ca.dreamteam.logrunner.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import ca.dreamteam.logrunner.R;

public class BluetoothLeCharacteristicActivity extends Activity {
    private UUID mLeCharacteristic;
    private UUID mLeService;
    private BluetoothGatt mLeGatt;
    private CharacteristicChangeListener mBtLeListener;
    private TextView mHexText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristic);

        mHexText = (TextView) findViewById(R.id.hexText);

        Intent intent = getIntent();

        mLeCharacteristic = (UUID)
                intent.getSerializableExtra(getString(R.string.bluetooth_le_gatt_characteristic_extra));
        mLeService = (UUID)
                intent.getSerializableExtra(getString(R.string.bluetooth_le_gatt_service_extra));


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

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    private class CharacteristicChangeListener extends BluetoothLeService.BluetoothLeServiceGattListener {

        @Override
        public void onCharacteristicChanged(BluetoothLeService service, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(service, characteristic);

            final byte result [] = characteristic.getValue();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHexText.setText(bytesToHex(result));

                }
            });
        }
    }

    private void serviceConnected(BluetoothLeService service) {
        mBtLeListener = new CharacteristicChangeListener();
        service.addListener(mBtLeListener);
        if(service.setCharacteristicNotification(mLeService, mLeCharacteristic, true)){
            Log.w("BLCA","successfully set characteristic notification enabled");
        }else{
            Log.w("BLCA","NOT successfully set characteristic notification enabled");
        }
    }

}
