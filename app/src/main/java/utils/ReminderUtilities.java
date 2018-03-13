package utils;

import android.content.Context;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by jelav on 26/02/2018.
 */

public class ReminderUtilities {
    private static final int REMINDER_INTERVAL_MINUTES = 20;
    private static final int REMINDER_INTERVAL_SECONDS = (int)(TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = REMINDER_INTERVAL_SECONDS;

    private static final String REMINDER_JOB_TAG = "obavijest";
    private static boolean sInitialized;

    synchronized public static void scheduleChargingReminder(final Context context){
        if(sInitialized) return;

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(ReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(10, REMINDER_INTERVAL_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintReminderJob);
        sInitialized = true;
    }
}
