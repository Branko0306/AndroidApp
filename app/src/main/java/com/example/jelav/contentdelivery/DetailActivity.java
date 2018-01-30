package com.example.jelav.contentdelivery;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private Uri mUri; //uri passed from main activity
    private TextView mNaziv;
    private TextView mOpis;

    private SadrzajData mSadrzajData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mNaziv = (TextView) findViewById(R.id.detail_naziv);
        mOpis = (TextView) findViewById(R.id.detail_opis);

        Intent intent = getIntent();

        mSadrzajData = new SadrzajData();
        mSadrzajData.Naziv = intent.getStringExtra("SADRZAJ_NAZIV");

        mSadrzajData.Latitude = intent.getDoubleExtra("SADRZAJ_LATITUDE", 0);
        mSadrzajData.Longitude = intent.getDoubleExtra("SADRZAJ_LONGITUDE", 0);
        mSadrzajData.Opis = intent.getStringExtra("SADRZAJ_OPIS");
        mSadrzajData.PK = intent.getIntExtra("SADRZAJ_PK", 0);
        mSadrzajData.URL = NetworkUtils.buildUri(Integer.valueOf((mSadrzajData.PK))).toString();

        mNaziv.setText(mSadrzajData.Naziv);
        mOpis.setText(mSadrzajData.Opis);
    }

    public void onClickOtvoriURL(View v) {
        //Toast.makeText(this, mSadrzajData.URL, Toast.LENGTH_LONG).show();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSadrzajData.URL));
        startActivity(browserIntent);
    }

    public void onClickOtvoriMap(View v){
        Uri uri2 = Uri.parse(String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=w", mSadrzajData.Latitude, mSadrzajData.Longitude));
        showMap(uri2);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private class SadrzajData{
        public String Naziv;
        public String Opis;
        public Double Latitude;
        public Double Longitude;
        public Integer PK;
        public String URL;
    }
}
