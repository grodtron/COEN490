package ca.dreamteam.logrunner.Util;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableRunObject implements Parcelable {

    // put the different values
    private String strValue;
    private Integer intValue;

    private String title;
    private String comment;
    private double avgTemp;
    private double avgPressure;
    private double avgHumidity;
    private double distance;
    private String duration;
    //GoogleMap & Graph

    public ParcelableRunObject() {

    };
    public ParcelableRunObject(Parcel in) {
        readFromParcel(in);
    }

    // need getters & setters?

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(comment);
        dest.writeDouble(avgTemp);
        dest.writeDouble(avgPressure);
        dest.writeDouble(avgHumidity);
        dest.writeDouble(distance);
        dest.writeString(duration);
    }

    private void readFromParcel(Parcel in) {
        title = in.readString();
        comment = in.readString();
        avgTemp = in.readDouble();
        avgPressure = in.readDouble();
        avgHumidity = in.readDouble();
        distance = in.readDouble();
        duration = in.readString();
    }

     public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ParcelableRunObject createFromParcel(Parcel in) {
            return new ParcelableRunObject(in);
        }
        public ParcelableRunObject[] newArray(int size) {
            return new ParcelableRunObject[size];
        }
     };
}
