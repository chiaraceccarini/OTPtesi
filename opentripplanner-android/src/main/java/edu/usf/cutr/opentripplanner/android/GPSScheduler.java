package edu.usf.cutr.opentripplanner.android;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.opentripplanner.api.model.WalkStep;

import java.util.LinkedList;
import java.util.List;

import util.Log;

/**
 * Created by chiara on 17/08/16.
 */
/*public class GPSScheduler extends JobService {

    /*private static final String TAG = "SyncService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<WalkStep> ws = intent.getParcelableArrayListExtra("step");
        intent.getParcelableArrayListExtra("");
        Messenger callback = intent.getParcelableExtra("messenger");
        Message m = Message.obtain();
        m.what = MyActivity.MSG_SERVICE_OBJ;
        m.obj = this;
        try {
            callback.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        // We don't do any real 'work' in this sample app. All we'll
        // do is track which jobs have landed on our service, and
        // update the UI accordingly.
        jobParamsMap.add(params);
        if (mActivity != null) {
            mActivity.onReceivedStartJob(params);
        }
        Log.i(TAG, "on start job: " + params.getJobId());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Stop tracking these job parameters, as we've 'finished' executing.
        jobParamsMap.remove(params);
        if (mActivity != null) {
            mActivity.onReceivedStopJob();
        }
        Log.i(TAG, "on stop job: " + params.getJobId());
        return true;
    }

    MyActivity mActivity;
    private final LinkedList<JobParameters> jobParamsMap = new LinkedList<JobParameters>();

    public void setUiCallback(MyActivity activity) {
        mActivity = activity;
    }

    /** Send job to the JobScheduler.
    public void scheduleJob(JobInfo t) {
        Log.d(TAG, "Scheduling job");
        JobScheduler tm =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(t);
    }

    /**
     * Not currently used, but as an exercise you can hook this
     * up to a button in the UI to finish a job that has landed
     * in onStartJob().
     */
   /* public boolean callJobFinished() {
        JobParameters params = jobParamsMap.poll();
        if (params == null) {
            return false;
        } else {
            jobFinished(params, false);
            return true;
        }
    }

}*/
