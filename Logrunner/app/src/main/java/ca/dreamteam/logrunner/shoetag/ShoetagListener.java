package ca.dreamteam.logrunner.shoetag;

/**
 * Created by gordon on 12/02/2015.
 */
public interface ShoetagListener {

    /**
     * Receive an updated force reading
     */
    public void updateForce(ForceReading reading);

    /**
     * Receive an updated acceleration reading
     */
    public void updateAcceleration(AccelerationReading reading);

}
