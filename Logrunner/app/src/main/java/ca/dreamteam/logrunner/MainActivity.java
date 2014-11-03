package ca.dreamteam.logrunner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

    final String TAG = MainActivity.this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find ImageView-elements
        ImageView startRunButton = (ImageView) findViewById(R.id.StartRun);
        startRunButton.setDrawingCacheEnabled(true);
        ImageView viewHistoryButton = (ImageView) findViewById(R.id.ViewHistory);
        viewHistoryButton.setDrawingCacheEnabled(true);

        startRunButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
                int color = 0;
                try {
                    color = bmp.getPixel((int) event.getX(), (int) event.getY());
                } catch(Exception e) {
                    android.util.Log.e(TAG,"getting the Bitmap" +
                            " Pixel touched for startRunButton threw an exception");
                }
                if(color == Color.TRANSPARENT) return false;
                else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Intent intent = new Intent(getApplicationContext(), StartRunActivity.class);
                            startActivity(intent);
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });

        viewHistoryButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
                int color = 0;
                try {
                    color = bmp.getPixel((int) event.getX(), (int) event.getY());
                } catch(Exception e) {
                    android.util.Log.e(TAG,"getting the Bitmap" +
                            " Pixel touched for viewHistoryButton threw an exception");
                }
                if(color == Color.TRANSPARENT) return false;
                else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Intent intent = new Intent(getApplicationContext(), ViewHistoryActivity.class);
                            startActivity(intent);
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}