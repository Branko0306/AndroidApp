package utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ReminderFirebaseJobService  extends JobService {

    private AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(final JobParameters params) {

        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = ReminderFirebaseJobService.this;
                ReminderTasks.executeTask(context, ReminderTasks.ACTION_PRIKAZI_NOTIFIKACIJU_PODSJETNIK);
                return null;
            }

            @Override
            protected void onPostExecute(Object o){
                jobFinished(params, false);
            }
        };

        mBackgroundTask.execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        if(mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}
