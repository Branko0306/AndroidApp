package com.example.jelav.contentdelivery;

import android.net.Uri;
import android.util.Log;

import com.example.jelav.contentdelivery.QueryFilters;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by jelav on 25/12/2017.
 */

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String DEBUG_URL = "http://10.0.2.2/webapp/DohvatiSadrzaj/DohvatiSadrzaje";

    private static final String RELEASE_URL = "http://10.0.2.2/webapp/DohvatiSadrzaj/DohvatiSadrzaje";

    private static final String BASE_URL = DEBUG_URL;

    //primjer upita
    //http://192.168.5.107:58051/DohvatiSadrzaj/DohvatiSadrzaje?country=Croatia&town=Dubrovnik&Latitude=42.64696525910761&Longitude=18.08006286621094&Tagovi=tag1;tag2;tag1222&Kategorija=231231
    final static String LAT_PARAM = "Latitude";
    final static String LON_PARAM = "Longitude";
    final static String KAT_PARAM = "Kategorije";
    final static String TAG_PARAM = "Tagovi";

    public static URL buildUrl(QueryFilters filteri) {
        // COMPLETED (1) Fix this method to return the URL used to query Open Weather Map's API
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM, (Double.toString((filteri.Latitude))))
                .appendQueryParameter(LON_PARAM, (Double.toString((filteri.Longitude))))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);
        return url;
    }


    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
