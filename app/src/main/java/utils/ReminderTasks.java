package utils;


import android.content.Context;

public class ReminderTasks {

    public static final String ACTION_DOHVATI_PRIKAZI_NOTIFIKACIJU = "dohvati-prikazi-notifikaciju";
    public static final String ACTION_OCISTI_NOTIFIKACIJE = "ocisti-notifikacije";
    public static final String ACTION_PRIKAZI_NOTIFIKACIJU_PODSJETNIK = "prikazi-notifikaciju-podsjetnik";

    public static void executeTask(Context context, String action) {
        if (ACTION_DOHVATI_PRIKAZI_NOTIFIKACIJU.equals(action)) {
            dohvatiPrikaziNotifikaciju(context);
        } else if (ACTION_OCISTI_NOTIFIKACIJE.equals(action)) {
            NotificationUtils.ocistiSveNotifikacije(context);
        } else if(ACTION_PRIKAZI_NOTIFIKACIJU_PODSJETNIK.equals(action)){
            dohvatiSadrzajPrikaziNotifikaciju(context);
        }
    }

    private static void ocistiSveNotifikacije(Context context){
        NotificationUtils.ocistiSveNotifikacije(context);
    }

    private static void dohvatiSadrzajPrikaziNotifikaciju(Context context) {
        NotificationUtils.dohvatiSadrzajPrikaziNotifikaciju(context);
    }

    private static void dohvatiPrikaziNotifikaciju(Context context) {
        NotificationUtils.ocistiSveNotifikacije(context);
    }
}