package ca.dreamteam.logrunner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RatingBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ca.dreamteam.logrunner.data.RunningDbHelper;

public class SaveDialogFragment extends DialogFragment {

    static SaveDialogFragment newInstance(
            double temperature, double pressure,
                    double humidity, double distance, String duration) {

        SaveDialogFragment f = new SaveDialogFragment();

        // Supply input as an argument.
        Bundle args = new Bundle();
        args.putString("duration", duration);
        args.putDouble("temperature", temperature);
        args.putDouble("pressure", pressure);
        args.putDouble("humidity", humidity);
        args.putDouble("distance", distance);

        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_saving, null))
                .setPositiveButton(R.string.save2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        RatingBar mBar = (RatingBar) getView().findViewById(R.id.ratingBar);
                        float rating = mBar.getRating();
                        EditText titleInput = (EditText) getView().findViewById(R.id.title);
                        String title = titleInput.getText().toString();
                        EditText commentInput = (EditText) getView().findViewById(R.id.comment);
                        String comment = commentInput.getText().toString();
                        RunningDbHelper.addRunInfo(
                                df.format(Calendar.getInstance().getTime()),
                                comment,
                                getArguments().getDouble("temperature"),
                                getArguments().getDouble("pressure"),
                                getArguments().getString("duration"),
                                "00:00",
                                getArguments().getDouble("humidity"),
                                getArguments().getDouble("distance"),
                                rating,
                                getActivity().getContentResolver()
                        );
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SaveDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
