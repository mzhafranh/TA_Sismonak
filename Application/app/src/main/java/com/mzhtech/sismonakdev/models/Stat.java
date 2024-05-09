package com.mzhtech.sismonakdev.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Stat implements Parcelable {

    public static final Creator<Stat> CREATOR = new Creator<Stat>() {
        @Override
        public Stat createFromParcel(Parcel in) {
            return new Stat(in);
        }

        @Override
        public Stat[] newArray(int size) {
            return new Stat[size];
        }
    };

    private String appName;
    private long duration;

    public Stat() {
    }

    public Stat(String appName, long duration){
        this.appName = appName;
        this.duration = duration;
    }

    protected Stat(Parcel in){
        appName = in.readString();
        duration = in.readLong();
    }

    public String getAppName(){ return appName;}

    public void setAppName(String appName) {this.appName = appName;}

    public long getDuration(){return duration;}

    public void setDuration(long duration) {this.duration = duration;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeLong(duration);
    }
}
