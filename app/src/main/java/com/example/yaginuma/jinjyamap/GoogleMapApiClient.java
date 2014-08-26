package com.example.yaginuma.jinjyamap;

import android.content.Context;

/**
 * Created by yaginuma on 14/08/20.
 */
public class GoogleMapApiClient {
    private Context mContext;
    private Position mPosition;
    private String mGooglePlaceAPIKey;

    private static final String URL_BASE =
            "https://maps.googleapis.com/maps/api/place/textsearch/json?radius=500&sensor=true&language=ja";

    public GoogleMapApiClient(Context context, Position position) {
        this.mContext = context;
        this.mPosition = position;
        this.mGooglePlaceAPIKey = context.getString(R.string.google_place_api_key);
    }

    public String getRequestUrl() {
        String location = "&location=" + mPosition.getLat() + "," + mPosition.getLng();
        String url = URL_BASE + location + "&query=" + getSearchWord() + "&key=" + mGooglePlaceAPIKey;
        return url;
    }


    private String getSearchWord() {
        String searchWord = "神社"; // TODO: 値を設定画面から取得出来るようにする
        return Util.encode(searchWord);
    }
}
