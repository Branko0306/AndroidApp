package utils;


import android.content.Context;

import models.IntentServiceExtras;

public class ReminderTasks {

    public static final String ACTION_DOHVATI_PRIKAZI_SADRZAJ = "dohvati-prikazi-sadrzaj";
    public static final String ACTION_OCISTI_NOTIFIKACIJE = "ocisti-notifikacije";
    public static final String ACTION_PRIKAZI_NOTIFIKACIJU_PODSJETNIK = "prikazi-notifikaciju-podsjetnik";
    public static final String ACRTION_DOHVATI_NAVIGATE_SADZAJ = "dohvati-navigate-sadrzaj";

    public static void executeTask(Context context, String action, IntentServiceExtras extras) {

        if (ACTION_DOHVATI_PRIKAZI_SADRZAJ.equals(action)) {
            NotificationUtils.otvoriUBrowseru(context, extras);
        } else if (ACTION_OCISTI_NOTIFIKACIJE.equals(action)) {
            NotificationUtils.ocistiSveNotifikacije(context);
        } else if(ACTION_PRIKAZI_NOTIFIKACIJU_PODSJETNIK.equals(action)){
            NotificationUtils.dohvatiSadrzajPrikaziNotifikaciju(context);
        }else if(ACRTION_DOHVATI_NAVIGATE_SADZAJ.equals(action)){
            NotificationUtils.otvoriNavigaciju(context, extras);
        }
    }
}