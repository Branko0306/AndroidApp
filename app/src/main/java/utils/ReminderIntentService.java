package utils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.SupportActivity;

import models.IntentServiceExtras;

public class ReminderIntentService extends IntentService {

    public ReminderIntentService() {
        super("ReminderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        int sadrzaj_pk = intent.getIntExtra("sadrzaj_pk", 0);
        Double latitude = intent.getDoubleExtra("sadrzaj_latitude", 0);
        Double longitude = intent.getDoubleExtra("sadrzaj_longitude", 0);

        IntentServiceExtras extras = new IntentServiceExtras();
        extras.sadrzaj_pk = sadrzaj_pk;
        extras.sadrzaj_latitude = latitude;
        extras.sadrzaj_longitude = longitude;

        ReminderTasks.executeTask(this, action, extras);
    }
}
