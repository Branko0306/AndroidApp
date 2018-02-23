package com.example.jelav.contentdelivery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import database.ApplicationDatabase;
import database.SadrzajLogEntity;
import database.SadrzajLogEntityDao;
import models.Sadrzaj;
import models.SadrzajResponse;
import network.NetworkUtils;
import network.QueryFilters;
import utils.RecyclerItemTouchHelper;
import utils.SadrzajAdapter;
import utils.SadrzajWrapper;

public class MainActivity extends AppCompatActivity implements
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    //region Static final memberi

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    private static final String LOCATION_GRANTED_KEY = "LOCATION_GRANTED_KEY";
    private static final String LOCATION_LATITUDE = "LOCATION_LATITUDE";
    private static final String LOCATION_LONGITUDE = "LOCATION_LONGITUDE";

    public static final String SADRZAJ_NAZIV = "SADRZAJ_NAZIV";
    public static final String SADRZAJ_OPIS = "SADRZAJ_OPIS";
    public static final String SADRZAJ_LATITUDE = "SADRZAJ_LATITUDE";
    public static final String SADRZAJ_LONGITUDE = "SADRZAJ_LONGITUDE";
    public static final String SADRZAJ_PK = "SADRZAJ_PK";

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //endregion

    private Location mLocation;
    private SadrzajAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SadrzajResponse mSadrzajResponse;
    private Toast mToast;
    private TextView mErrorMessageDisplay;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;
    private boolean mLocationGranted;
    private SwipeRefreshLayout swipeContainer;
    private CoordinatorLayout coordinatorLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        mRequestingLocationUpdates = true;

        updateValuesFromBundle(savedInstanceState); //vrati stanje aplikacije nakon rotacije

        dopustenjeZaKoristenjeLokacije(); //upitaj dopustenje za koristenje lokacijskih servisa

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_sadrzaj);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new SadrzajAdapter( this);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        konfigurirajSwipeRefreshLayout();
    }

    //region SwipeRefreshLayout

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        final Sadrzaj deletedItem = mAdapter.getItem(viewHolder.getAdapterPosition());
        final int deletedIndex = viewHolder.getAdapterPosition();

        if(direction == ItemTouchHelper.LEFT) {

            mAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Ne želite primati obavijesti za kategoriju", Snackbar.LENGTH_LONG);
            snackbar.setAction("PONIŠTI", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
        else if(direction == ItemTouchHelper.RIGHT){
            Sadrzaj sadrzaj = mAdapter.getItem(viewHolder.getAdapterPosition());
            mAdapter.removeItem(viewHolder.getAdapterPosition());

            if(sadrzaj != null) {
                Parametri params = new Parametri();
                params.PK = sadrzaj.pk;
                params.skrivenSadrzaj = true;
                params.obavijestPrikazana = true;
                new SpremiSkrivenTask().execute(params);
            }

            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Uklonili ste sadržaj", Snackbar.LENGTH_LONG);
            snackbar.setAction("PONIŠTI", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapter.restoreItem(deletedItem, deletedIndex);

                    if(deletedItem != null) {
                        Parametri params = new Parametri();
                        params.PK = deletedItem.pk;
                        params.skrivenSadrzaj = false;
                        params.obavijestPrikazana = false;
                        new SpremiSkrivenTask().execute(params);
                    }
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void konfigurirajSwipeRefreshLayout(){

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.setSadrzajData(null);
                odradiDohvatSadrzaja();

                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    //endregion

    //region Trazenje dozvole za lokacijske servise
    private  void dopustenjeZaKoristenjeLokacije(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("Potrebno je dozvoliti pristup lokacijskim servisima",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        mLocationGranted = true;

        konfigurirajLokacijskeServiceClient();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Uredu", okListener)
                .setNegativeButton("Odustani", null)
                .create()
                .show();
    }

    @SuppressLint("MissingPermission")
    private void konfigurirajLokacijskeServiceClient(){

        //ako lokacijski servisi nisu dozvoljeni onda nemoj kreirati klijenta
        if(mLocationGranted == false) {
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (mLocation == null) {
                            return;
                        }

                        mLocation = location;
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    mLocation = location;
                    break;
                }
                odradiDohvatSadrzaja();
            }
        };
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationGranted = true;
                    showToast("Lokacijski servisi su dozvoljeni.");
                    konfigurirajLokacijskeServiceClient();
                } else {
                    mLocationGranted = false;
                    showToast("Lokacijski servisi nisu dozvoljeni.");
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100000);
        mLocationRequest.setFastestInterval(50000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }
    //endregion

    //region Spremanje stanja trenutne aktivnosti i citanje
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        outState.putDouble(LOCATION_LATITUDE, mLocation.getLatitude());
        outState.putDouble(LOCATION_LONGITUDE, mLocation.getLongitude());
        outState.putBoolean(LOCATION_GRANTED_KEY, mLocationGranted);

        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        // Update the value of mRequestingLocationUpdates from the Bundle.

        if (savedInstanceState == null) {
            mRequestingLocationUpdates = true;
            return;
        }

        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }

        if (savedInstanceState.keySet().contains(LOCATION_GRANTED_KEY)) {
            mLocationGranted = savedInstanceState.getBoolean(LOCATION_GRANTED_KEY);
        }

        double latitude = 0;
        double longitude = 0;
        if (savedInstanceState.keySet().contains(LOCATION_LATITUDE)) {
            latitude = savedInstanceState.getDouble(LOCATION_LATITUDE);
        }

        if (savedInstanceState.keySet().contains(LOCATION_LONGITUDE)) {
            longitude = savedInstanceState.getDouble(LOCATION_LONGITUDE);
        }


        if (mLocation == null) {
            mLocation = new Location("LocationDummyProvider");
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
        }
    }
    //endregion

    //region OnPouse & OnResume
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates && mLocationGranted) {
            stopLocationUpdates();
        }
    }

    private void stopLocationUpdates() {
        if(mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates && mLocationGranted) {
            startLocationUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if(mFusedLocationClient != null){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }
    //endregion

    //region Menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sadrzaj, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Prikaz poruke
    private void showToast(String tekst) {

        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        mToast = Toast.makeText(this, tekst, Toast.LENGTH_LONG);
        mToast.show();
    }
    //endregion

    //region Dohvat i prikaz sadrzaja

    private void showSadrzajDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessageView() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void odradiDohvatSadrzaja() {

        if (!mLocationGranted) {
            return;
        }

        if (mLocation == null) {
            return;
        }


        QueryFilters filters = new QueryFilters();
        filters.Longitude = mLocation.getLongitude();
        filters.Latitude = mLocation.getLatitude();

        URL url = NetworkUtils.buildUrl(filters);
        new SadrzajDohvatTask().execute(url);
    }



    public class SadrzajDohvatTask extends AsyncTask<URL, Void, SadrzajResponse> {
        @Override
        protected SadrzajResponse doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String result = null;
            SadrzajResponse response = null;
            try {
                result = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                response = SadrzajWrapper.fromJson(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(SadrzajResponse result) {
            showSadrzajDataView();
            if (result != null && !result.equals("")) {
                mSadrzajResponse = result;
                mAdapter.setSadrzajData(mSadrzajResponse);
            } else {
                showErrorMessageView();
            }

        }
    }

    //endregion

    //region Dohvat i prikaz sadrzaja
    public void onClickOtvoriURL(View v) {
        Button btn = (Button)v;

        Sadrzaj sadrzaj = (Sadrzaj)btn.getTag();
        String url = NetworkUtils.buildUri(Integer.valueOf((sadrzaj.pk))).toString();

        //Toast.makeText(this, mSadrzajData.URL, Toast.LENGTH_LONG).show();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public void onClickOtvoriMap(View v){
        Button btn = (Button)v;

        Sadrzaj sadrzaj = (Sadrzaj)btn.getTag();

        Uri uri2 = Uri.parse(String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=w", sadrzaj.lokacijaLatitude, sadrzaj.lokacijaLongitude));
        showMap(uri2);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    //endregion

    public class SpremiSkrivenTask extends AsyncTask<Parametri, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Parametri... params) {

            Parametri parametri = params[0];

            ApplicationDatabase db = Room.databaseBuilder(getApplicationContext(), ApplicationDatabase.class, "AppDatabase").build();
            SadrzajLogEntityDao dao = db.SadrzajLogEntityDao();

            SadrzajLogEntity entity = new SadrzajLogEntity();
            entity.setSadrzajPK(parametri.PK);
            entity.setSkriven(parametri.skrivenSadrzaj);
            entity.setPrikazanaObavijest(parametri.obavijestPrikazana);
            dao.insertSadrzajLogEntity(entity);

            return true;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }

    public  class Parametri{
        public int PK;
        public boolean skrivenSadrzaj;
        public boolean obavijestPrikazana;
    }
}
