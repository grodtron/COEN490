package ca.dreamteam.logrunner;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class StartRunActivity extends Activity {

    final String TAG = StartRunActivity.this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_run);

        Button runButton = (Button) findViewById(R.id.runButton);
        final Chronometer chronometer = (Chronometer) findViewById(R.id.mChronometer);

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView textButton = (TextView) findViewById(R.id.textButton);
                Button tempButton = (Button) findViewById(R.id.runButton);
                // Based on the textButton value change between Run, Stop & Save actions
                if (((String)textButton.getText()).compareTo("START RUN") == 0) {
                    textButton.setText("STOP");
                    tempButton.setBackgroundColor(android.graphics.Color.RED); // Blue
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                } else if (((String)textButton.getText()).compareTo("STOP") == 0) {
                    //chronometer.stop();
                    textButton.setText("SAVE");
                    tempButton.setBackgroundColor(android.graphics.Color.parseColor("#33B5E5"));
                    chronometer.stop();
                } else if (((String)textButton.getText()).compareTo("SAVE") == 0) {
                    textButton.setText("SAVE COMPLETE!");
                    // intent to do the NEXT thing
                }

                // Each point should run on a thread but not the UI thread (it's own class?)
                // * Read humidity twice at start and end
                // * Read temperature, pressure with same freq on sensor
                // * Update distance
                // * Update Chronometer
            }
        });
    }
}
