package utils;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.jelav.contentdelivery.MainActivity;
import com.example.jelav.contentdelivery.R;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import models.IntentServiceExtras;
import models.Sadrzaj;
import models.SadrzajResponse;
import network.NetworkUtils;
import network.QueryFilters;
import network.QueryObavijest;


public class NotificationUtils {

    private static final int SADRZAJ_PODSJETNIK_ID = 1234;
    private static final int SADRZAJ_PODSJETNIK_PENDING_INTENT_ID = 4321;
    private static final int ACTION_OTVORI_PENDING_INTENT_ID = 1;
    private static final int ACTION_ODUSTANI_PENDING_INTENT_ID = 2;
    private static final int ACTION_NAVIGATE_PENDING_INTENT_ID = 3;

    private static final String NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    public static void ocistiSveNotifikacije(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void otvoriUBrowseru(Context context, IntentServiceExtras extras)
    {
        ocistiSveNotifikacije(context);
        postaviSadrzajPrikazan(context, extras.sadrzaj_pk);

        String url = NetworkUtils.buildUri(extras.sadrzaj_pk).toString();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static void otvoriNavigaciju(Context context, IntentServiceExtras extras)
    {
        ocistiSveNotifikacije(context);
        postaviSadrzajPrikazan(context, extras.sadrzaj_pk);

        Uri uri = Uri.parse(String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=w", extras.sadrzaj_latitude, extras.sadrzaj_longitude));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void postaviSadrzajPrikazan(Context context, int sadrzaj_pk){

        try
        {

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {

        }

    }

    public static void dohvatiSadrzajPrikaziNotifikaciju(Context context) {

        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(mChannel);
            }

            InstanceID instanceID = InstanceID.getInstance(context);

            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_name), (Context.MODE_PRIVATE));
            Double latitude = Double.valueOf(sharedPref.getString(context.getString(R.string.latitude), "0"));
            Double longitude = Double.valueOf(sharedPref.getString(context.getString(R.string.longitude), "0"));


            QueryObavijest filters = new QueryObavijest();
            filters.Latitude = latitude;
            filters.Longitude = longitude;
            filters.instanceID = instanceID.getId();

            URL url = NetworkUtils.buildUrl(filters);
            String result = null;
            Sadrzaj sadrzaj = null;
            try {
                result = NetworkUtils.getResponseFromHttpUrl(url);
                sadrzaj = SadrzajWrapper.fromJsonSingle(result);

                if(sadrzaj != null) {

                    String text = "";
                    BufferedReader reader = null;
                    try {
                        String data = URLEncoder.encode("sadrzajID", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(sadrzaj.PK), "UTF-8");
                        data += "&" + URLEncoder.encode("instanceID", "UTF-8") + "=" + URLEncoder.encode(instanceID.getId(), "UTF-8");

                        URL urlOznaciPrikazan = NetworkUtils.buildUrlOznaciPrikazan();
                        URLConnection conn = urlOznaciPrikazan.openConnection();
                        conn.setDoOutput(true);

                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                        wr.write( data );
                        wr.flush();

                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line = null;

                        // Read Server Response
                        while((line = reader.readLine()) != null)
                        {
                            sb.append(line + "\n");
                        }

                        text = sb.toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                    }
                }else {
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(R.drawable.ic_sentiment_very_satisfied_black_24px)
                    .setLargeIcon(largeIcon(context))
                    .setContentTitle(sadrzaj.Naziv)
                    .setContentText(sadrzaj.SkraceniOpis)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(sadrzaj.DugiOpis))
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent(context, sadrzaj))
                    .addAction(navodiMeNotifikacijaAkcija(context, sadrzaj))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            notificationManager.notify(SADRZAJ_PODSJETNIK_ID, notificationBuilder.build());

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {

        }
    }

    private static NotificationCompat.Action otvoriNotifikacijuAkcija(Context context, Sadrzaj sadrzaj) {

        Intent otvoriPreglednikIntent = new Intent(context, ReminderIntentService.class);
        otvoriPreglednikIntent.setAction(ReminderTasks.ACTION_DOHVATI_PRIKAZI_SADRZAJ);
        otvoriPreglednikIntent.putExtra("sadrzaj_pk", sadrzaj.PK);
        PendingIntent incrementWaterPendingIntent = PendingIntent.getService(context, ACTION_OTVORI_PENDING_INTENT_ID, otvoriPreglednikIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action akcija = new NotificationCompat.Action(R.drawable.ic_sentiment_very_satisfied_black_24px,"View", incrementWaterPendingIntent);

        return akcija;
    }


    private static NotificationCompat.Action navodiMeNotifikacijaAkcija(Context context, Sadrzaj sadrzaj) {

        Intent navodiMeIntent = new Intent(context, ReminderIntentService.class);
        navodiMeIntent.setAction(ReminderTasks.ACRTION_DOHVATI_NAVIGATE_SADZAJ);
        navodiMeIntent.putExtra("sadrzaj_pk", sadrzaj.PK);
        navodiMeIntent.putExtra("sadrzaj_latitude", sadrzaj.LokacijaLatitude);
        navodiMeIntent.putExtra("sadrzaj_longitude", sadrzaj.LokacijaLongitude);

        PendingIntent incrementWaterPendingIntent = PendingIntent.getService(context, ACTION_NAVIGATE_PENDING_INTENT_ID, navodiMeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action akcija = new NotificationCompat.Action(R.drawable.ic_sentiment_very_satisfied_black_24px,"Navigate", incrementWaterPendingIntent);

        return akcija;
    }

    private static PendingIntent contentIntent(Context context, Sadrzaj sadrzaj) {
        String url = NetworkUtils.buildUri(sadrzaj.PK).toString();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        return PendingIntent.getActivity(context, SADRZAJ_PODSJETNIK_PENDING_INTENT_ID, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_sentiment_very_satisfied_black_24px);
        return largeIcon;
    }
}
