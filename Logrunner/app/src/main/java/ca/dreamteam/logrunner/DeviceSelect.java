package ca.dreamteam.logrunner;

import android.content.Intent;

public class DeviceSelect extends ti.android.ble.sensortag.DeviceSelectActivity {

    /**
     * Returns an Intent that is destined to start the app / splash screen activity. */
    @Override
    protected Intent getDeviceActivityIntent() {
        return new Intent(this, StartRunActivity.class);
    }

}
