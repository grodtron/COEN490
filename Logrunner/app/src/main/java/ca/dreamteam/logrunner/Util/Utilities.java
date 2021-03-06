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
    public final static float MAP_ZOOM = 15;

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
            baroTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            return mbar_BaroFormat.format(10 * kpa) + "mBar";
        } else if (mBaroUnit.equals("-1")) { // Inch Hg
            baroTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            return inchgh_BaroFormat.format(0.295299830714 * kpa) + " Hg";
        } else { // KiloPascal
            if (kpa >= 99.9) {
                baroTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                return kpa_BaroFormat.format(kpa) + "kpa";
            } else {
                baroTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                return kpa_BaroFormat.format(kpa) + " kpa";
            }
        }
    }

    public static String convertDist(double Km, TextView distTextView, Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mDistUnit = prefs.getString(context.getString(R.string.pref_dist_unit_key),
                context.getString(R.string.pref_default_value));

        if (mDistUnit.equals("0")) {
            if (Km * 0.621371 >= 10) {
                distTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
            } else {
                distTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            }
            return distFormat.format(Km * 0.621371) + " mi";
        } else {
            if (Km >= 10) {
                distTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
            } else {
                distTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            }
            return distFormat.format(Km) + "Km";
        }
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {

        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}