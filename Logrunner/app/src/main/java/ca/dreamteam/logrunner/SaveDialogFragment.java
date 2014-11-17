package ca.dreamteam.logrunner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

public class SaveDialogFragment extends DialogFragment {

    private static final String KEY_SAVE_RATING_BAR_VALUE = "KEY_SAVE_RATING_BAR_VALUE";
    private RatingBar mRatingBar;
    private EditText titleInput;
    private EditText commentInput;

    static SaveDialogFragment newInstance(String duration) {

        SaveDialogFragment f = new SaveDialogFragment();
        Bundle args = new Bundle();
        args.putString("duration", duration);

        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_saving, null);

        builder.setView(dialogView)
               .setTitle(getString(R.string.how_did_it_go))
               .setPositiveButton(R.string.save2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*mListener.onDialogPositiveClick(SaveDialogFragment.this,
                                4,//mRatingBar.getRating(),
                                "woohoo",//titleInput.getText().toString(),
                                "woohoo",//commentInput.getText().toString(),
                                getArguments().getString("duration"));*/
                        SaveDialogFragment.this.getDialog().dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SaveDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    public interface SaveDialogFragmentListener
    {
        public void onDialogPositiveClick(DialogFragment dialog,
                                          double rating,
                                          String titleInput,
                                          String commentInput,
                                          String duration);
    }

    SaveDialogFragmentListener mListener;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putFloat(KEY_SAVE_RATING_BAR_VALUE, mRatingBar.getRating());
        super.onSaveInstanceState(outState);
    }
}