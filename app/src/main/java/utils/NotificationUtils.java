package utils;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.jelav.contentdelivery.MainActivity;
import com.example.jelav.contentdelivery.R;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import database.ApplicationDatabase;
import database.SadrzajLogEntityDao;
import models.Sadrzaj;
import models.SadrzajResponse;
import network.NetworkUtils;
import network.QueryFilters;

/**
 * Created by jelav on 26/02/2018.
 */

public class NotificationUtils {

    private static final int SADRZAJ_PODSJETNIK_ID = 1234;
    private static final int SADRZAJ_PODSJETNIK_PENDING_INTENT_ID = 4321;
    private static final int ACTION_OTVORI_PENDING_INTENT_ID = 1;
    private static final int ACTION_ODUSTANI_PENDING_INTENT_ID = 14;

    private static final String NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    public static void ocistiSveNotifikacije(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void dohvatiSadrzajPrikaziNotifikaciju(Context context) {

        ApplicationDatabase db = Room.databaseBuilder(context, ApplicationDatabase.class, "AppDatabase").build();
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_name), NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(mChannel);
            }

            SadrzajLogEntityDao dao = db.SadrzajLogEntityDao();
            List<Integer> nemojDohvatiti = dao.getSkrivenPrikazanaObavijestAll();

            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_name), (Context.MODE_PRIVATE));
            Double latitude = Double.valueOf(sharedPref.getString(context.getString(R.string.latitude), "0"));
            Double longitude = Double.valueOf(sharedPref.getString(context.getString(R.string.longitude), "0"));

            QueryFilters filters = new QueryFilters();
            filters.najblizi = true;
            filters.Latitude = latitude;
            filters.Longitude = longitude;
            filters.skriveniSadrzaji = new ArrayList<Integer>();

            URL url = NetworkUtils.buildUrl(filters);
            String result = null;
            Sadrzaj sadrzaj = null;
            try {
                result = NetworkUtils.getResponseFromHttpUrl(url);
                sadrzaj = SadrzajWrapper.fromJsonSingle(result);

                if(sadrzaj == null) {
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(R.drawable.ic_drink_notification)
                    .setLargeIcon(largeIcon(context))
                    .setContentTitle(sadrzaj.Naziv)
                    .setContentText(sadrzaj.SkraceniOpis)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(sadrzaj.DugiOpis))
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent(context))
                    .addAction(otvoriNotifikacijuAkcija(context))
                    .addAction(otkaziNotifikacijuAkcija(context))
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            notificationManager.notify(SADRZAJ_PODSJETNIK_ID, notificationBuilder.build());

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            db.close();
        }
    }

    private static NotificationCompat.Action otkaziNotifikacijuAkcija(Context context) {
        Intent otkaziNotifikacijuIntent = new Intent(context, ReminderIntentService.class);
        otkaziNotifikacijuIntent.setAction(ReminderTasks.ACTION_OCISTI_NOTIFIKACIJE);
        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(context, ACTION_ODUSTANI_PENDING_INTENT_ID, otkaziNotifikacijuIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action akcija = new NotificationCompat.Action(R.drawable.ic_cancel_black_24px,"Otkaži", ignoreReminderPendingIntent);

        return akcija;
    }

    private static NotificationCompat.Action otvoriNotifikacijuAkcija(Context context) {

        Intent otvoriPreglednikIntent = new Intent(context, ReminderIntentService.class);
        otvoriPreglednikIntent.setAction(ReminderTasks.ACTION_DOHVATI_PRIKAZI_NOTIFIKACIJU);
        PendingIntent incrementWaterPendingIntent = PendingIntent.getService(context, ACTION_OTVORI_PENDING_INTENT_ID, otvoriPreglednikIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action akcija = new NotificationCompat.Action(R.drawable.ic_local_drink_black_24px,"Otvori", incrementWaterPendingIntent);

        return akcija;
    }


    private static NotificationCompat.Action navodiMeNotifikacijaAkcija(Context context) {

        Intent navodiMeIntent = new Intent(context, ReminderIntentService.class);
        navodiMeIntent.setAction(ReminderTasks.ACTION_DOHVATI_PRIKAZI_NOTIFIKACIJU);
        PendingIntent incrementWaterPendingIntent = PendingIntent.getService(context, ACTION_OTVORI_PENDING_INTENT_ID, navodiMeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action akcija = new NotificationCompat.Action(R.drawable.ic_local_drink_black_24px,"Otvori", incrementWaterPendingIntent);

        return akcija;
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, SADRZAJ_PODSJETNIK_PENDING_INTENT_ID, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_local_drink_black_24px);
        return largeIcon;
    }
}
