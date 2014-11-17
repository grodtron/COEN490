package ca.dreamteam.logrunner;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

    static SaveDialogFragment newInstance(String duration) {
        SaveDialogFragment f = new SaveDialogFragment();
        Bundle args = new Bundle();
        args.putString("duration", duration);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        duration = getArguments().getString("duration");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_saving, container, false);

        Button saveButton = (Button) dialogView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add values to DB
                DateFormat df = new SimpleDateFormat("EEE, dd MMM");
                RunningDbHelper.addRunInfo(
                        "Morning run",
                        df.format(Calendar.getInstance().getTime()),
                        "WOO",//comment,
                        getArguments().getString("duration"),
                        "00:00",//Start time
                        0,//mAvgTemperature,
                        0,//mAvgPressure,
                        0,//mAvgHumidity,
                        0,//Distance
                        4,//rating,
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