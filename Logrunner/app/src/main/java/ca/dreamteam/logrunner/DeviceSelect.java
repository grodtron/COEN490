package ca.dreamteam.logrunner;

import android.content.Intent;

public class DeviceSelect extends ti.android.ble.sensortag.DeviceSelectActivity {

    /**
     * Returns an Intent that is destined for the application's main activity. No other extras or
     * data need be specified for this Intent related to the SensorTag/Bluetooth device (you may,
     * however, specify other data required by the Activity that is not SensorTag-related).
     *
     * See the Minimal example for more detailed explanations.
     */
    @Override
    protected Intent getDeviceActivityIntent() {
        return new Intent(this, SplashScreen.class);
    }

}
