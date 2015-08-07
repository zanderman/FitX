package pinkraptorproductions.fitx.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import pinkraptorproductions.fitx.AppActivity;

public class ContinuousRefreshService extends Service {

    IBinder binder = new LocalBinder();
    private AppActivity activity;
    private ServiceTask task;
    private static final int refreshTime = 5; // seconds

    //An instance of binder will be used to bind with the this service
    // from elsewhere
    //
    // binder can start a service even if its dead.
    public class LocalBinder extends Binder {

        public ContinuousRefreshService getServiceInstance() {
            return ContinuousRefreshService.this;
        }
    }

    public ContinuousRefreshService() {
    }

    // happens when someone binds to the service from outside
    @Override
    public IBinder onBind(Intent intent) {

        // update SP
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=server_status.edit();
        editor.putBoolean("bind", true); // stores key/value pairs.
        editor.commit();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        // update SP
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=server_status.edit();
        editor.putBoolean("bind", false); // stores key/value pairs.
        editor.commit();

        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d("hw4", "service started");

        // update SP
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=server_status.edit();
        editor.putBoolean("started", true);
        editor.commit();

        // Start the AsyncTask
        startServiceTask();
//        task = new ServiceTask();
//        task.execute();

        return START_STICKY;
    }

    public void startServiceTask() {
        // Start the AsyncTask
        task = new ServiceTask();
        task.execute();
    }

    public void stopServiceTask() {
        if (task != null)
            task.onCancelled();
    }

    @Override
    public void onDestroy() {

        // update SP
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = server_status.edit();
        editor.putBoolean("started", false);
        editor.commit();

        // Cancel the AsynTask
        if (task != null) {
            task.onCancelled();
            Log.d("hw4","[cancelled] the refresh task onDestroy.");
        }

        super.onDestroy();
    }



    // allows service to have reference to the binded activity.
    public void sendCallbacks(AppActivity activity) {
        this.activity = activity;
    }


    public class ServiceTask extends AsyncTask<Void, Void, Void> {

        volatile boolean run;
        SharedPreferences prefs;

        public ServiceTask() {
            super();
            this.run = true;
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        protected Void doInBackground(Void... params) {

            while(run) {
                try {
                    Thread.sleep(refreshTime * 1000); // convert to milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                prefs = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());

                // Start refreshthread on AppActivity
                Log.d("hw4","attempting service task again...");
                Log.d("hw4","network: " + isNetworkAvailable());
                Log.d("hw4","deleteentry: " + prefs.getBoolean("deleteentry", false));
                Log.d("hw4","saveentry: " + prefs.getBoolean("saveentry", false));

                if (activity != null && isNetworkAvailable() && !prefs.getBoolean("deleteentry", false) && !prefs.getBoolean("saveentry", false)) {
                    Log.d("hw4","network is available and activity is not null.");
                    activity.startRefreshThread();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            this.run = false;
        }
    }
}
