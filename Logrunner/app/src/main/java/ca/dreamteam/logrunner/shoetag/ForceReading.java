package ca.dreamteam.logrunner.shoetag;

import java.util.HashMap;

/**
 * Created by gordon on 12/02/2015.
 */
public class ForceReading {

    public enum Location {
        FRONT_LEFT,
        FRONT_RIGHT,
        FRONT_MIDDLE,
        MIDDLE_MIDDLE,
        BACK_RIGHT,
    }

    private HashMap<Location, Integer> readings;
    private final long millis;

    public ForceReading(long millis){
        this.millis = millis;
        readings = new HashMap<Location, Integer>(5);
        for(Location loc : Location.values()){
            readings.put(loc, 0);
        }
    }

    /**
     * Store a force value for a particular location, if no value is currently stored
     * for that location.
     *
     * @param where The location of the force
     * @param value The numerical value of the force
     */
    public void setReading(Location where, int value){
        readings.put(where, value);
    }

    /**
     * Get the numerical force value for a particular location on the foot
     * @param where The location
     * @return the value, or whatever a null Integer gets unboxed to (0 I guess).
     */
    public int getReading(Location where){
        return readings.get(where);
    }

    public long getTime(){
        return millis;
    }

}
