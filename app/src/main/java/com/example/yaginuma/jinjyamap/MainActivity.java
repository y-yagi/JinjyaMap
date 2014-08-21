package com.example.yaginuma.jinjyamap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.location.LocationListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends FragmentActivity
        implements LocationListener , Response.Listener<JSONObject>, Response.ErrorListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public LocationManager mLocationManager;
    private boolean mDisplayedMarker = false;
    private Marker mCurrentPosMarker = null;
    private String mGooglePlaceAPIKey;
    private Position mCurrentPosition;
    private View mProgressView;
    private View mMapFragment;

    private static final int ZOOM = 15;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String URL_BASE =
        "https://maps.googleapis.com/maps/api/place/textsearch/json?radius=500&sensor=true&language=ja";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGooglePlaceAPIKey = getString(R.string.google_place_api_key);
        mCurrentPosition = new Position(this);
        mProgressView = findViewById(R.id.progress);
        mMapFragment = findViewById(R.id.map) ;

        showProgress(true);
        setLocationProvider();
        setUpMapIfNeeded();
        setCurrentPosMarkerToMap(mCurrentPosition.getLat(), mCurrentPosition.getLng());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        mLocationManager.removeUpdates(this);
        mCurrentPosition.apply();
        super.onStop();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            moveCamera();
        }
    }

    private void addMarkerToMap(String title, Double lat, Double lng) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title));
    }

    private void setCurrentPosMarkerToMap(Double lat, Double lng) {
        if (mCurrentPosMarker != null) {
            mCurrentPosMarker.remove();
        }

        mCurrentPosMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.current)));
    }

    private void setLocationProvider() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Boolean mLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!mLocationEnabled) {
            Toast.makeText(this, "Location機能がOFFになっています。Location機能をONにして下さい。", Toast.LENGTH_LONG).show();
            Intent settingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingIntent);
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, this);  // 10秒/10m間隔
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentPosition.setLat(location.getLatitude());
        mCurrentPosition.setLng(location.getLongitude());
        showProgress(false);

        if (!mDisplayedMarker) {
            fetchPlaces();
            moveCamera();
            mDisplayedMarker = true;
        }
        setCurrentPosMarkerToMap(mCurrentPosition.getLat(), mCurrentPosition.getLng());
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void moveCamera() {
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentPosition.getLat(), mCurrentPosition.getLng()), ZOOM);
        mMap.moveCamera(cu);
    }

    private void fetchPlaces() {
        RequestQueue mQueue;
        mQueue = Volley.newRequestQueue(this);
        String location = "&location=" + mCurrentPosition.getLat() + "," + mCurrentPosition.getLng();
        String url = URL_BASE + location + "&query=" + encode("神社") + "&key=" + mGooglePlaceAPIKey;
        mQueue.add(new JsonObjectRequest(Request.Method.GET, url,
                null, this, this
        ));
    }
    @Override
    public void onResponse(JSONObject response) {
        try {
            JSONArray results = response.getJSONArray("results");
            int result_count = results.length();

            if (result_count == 0) {
                Toast.makeText(this, "近くに検索対象はありませんでした", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, "読み込みが完了しました", Toast.LENGTH_LONG).show();
            for (int i = 0; i < result_count; i++) {
                String name = results.getJSONObject(i).getString("name");
                Double lat = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                Double lng = results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                addMarkerToMap(name, lat, lng);
            }

        } catch (JSONException e ) {
            Log.e(TAG, "Data parse error");
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Data load error");
        error.printStackTrace();
    }

    private String encode(String str) {
        String result = "";
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  result;
    }

    /**
     * Shows the progress UI
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mMapFragment.setVisibility(show ? View.GONE : View.VISIBLE);
        mMapFragment.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMapFragment.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

