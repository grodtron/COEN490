package ca.dreamteam.logrunner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;


public class Main extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        else if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final ImageView startRunButton = (ImageView) rootView.findViewById(R.id.StartRun);
            startRunButton.setDrawingCacheEnabled(true);
            final ImageView viewHistoryButton = (ImageView) rootView.findViewById(R.id.ViewHistory);
            viewHistoryButton.setDrawingCacheEnabled(true);
            startRunButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, MotionEvent event) {
                    Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
                    int color = 0;
                    try {
                        color = bmp.getPixel((int) event.getX(), (int) event.getY());
                    } catch(Exception e) {
                        // log
                    }
                    if(color == Color.TRANSPARENT) return false;
                    else {
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
 //                               startRunButton.setImageResource(R.drawable.startrun_pressed);
                                break;
                            case MotionEvent.ACTION_OUTSIDE:
                                break;
                            case MotionEvent.ACTION_CANCEL:
                                break;
                            case MotionEvent.ACTION_MOVE:
                                break;
                            case MotionEvent.ACTION_SCROLL:
                                break;
                            case MotionEvent.ACTION_UP:
 //                               startRunButton.setImageResource(R.drawable.startrun);
                                Intent intent = new Intent(getActivity(), RunningActivity.class);
                                startActivity(intent);
                                break;
                            default: break;

                        }
                        return true;
                    }
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
                        // log
                    }
                    if(color == Color.TRANSPARENT) return false;
                    else {
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                break;
                            case MotionEvent.ACTION_OUTSIDE:
                                break;
                            case MotionEvent.ACTION_CANCEL:
                                break;
                            case MotionEvent.ACTION_MOVE:
                                break;
                            case MotionEvent.ACTION_SCROLL:
                                break;
                            case MotionEvent.ACTION_UP:
                                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                                startActivity(intent);
                                break;
                            default: break;

                        }
                        return true;
                    }
                }
            });
            return rootView;
        }


    }
}
