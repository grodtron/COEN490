package ca.dreamteam.logrunner.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.TextView;

import java.text.DecimalFormat;

import ca.dreamteam.logrunner.R;

public class Utilities {

    private final static DecimalFormat CF_TempFormat = new DecimalFormat("0.0;-0.0");
    private final static DecimalFormat K_TempFormat = new DecimalFormat("0");
    private final static DecimalFormat kpa_BaroFormat = new DecimalFormat("0.0");
    private final static DecimalFormat mbar_BaroFormat = new DecimalFormat("0");
    private final static DecimalFormat inchgh_BaroFormat = new DecimalFormat("0.0");
    private final static DecimalFormat distFormat = new DecimalFormat("0.00");
    public final static DecimalFormat humiFormat = new DecimalFormat("0");

    public static String convertTemp(double C, TextView tempTextView, Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mTempUnit = prefs.getString(context.getString(R.string.pref_temp_unit_key),
                context.getString(R.string.pref_default_value));

        if (mTempUnit.equals("0")) {
            return CF_TempFormat.format(1.8 * C + 32) + "°F";
        } else if (mTempUnit.equals("-1")) {
            return K_TempFormat.format(C + 273.15) + "°K";
        } else {
            return CF_TempFormat.format(C) + "°C";
        }
    }

    public static String convertBaro(double kpa, TextView baroTextView, Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mBaroUnit = prefs.getString(context.getString(R.string.pref_baro_unit_key),
                context.getString(R.string.pref_default_value));

        if (mBaroUnit.equals("0")) { // MillBar
            return mbar_BaroFormat.format(10 * kpa) + "mBar";
        } else if (mBaroUnit.equals("-1")) { // Inch Hg
            return inchgh_BaroFormat.format(0.295299830714 * kpa) + "Inch.Hg";
        } else { // KiloPascal
            return kpa_BaroFormat.format(kpa) + "kpa";
        }
    }

    public static String convertDist(double Km, TextView distTextView, Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mDistUnit = prefs.getString(context.getString(R.string.pref_dist_unit_key),
                context.getString(R.string.pref_default_value));

        if (mDistUnit.equals("0")) {
            return distFormat.format(Km * 0.621371) + "mi";
        } else {
            return distFormat.format(Km) + "Km";
        }
    }

    public static void setHumiTextViewSize(double rh, TextView humiTextView) {
        if (rh >= 100) {
            humiTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        } else if (rh >= 10) {
            humiTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        } else {
            humiTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }
    }
}