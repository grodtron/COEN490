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
        FRONT_LEFT_roo,
        FRONT_RIGHT_roo,
        FRONT_MIDDLE_roo,
        MIDDLE_MIDDLE_roo,
        BACK_RIGHT_roo,
    }

    private HashMap<Location, Double> readings;
    private final long millis;

    public int getGround_contact_time() {
        return ground_contact_time;
    }

    public void setGround_contact_time(int ground_contact_time) {
        this.ground_contact_time = ground_contact_time;
    }

    private int ground_contact_time;

    public ForceReading(long millis){
        this.millis = millis;
        readings = new HashMap<Location, Double>(10);
        for(Location loc : Location.values()){
            readings.put(loc, 0.0);
        }
    }

    /**
     * Store a force value for a particular location, if no value is currently stored
     * for that location.
     *
     * @param where The location of the force
     * @param value The numerical value of the force
     */
    public void setReading(Location where, double value){
        readings.put(where, value);
    }

    /**
     * Get the numerical force value for a particular location on the foot
     * @param where The location
     * @return the value, or whatever a null Integer gets unboxed to (0 I guess).
     */
    public double getReading(Location where){
        return readings.get(where);
    }

    public long getTime(){
        return millis;
    }

}
