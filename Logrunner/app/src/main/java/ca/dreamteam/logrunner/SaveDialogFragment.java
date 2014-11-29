package ca.dreamteam.logrunner;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ca.dreamteam.logrunner.data.RunningDbHelper;

public class SaveDialogFragment extends DialogFragment {

    private static final String KEY_SAVE_RATING_BAR_VALUE = "KEY_SAVE_RATING_BAR_VALUE";
    private double rating;
    private String title;
    private String comment;
    private String duration;
    private double temperature;
    private double pressure;
    private double humidity;
    private double distance;
    private byte[] image;

    static SaveDialogFragment newInstance(String duration,
                                          double temperature,
                                          double pressure,
                                          double humidity,
                                          double distance,
                                          byte[] byteArray) {
        SaveDialogFragment f = new SaveDialogFragment();
        Bundle args = new Bundle();
        args.putString("duration", duration);
        args.putDouble("temperature", temperature);
        args.putDouble("pressure", pressure);
        args.putDouble("humidity", humidity);
        args.putDouble("distance", distance);
        args.putByteArray("image", byteArray);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        duration = getArguments().getString("duration");
        temperature = getArguments().getDouble("temperature");
        pressure = getArguments().getDouble("pressure");
        humidity = getArguments().getDouble("humidity");
        distance = getArguments().getDouble("distance");
        image = getArguments().getByteArray("image");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View dialogView = inflater.inflate(R.layout.dialog_saving, container, false);

        Button saveButton = (Button) dialogView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add values to DB
                DateFormat df = new SimpleDateFormat("EEE, dd MMM");
                RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
                rating = ratingBar.getRating();
                EditText titleEditText = (EditText) dialogView.findViewById(R.id.title);
                title = titleEditText.getText().toString();
                EditText commentEditText = (EditText) dialogView.findViewById(R.id.comment);
                comment = commentEditText.getText().toString();

                RunningDbHelper.addRunInfo(
                        title,
                        df.format(Calendar.getInstance().getTime()), // current Date
                        comment,
                        duration,
                        "00:00", //Start time of run
                        temperature,
                        pressure,
                        humidity,
                        distance,
                        rating,
                        image,
                        getActivity().getContentResolver()
                );
                getDialog().dismiss();
            }
        });

        Button cancelButton = (Button) dialogView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        return dialogView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.how_did_it_go));
        return dialog;
    }
}
/*
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        RunningDbHelper.addRunInfo(
                df.format(Calendar.getInstance().getTime()),
                commentInput,
                mAvgTemperature,
                mAvgPressure,
                duration,
                "00:00",
                mAvgHumidity,
                0,
                rating,
                StartRunActivity.this.getContentResolver()
        );
*/