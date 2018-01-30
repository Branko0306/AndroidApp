package com.example.jelav.contentdelivery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements SadrzajAdapter.ListItemClickListener {

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
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;
    private boolean mLocationGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRequestingLocationUpdates = true;

        updateValuesFromBundle(savedInstanceState); //vrati stanje aplikacije nakon rotacije

        dopustenjeZaKoristenjeLokacije(); //upitaj dopustenje za koristenje lokacijskih servisa

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_sadrzaj);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new SadrzajAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    //region Trazenje dozvole za lokacijske servise
    private  void dopustenjeZaKoristenjeLokacije(){
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
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
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
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
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sadrzaj, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            mAdapter.setSadrzajData(null);
            odradiDohvatSadrzaja();
            return true;
        }

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
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessageView() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public void onListItemClick(Sadrzaj sadrzaj) {
        Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
        detailIntent.putExtra(SADRZAJ_NAZIV, sadrzaj.naziv);
        detailIntent.putExtra(SADRZAJ_OPIS, sadrzaj.opis);
        detailIntent.putExtra(SADRZAJ_LATITUDE, sadrzaj.lokacijaLatitude);
        detailIntent.putExtra(SADRZAJ_LONGITUDE, sadrzaj.lokacijaLongitude);
        detailIntent.putExtra(SADRZAJ_PK, sadrzaj.pk);

        startActivity(detailIntent);
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

    public class SadrzajDohvatTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String result = null;
            try {
                result = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            showSadrzajDataView();
            if (result != null && !result.equals("")) {
                mSadrzajResponse = SadrzajWrapper.fromJson(result);
                mAdapter.setSadrzajData(mSadrzajResponse);
            } else {
                showErrorMessageView();
            }

        }
    }
    //endregion
}
