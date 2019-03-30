package com.lescour.ben.go4lunch.model.details;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by benja on 26/03/2019.
 */
public class Close implements Parcelable {

    @SerializedName("day")
    @Expose
    private Integer day;
    @SerializedName("time")
    @Expose
    private String time;

    protected Close(Parcel in) {
        if (in.readByte() == 0) {
            day = null;
        } else {
            day = in.readInt();
        }
        time = in.readString();
    }

    public static final Creator<Close> CREATOR = new Creator<Close>() {
        @Override
        public Close createFromParcel(Parcel in) {
            return new Close(in);
        }

        @Override
        public Close[] newArray(int size) {
            return new Close[size];
        }
    };

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (day == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(day);
        }
        dest.writeString(time);
    }
}
