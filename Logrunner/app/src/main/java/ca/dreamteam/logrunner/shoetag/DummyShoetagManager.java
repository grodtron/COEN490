package ca.dreamteam.logrunner.shoetag;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by gordon on 12/02/2015.
 */
public class DummyShoetagManager extends ShoetagManager {

    Thread thisThread;

    AtomicBoolean running;
    AtomicBoolean paused;

    private class DummyShoetagThread implements Runnable {
        @Override public void run() {
            while(running.get()){
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    Log.w("interrupted while sleeping", e);
                }

                // if we're paused loop back and keep waiting
                if(paused.get()){
                    continue;
                }

                DummyShoetagManager.this.updateForce(
                        DummyReadingFactory.createForceReading(System.currentTimeMillis()));
                DummyShoetagManager.this.updateAcceleration(
                        DummyReadingFactory.createAccelerationReading(System.currentTimeMillis()));
            }

        }
    }

    public DummyShoetagManager(){
        running = new AtomicBoolean(false);
        paused  = new AtomicBoolean(false);
    }

    public void start(){
        if(! running.get()) {
            running.set(true);
            thisThread = new Thread(new DummyShoetagThread());
            thisThread.start();
        }
    }

    public void stop(){
        if(running.get()) {
            running.set(false);
            try {
                thisThread.join();
                thisThread = null;
            } catch (InterruptedException e) {
                Log.w("interrupted while joining", e);
            }
        }
    }

    @Override
    public void pause() {
        paused.set(true);
    }

    @Override
    public void resume() {
        paused.set(false);
    }


}
