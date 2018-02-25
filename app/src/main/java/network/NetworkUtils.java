package network;

import android.net.Uri;
import android.util.Log;

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

    private static final String DEBUG_HOST = "http://10.0.2.2/webapp/";
    private static final String RELEASE_HOST = "https://webappcd.azurewebsites.net/";

    private static final String AKCIJA_DOHVATI_SADRZAJ = "DohvatiSadrzaj";
    private static final String AKCIJA_DOHVATI_SADRZAJ_PICTURE = "Sadrzaj/GetPicture";
    private static final String AKCIJA_DOHVATI_SADRZAJE = "DohvatiSadrzaj/DohvatiSadrzaje";

    private static final boolean IS_DEBUG = true;

    //primjer upita
    //http://192.168.5.107:58051/DohvatiSadrzaj/DohvatiSadrzaje?country=Croatia&town=Dubrovnik&Latitude=42.64696525910761&Longitude=18.08006286621094&Tagovi=tag1;tag2;tag1222&Kategorija=231231
    final static String LAT_PARAM = "Latitude";
    final static String LON_PARAM = "Longitude";
    final static String KAT_PARAM = "Kategorije";
    final static String TAG_PARAM = "Tagovi";
    final static String ID_PARAM = "sadrzajID";
    final static String ID_SADRZAJ_PICTURE = "id";
    final static String SKRIVENI_SADRZAJI = "skriveni";

    public static URL buildUrl(QueryFilters filteri) {
        // COMPLETED (1) Fix this method to return the URL used to query Open Weather Map's API
        Uri builtUri = null;

        if(filteri.sadrzajID <= 0){
            builtUri = Uri.parse(getBaseURL(AKCIJA_DOHVATI_SADRZAJE)).buildUpon()
                    .appendQueryParameter(LAT_PARAM, (Double.toString((filteri.Latitude))))
                    .appendQueryParameter(LON_PARAM, (Double.toString((filteri.Longitude))))
                    .appendQueryParameter(SKRIVENI_SADRZAJI, filteri.getSkriveni())
                    .build();
        }else {
            builtUri = Uri.parse(getBaseURL(AKCIJA_DOHVATI_SADRZAJ)).buildUpon()
                    .appendQueryParameter(ID_PARAM, (Integer.toString((filteri.sadrzajID))))
                    .appendQueryParameter(SKRIVENI_SADRZAJI, filteri.getSkriveni())
                    .build();
        }

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);
        return url;
    }

    public static Uri buildUri(int sadrzajID) {
        // COMPLETED (1) Fix this method to return the URL used to query Open Weather Map's API
        Uri builtUri = Uri.parse(getBaseURL(AKCIJA_DOHVATI_SADRZAJ)).buildUpon()
                    .appendQueryParameter(ID_PARAM, (Integer.toString((sadrzajID))))
                    .build();

        return builtUri;
    }

    public static Uri buildUriGetPicture(int sadrzajID) {
        // COMPLETED (1) Fix this method to return the URL used to query Open Weather Map's API
        Uri builtUri = Uri.parse(getBaseURL(AKCIJA_DOHVATI_SADRZAJ_PICTURE)).buildUpon()
                .appendQueryParameter(ID_SADRZAJ_PICTURE, (Integer.toString((sadrzajID))))
                .build();

        return builtUri;
    }

    public static URL buildUrl(Uri uri){
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);
        return url;
    }

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

    public static String getBaseURL(String akcija) {

        if(IS_DEBUG){
           return DEBUG_HOST + akcija;
        }else{
            return  RELEASE_HOST + akcija;
        }
    }
}
