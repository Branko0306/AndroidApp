package com.example.jelav.contentdelivery;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import models.Sadrzaj;
import models.SadrzajResponse;
import network.NetworkUtils;
import network.QuerySadrzaji;
import utils.AkcijeUtils;
import utils.SadrzajWrapper;
import utils.ShowDialog;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener, ShowDialog {
    private GoogleMap mMap;
    private String mInstance;

    private FloatingActionButton mRefreshMarkers;

    TextView listItemSadrzajNaziv;
    TextView listItemSadrzajSkraceniOpis;
    TextView listItemSadrzajOpis;
    TextView firmaInfo;

    Button navigateButton;
    Button otvoriButton;

    private ArrayList<Marker> markers;

    public ImageView thumbnail;
    public ImageView firmaLogo;

    CardView itemView;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        InstanceID instanceID = InstanceID.getInstance(this);
        mInstance = instanceID.getId();

        itemView = (CardView)this.findViewById(R.id.include);

        listItemSadrzajNaziv = (TextView) itemView.findViewById(R.id.tv_sadrzaj_naziv);
        listItemSadrzajSkraceniOpis = (TextView) itemView.findViewById(R.id.tv_sadrzaj_skraceni_opis);
        listItemSadrzajOpis = (TextView) itemView.findViewById(R.id.tv_sadrzaj_opis);
        navigateButton = (Button)itemView.findViewById(R.id.actionButtonNavigateID);
        otvoriButton= (Button)itemView.findViewById(R.id.actionButtonOpenID);

        thumbnail = itemView.findViewById(R.id.thumbnail);
        firmaLogo = itemView.findViewById(R.id.firmaLogo);
        firmaInfo = itemView.findViewById(R.id.firmaInfo);

        mRefreshMarkers = (FloatingActionButton)this.findViewById(R.id.refreshMarkers);

        listItemSadrzajOpis.setVisibility(View.GONE);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Molim pričekajte...");
        pDialog.setCancelable(false);
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
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);

        SharedPreferences sharedPref = this.getSharedPreferences(this.getString(R.string.app_name), (Context.MODE_PRIVATE));
        Double latitude = Double.valueOf(sharedPref.getString(this.getString(R.string.latitude), "0"));
        Double longitude = Double.valueOf(sharedPref.getString(this.getString(R.string.longitude), "0"));

        if(latitude != 0 && longitude != 0){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
        }
        odradiDohvatSadrzaja();

        itemView.setVisibility(View.GONE);
        mRefreshMarkers.setVisibility(View.GONE);
    }

    private void odradiDohvatSadrzaja() {

        if(mMap == null)
            return;

        QuerySadrzaji filters = new QuerySadrzaji();
        filters.Zoom = mMap.getCameraPosition().zoom;
        filters.Longitude = mMap.getCameraPosition().target.longitude;
        filters.Latitude = mMap.getCameraPosition().target.latitude;
        filters.instanceID = mInstance;

        new SadrzajDohvatTask().execute(filters);
    }

    public void onClickOtvoriURL(View v) {
        Button btn = (Button)v;

        Sadrzaj sadrzaj = (Sadrzaj)btn.getTag();

        AkcijeUtils utils = new AkcijeUtils(this);
        utils.OtvoriUrl(sadrzaj, mInstance);
    }

    public void onClickOtvoriMap(View v){
        Button btn = (Button)v;

        Sadrzaj sadrzaj = (Sadrzaj)btn.getTag();

        AkcijeUtils utils = new AkcijeUtils(this);
        utils.OtvoriMapu(sadrzaj);
    }

    public void onOpenListActivity(View view){
        finish();
    }

    public void onRefreshMarkers(View view){

        itemView.setVisibility(View.GONE);
        mRefreshMarkers.setVisibility(View.GONE);

        odradiDohvatSadrzaja();
    }

    @Override
    public void onCameraIdle() {
        mRefreshMarkers.setVisibility(View.VISIBLE);
    }
    @Override
    public void onCameraMove() {

    }
    @Override
    public boolean onMarkerClick(final Marker marker) {

        Sadrzaj sadrzaj = (Sadrzaj) marker.getTag();

        listItemSadrzajNaziv.setText(sadrzaj.Naziv);
        listItemSadrzajSkraceniOpis.setText(sadrzaj.SkraceniOpis);

        String info = "";
        if(sadrzaj.SatiOd != sadrzaj.SatiDo)
            info = String.format("%s %s m  od %d:%d do %d:%d", sadrzaj.FirmaNaziv, sadrzaj.Udaljenost, sadrzaj.SatiOd, sadrzaj.MinuteOd, sadrzaj.SatiDo, sadrzaj.MinuteDo);
        else
            info = String.format("%s %s m", sadrzaj.FirmaNaziv, sadrzaj.Udaljenost);

        firmaInfo.setText(info);

        navigateButton.setTag(sadrzaj);
        otvoriButton.setTag(sadrzaj);



        Glide.with(this).load(NetworkUtils.buildUriGetPicture(sadrzaj.PK))
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the original size to disk so that open will be fast
                .skipMemoryCache(true)  // Cache everything
                .fitCenter() // scale to fit entire image within ImageView
                .into(thumbnail);

        Glide.with(this).load(NetworkUtils.buildUriGetLogo(sadrzaj.FirmaPK))
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the original size to disk so that open will be fast
                .skipMemoryCache(true)  // Cache everything
                .fitCenter() // scale to fit entire image within ImageView
                .into(firmaLogo);


        itemView.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void ShowDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    @Override
    public void HideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    public class SadrzajDohvatTask extends AsyncTask<QuerySadrzaji, Void, SadrzajResponse> {
        @Override
        protected SadrzajResponse doInBackground(QuerySadrzaji... filters) {
            QuerySadrzaji filteri = filters[0];

            filteri.instanceID = mInstance;

            URL url = NetworkUtils.buildUrlMap(filteri);

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

            if(result == null || result.data == null)
                return;

            markers = new ArrayList<Marker>();

            for ( Sadrzaj sadrzaj: result.data) {

                LatLng location = new LatLng(sadrzaj.LokacijaLatitude, sadrzaj.LokacijaLongitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(sadrzaj.Naziv));
                marker.setTag(sadrzaj);

                marker.showInfoWindow();
                markers.add(marker);
            }

            mRefreshMarkers.setVisibility(View.GONE);
        }
    }
}
