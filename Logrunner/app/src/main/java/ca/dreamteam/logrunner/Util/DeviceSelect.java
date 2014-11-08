package ca.dreamteam.logrunner.Util;

import android.content.Intent;

import ca.dreamteam.logrunner.StartRunActivity;

public class DeviceSelect extends ti.android.ble.sensortag.DeviceSelectActivity {

    /**
     * Returns an Intent that is destined to start the StartRunActivity. */
    @Override
    protected Intent getDeviceActivityIntent() {
        return new Intent(this, StartRunActivity.class);
    }

}
