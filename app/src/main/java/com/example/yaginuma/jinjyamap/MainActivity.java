package com.example.yaginuma.jinjyamap;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.location.LocationListener;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
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
    private double mLat = 35.681382; // set default to Tokyo
    private double mLng = 139.766084;

    private String mGooglePlaceAPIKey;
    private static final int ZOOM = 15;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String URL_BASE =
        "https://maps.googleapis.com/maps/api/place/textsearch/json?radius=500&sensor=false&language=ja";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGooglePlaceAPIKey = getString(R.string.google_place_api_key);
        setLocationProvider();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(this);
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

    private void setLocationProvider() {
        mLocationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLat = location.getLatitude();
        mLng =  location.getLongitude();
        Toast.makeText(this, "pos : " + mLat + " - " + mLng , Toast.LENGTH_SHORT).show();
        moveCamera();
        fetchPlaces();
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
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLng), ZOOM);
        mMap.moveCamera(cu);
    }

    private void fetchPlaces() {
        RequestQueue mQueue;
        mQueue = Volley.newRequestQueue(this);
        String location = "&location=" + mLat + "," + mLng;
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
        // set only once
        mLocationManager.removeUpdates(this);
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
}
