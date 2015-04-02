package ca.dreamteam.logrunner;

import android.util.Log;

import java.io.OutputStream;
import java.io.PrintWriter;

import ca.dreamteam.logrunner.shoetag.AccelerationReading;
import ca.dreamteam.logrunner.shoetag.ForceReading;
import ca.dreamteam.logrunner.shoetag.ShoetagListener;

/**
 * Created by gordon on 02/04/2015.
 */
public class ShoedataLogger implements ShoetagListener {

    private final PrintWriter out;

    private boolean closed = false;

    public ShoedataLogger(OutputStream outStream){
        this.out = new PrintWriter(outStream);

        out.append("timestamp");
        out.append(',');

        for(ForceReading.Location location : ForceReading.Location.values()){
            out.append(location.name());
            out.append(',');
        }

        out.append("ground_contact_time");
        out.append('\n');
    }

    public void close(){
        out.close();
        closed = true;
    }

    @Override
    public void updateForce(ForceReading reading) {
        if(closed) {
            Log.w(ShoedataLogger.class.getSimpleName(), "force updated when closed");
            return;
        }

        out.append(Long.toString(System.currentTimeMillis()));
        out.append(',');

        for(ForceReading.Location location : ForceReading.Location.values()){
            out.append(Double.toString(reading.getReading(location)));
            out.append(',');
        }

        out.append(Integer.toString(reading.getGround_contact_time()));
        out.append('\n');
    }

    @Override
    public void updateAcceleration(AccelerationReading reading) {

    }
}
