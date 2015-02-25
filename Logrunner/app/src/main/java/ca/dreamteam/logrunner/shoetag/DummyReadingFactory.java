package ca.dreamteam.logrunner.shoetag;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gordon on 12/02/2015.
 */
public class DummyReadingFactory {


    private static class DummyWaveform {
        // Amplitude factors
        private final double ampA, ampB, ampC;

        // Phase shifts
        private final double phaseA, phaseB, phaseC;

        private final double periodA, periodB, periodC;

        private double getAmplitudeFactor(){
            return 1 + ((Math.random() - 0.5) * 0.1);
        }

        private double getPhaseFactor(){
            return (Math.random() - 0.5) * (Math.PI/8);
        }

        public DummyWaveform(){
            ampA   = getAmplitudeFactor();
            ampB   = getAmplitudeFactor();
            ampC   = getAmplitudeFactor();

            phaseA = getPhaseFactor();
            phaseB = getPhaseFactor();
            phaseC = getPhaseFactor();

            periodA = getAmplitudeFactor();
            periodB = getAmplitudeFactor();
            periodC = getAmplitudeFactor();
        }

        public int get(long millis){
            double t = (((double)(millis%1000))/1000.0) * 2 * Math.PI;

//            return (int)millis % 10000;

            return (int) (1024.0 * (
                            (ampA * (1.0/1.0) * (1 + 0.5*Math.sin((1*t*periodA) + phaseA))) +
                            (ampB * (1.0/4.0) * (1 + 0.5*Math.sin((4*t*periodB) + phaseB))) +
                            (ampC * (1.0/6.0) * (1 + 0.5*Math.sin((6*t*periodC) + phaseC)))));
        }
    }


    static HashMap<ForceReading.Location, DummyWaveform> waves;

    static {
        waves = new HashMap<ForceReading.Location, DummyWaveform>(5);
        waves.put(ForceReading.Location.FRONT_LEFT,    new DummyWaveform());
        waves.put(ForceReading.Location.FRONT_MIDDLE,  new DummyWaveform());
        waves.put(ForceReading.Location.FRONT_RIGHT,   new DummyWaveform());
        waves.put(ForceReading.Location.MIDDLE_MIDDLE, new DummyWaveform());
        waves.put(ForceReading.Location.BACK_RIGHT,    new DummyWaveform());
    }

    private DummyReadingFactory(){
        throw new RuntimeException("Why?");
    }

    public static ForceReading createForceReading(long millis){
        ForceReading f = new ForceReading(millis);

        for(Map.Entry<ForceReading.Location, DummyWaveform> e : waves.entrySet()){
            f.setReading(e.getKey(), e.getValue().get(millis));
        }

        return f;
    }

    public static AccelerationReading createAccelerationReading(long millis){
        return new AccelerationReading();
    }
}
