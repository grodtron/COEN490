package ca.dreamteam.logrunner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utilities {

    static double convertTempValue (double C, Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mTempUnit = prefs.getString(
                context.getString(R.string.pref_temp_unit_key),
                context.getString(R.string.pref_default_value));

        if (mTempUnit.equals("0")) {
            return 1.8 * C + 32;
        } else if (mTempUnit.equals("-1")) {
            return C + 273.15;
        } else {
            return C;
        }
    }

    static String convertTempUnit (Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mTempUnit = prefs.getString(
                context.getString(R.string.pref_temp_unit_key),
                context.getString(R.string.pref_default_value));

        if (mTempUnit.equals("0")) { // F
            return "°F";
        } else if (mTempUnit.equals("-1")) { // K
            return "°K";
        } else { //C
            return "°C";
        }
    }


    static double convertBaroValue (double kpa, Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mBaroUnit = prefs.getString(
                context.getString(R.string.pref_baro_unit_key),
                context.getString(R.string.pref_default_value));

        if (mBaroUnit.equals("0")) { // MillBar
            return  10 * kpa;
        } else if (mBaroUnit.equals("-1")) { // Inch Hg
            return 0.295299830714 * kpa;
        } else { // KiloPascal
            return kpa;
        }
    }

    static String convertBaroUnit (Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mBaroUnit = prefs.getString(
                context.getString(R.string.pref_baro_unit_key),
                context.getString(R.string.pref_default_value));

        if (mBaroUnit.equals("0")) { // MillBar
            return "mBar";
        } else if (mBaroUnit.equals("-1")) { // Inch Hg
            return "Inch.Hg";
        } else { // KiloPascal
            return "kpa";
        }
    }

    static double convertDistValue (double Km, Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mDistUnit = prefs.getString(
                context.getString(R.string.pref_dist_unit_key),
                context.getString(R.string.pref_default_value));

        if (mDistUnit.equals("0")) {
            return Km * 0.621371;
        } else {
            return Km;
        }
    }

    static String convertDistUnit (Context context) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        String mDistUnit = prefs.getString(
                context.getString(R.string.pref_dist_unit_key),
                context.getString(R.string.pref_default_value));

        if (mDistUnit.equals("0")) {
            return "mi";
        } else {
            return "Km";
        }
    }

}
