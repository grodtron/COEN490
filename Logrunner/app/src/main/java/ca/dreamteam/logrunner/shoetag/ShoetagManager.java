package ca.dreamteam.logrunner.shoetag;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gordon on 12/02/2015.
 */
public abstract class ShoetagManager {

    private Set<ShoetagListener> listeners;

    public ShoetagManager(){
        listeners = new HashSet<ShoetagListener>();
    }

    public void addListener(ShoetagListener listener){
        if(listener != null) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    protected void updateForce(ForceReading reading){
        synchronized (listeners) {
            for (ShoetagListener listener : listeners) {
                listener.updateForce(reading);
            }
        }
    }

    protected void updateAcceleration(AccelerationReading reading){
        synchronized (listeners) {
            for (ShoetagListener listener : listeners) {
                listener.updateAcceleration(reading);
            }
        }
    }

    public abstract void start();

    public abstract void stop();

    public abstract void pause();

    public abstract void resume();

}
