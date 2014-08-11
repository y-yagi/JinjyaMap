package com.example.yaginuma.jinjyamap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yaginuma on 14/08/11.
 */
public class Position {
    private double mLat;
    private double mLng;
    private Context mContext;
    private SharedPreferences mPref;

    private final static String PREF_KEY = "position";
    private final static String PREF_LATITUDE_KEY = "latitude";
    private final static String PREF_LONGITUDE_KEY = "longitude";

    public Position(Context context) {
        this.mContext = context;
        this.mPref = mContext.getSharedPreferences(PREF_KEY, mContext.MODE_PRIVATE);
        // set default to Tokyo
        this.mLat =  this.mPref.getFloat(PREF_LATITUDE_KEY, 35.681382f);
        this.mLng =  this.mPref.getFloat(PREF_LONGITUDE_KEY, 139.766084f);
    }

    public void apply() {
        SharedPreferences.Editor editor = this.mPref.edit();
        editor.putFloat(PREF_LATITUDE_KEY, (float)mLat);
        editor.putFloat(PREF_LONGITUDE_KEY, (float)mLng);
        editor.apply();
    }


    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        this.mLat = lat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double lng) {
        this.mLng = lng;
    }

}
