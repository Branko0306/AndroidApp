package com.example.jelav.contentdelivery;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import models.Sadrzaj;
import models.SadrzajResponse;
import network.NetworkUtils;
import network.QuerySadrzaji;
import utils.SadrzajWrapper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {


    private GoogleMap mMap;
    private String mInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        InstanceID instanceID = InstanceID.getInstance(this);
        mInstance = instanceID.getId();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraIdleListener(this);

        SharedPreferences sharedPref = this.getSharedPreferences(this.getString(R.string.app_name), (Context.MODE_PRIVATE));
        Double latitude = Double.valueOf(sharedPref.getString(this.getString(R.string.latitude), "0"));
        Double longitude = Double.valueOf(sharedPref.getString(this.getString(R.string.longitude), "0"));

        if(latitude != 0 && longitude != 0){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
        }
        odradiDohvatSadrzaja();
    }

    private void odradiDohvatSadrzaja() {

        if(mMap == null)
            return;

        QuerySadrzaji filters = new QuerySadrzaji();
        filters.Longitude = mMap.getCameraPosition().target.longitude;
        filters.Latitude = mMap.getCameraPosition().target.latitude;
        filters.instanceID = mInstance;

        new SadrzajDohvatTask().execute(filters);
    }

    @Override
    public void onCameraMove() {
    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    public void onCameraIdle() {
        odradiDohvatSadrzaja();
    }

    public void onMapRefreshArea(View view){
        odradiDohvatSadrzaja();
    }
    public class SadrzajDohvatTask extends AsyncTask<QuerySadrzaji, Void, SadrzajResponse> {
        @Override
        protected SadrzajResponse doInBackground(QuerySadrzaji... filters) {
            QuerySadrzaji filteri = filters[0];

            filteri.instanceID = mInstance;

            URL url = NetworkUtils.buildUrl(filteri);

            String result = null;
            SadrzajResponse response = null;
            try {
                result = NetworkUtils.getResponseFromHttpUrl(url);
                response = SadrzajWrapper.fromJson(result);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {

            }
            return response;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(SadrzajResponse result) {

            mMap.clear();
            for ( Sadrzaj sadrzaj: result.data) {
                LatLng location = new LatLng(sadrzaj.LokacijaLatitude, sadrzaj.LokacijaLongitude);
                mMap.addMarker(new MarkerOptions().position(location).title(sadrzaj.Naziv));
            }
        }
    }
}
