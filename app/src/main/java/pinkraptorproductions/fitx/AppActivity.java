package pinkraptorproductions.fitx;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import pinkraptorproductions.fitx.classes.Session;
import pinkraptorproductions.fitx.fragments.Dashboard;
import pinkraptorproductions.fitx.fragments.Messages;
import pinkraptorproductions.fitx.fragments.Progress;
import pinkraptorproductions.fitx.fragments.RetainedFragment;
import pinkraptorproductions.fitx.fragments.Settings;
import pinkraptorproductions.fitx.fragments.Social;
import pinkraptorproductions.fitx.interfaces.MessagesInteractionListener;
import pinkraptorproductions.fitx.interfaces.ProgressInteractionListener;
import pinkraptorproductions.fitx.interfaces.RetainedFragmentInteractionListener;
import pinkraptorproductions.fitx.interfaces.ValidateSessionInterface;
import pinkraptorproductions.fitx.services.ContinuousRefreshService;
import pinkraptorproductions.fitx.tasks.DeleteTask;
import pinkraptorproductions.fitx.tasks.SaveTask;
import pinkraptorproductions.fitx.tasks.ValidateSessionTask;


public class AppActivity extends Activity implements ServiceConnection,
        ProgressInteractionListener,
        Dashboard.DashboardInteractionListener, Social.SocialInteractionListener,
        Settings.SettingsInteractionListener, MessagesInteractionListener,
        RetainedFragmentInteractionListener,
        ValidateSessionInterface {

    // MAKE TAGS FOR THE FRAGMENTS
    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_MESSAGES = "messages";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_SOCIAL = "social";
    private static final String TAG_RETAIN = "retain";

    private static final String KEY_STORE_MILES = "miles";
    private static final String KEY_STORE_MINUTES = "minutes";
    private static final String KEY_STORE_CUPS = "cups";
    private static final String KEY_STORE_STEPS = "steps";
    private static final String KEY_STORE_DATE = "date";
    private static final String KEY_STORE_ID = "_id";

    // Network URL.
    public static final String BASE_URL="http://128.173.236.164:3000";


    //MAKE OBJECTS FOR THE FRAGMENTS
    private Fragment dashboard, settings, social;
    private Progress progress;
    private Messages messages;
    private RetainedFragment retainedFragment;

    // Fragment manager
    private FragmentManager fm;

    // id of the selected ActionBar button
    private int itemId = 0;

    // Declare generic toast.
    private static Toast genToast;

    // Shared Preferences
    SharedPreferences prefs;

    // Shared preferences editor.
    SharedPreferences.Editor editor;

    // Define the asynctask
    ValidateSessionTask validateSessionTask;

    // User Session Object
    Session session;

    // Length of session in milliseconds.
    private final int sleepLength = 500; // milliseconds

    // Flag to check if user credentials were already validated on last resume.
    private volatile boolean loggedIn;

    private Bundle downloadedEntries;

    // Boolean to tell if you're bound with a service
    boolean bound;

    // SP to tell what's going on inside the service
    private SharedPreferences serviceStatus;

    // Service attribute
    ContinuousRefreshService service;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (data.getStringExtra("login")) {
                case "success":

                    // User credentials were valid.
                    loggedIn = true;

                    Bundle bundle = data.getExtras();

                    // Save new session object.
                    this.session = new Session(
                            this,
                            BASE_URL,
                            bundle.getString("cookie")
                    );

                    prefs = getSharedPreferences(getString(R.string.sp_tag_session), MODE_PRIVATE);
                    Log.d("hw4", "sessionid = " + prefs.getString("sessionid", "default") + " | sessionUser = " + prefs.getString("sessionUser", "default"));

                    // Toast to screen.
                    makeToast("Login successful!");

                    if (!serviceIsStarted()) {
                        this.startService(new Intent(AppActivity.this, ContinuousRefreshService.class));
                    }
                    bindWithService();
                    break;

                case "exit":
                    System.exit(0);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_layout);

        // Initialize fragment manager
        fm = getFragmentManager();

        // Initialize session object with context
        if (session == null) session = new Session(this, BASE_URL, null);

        // Initialize generic toast.
        genToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        // Set initial login
        loggedIn = false;

        // Initialize downloadedEntries
        downloadedEntries = null;

        // Start the retained fragment.
        connectWithRetainedFragment();

        // Bind with service
//        bindWithService();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the progress fragment
        progress = (Progress) fm.findFragmentByTag(TAG_PROGRESS);

        if (!serviceIsStarted()) {
            this.startService(new Intent(AppActivity.this, ContinuousRefreshService.class));
        }
        bindWithService();
    }

    // Run just before activity is running.
    @Override
    protected void onResume() {
        super.onResume();

//        if (progress == null) {
//            makeToast("progress was null onResume.");
//            // Restore the progress fragment
//            progress = (Progress) fm.findFragmentByTag(TAG_PROGRESS);
//        }

        // Check user credentials on separate thread.
        prefs = getSharedPreferences("usersession", MODE_PRIVATE);
        new ValidateSessionTask(this, session).execute(prefs.getString("sessionUser", "default"));
    }

    @Override
    protected void onDestroy() {
//        if (session != null) session.onCancelled();
        if (serviceIsStarted()) {
//            service.onDestroy();
            getApplicationContext().stopService(new Intent(this, ContinuousRefreshService.class));
            Log.d("hw4","[stopping] service from activity onDestroy.");
        }
        super.onDestroy();
    }



    // this is triggered whenever activity is created (in cases when buttons are hidden, this
    // callback happens when the menu button is pressed.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);

        this.getActionBar().setDisplayShowHomeEnabled(false);
        //removing the title that goes onto the actionbar
        this.getActionBar().setDisplayShowTitleEnabled(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        if (bound) {
            Log.d("hw4", "[unbinding] from service");
            this.unbindService(this);
        }
        else Log.d("hw4", "already [unbound] from service");

        super.onPause();
    }


    /*
    * Service methods
    * */

    // Bind with the service
    public void bindWithService() {
        serviceStatus = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d("hw4","attempting to bind with service...");
        Log.d("hw4", "current service status is: " + serviceStatus.contains("started"));

        if (serviceIsStarted()) {
            Intent intent = new Intent(AppActivity.this, ContinuousRefreshService.class);
            // Bind with the service
            this.bindService(intent, this, Context.BIND_AUTO_CREATE);

            // Change bound flag value;
            bound = true;
        }
    }

    // Determine if the service is already started
    public boolean serviceIsStarted() {
        serviceStatus = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean flag = serviceStatus.contains("started") && serviceStatus.getBoolean("started", false);
        Log.d("hw4", "service is running?: " + flag);
        return flag;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ContinuousRefreshService.LocalBinder binder = (ContinuousRefreshService.LocalBinder) service;
        this.service = binder.getServiceInstance();

        // Assign the activity to send callbacks to.
        this.service.sendCallbacks(this);

        Log.d("hw4","Service is [connected] to AppActivity");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("hw4","Service is [disconnected] to AppActivity");
    }



    //helper method that removes the current fragment
    //if you don't remove current fragment and add a new one
    // the fragments will overlap
    private void removeFragments() {
        if (progress != null) {
            fm.beginTransaction().remove(progress).commit();
        }
        if (social != null) {
            fm.beginTransaction().remove(social).commit();
        }
        if (settings != null) {
            fm.beginTransaction().remove(settings).commit();
        }
        if (messages != null) {
            fm.beginTransaction().remove(messages).commit();
        }
        if (dashboard != null) {
            fm.beginTransaction().remove(dashboard).commit();
        }

    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        //remove current fragment if different button pressed
        if (itemId != item.getItemId()) {
            removeFragments();
            itemId = item.getItemId();
        }
        //do nothing if same button pressed
        else {
            return false;
        }

        switch (itemId) {

            // Dashboard icon is clicked.
            case R.id.action_dashboard:
                // Checks if dashboard fragment exists in the manager.
                dashboard = fm.findFragmentByTag(TAG_DASHBOARD);

                //create new fragment instance (manager returned null)
                if (dashboard == null) {

                    // Create new instance of the Dashboard class.
                    dashboard = Dashboard.newInstance("a", "z");

                    // Add fragment to manager
                    fm.beginTransaction().replace(R.id.frag, dashboard, TAG_DASHBOARD).commit();
                }
                break;


            // Settings icon is clicked.
            case R.id.action_settings:
                // Checks if settings fragment exists in the manager.
                settings = fm.findFragmentByTag(TAG_SETTINGS);

                if (settings == null) {

                    // Create new instance of the Settings class.
                    settings = Settings.newInstance("a", "z");

                    // Add fragment to manager
                    fm.beginTransaction().replace(R.id.frag, settings, TAG_SETTINGS).commit();
                }
                break;

            // Social icon is clicked.
            case R.id.action_social:
                // Checks if social fragment exists in the manager.
                social = fm.findFragmentByTag(TAG_SOCIAL);

                if (social == null) {

                    // Create new instance of the Settings class.
                    social = Social.newInstance("a", "z");

                    // Add fragment to manager
                    fm.beginTransaction().replace(R.id.frag, social, TAG_SOCIAL).commit();
                }
                break;

            // Messages icon is clicked.
            case R.id.action_messages:
                // Checks if social fragment exists in the manager.
                messages = (Messages) fm.findFragmentByTag(TAG_MESSAGES);

                if (messages == null) {

                    // Create new instance of the Settings class.
                    messages = Messages.newInstance("a", "z");

                    // Add fragment to manager
                    fm.beginTransaction().replace(R.id.frag, messages, TAG_MESSAGES).commit();
                }
                break;

            // Progress icon is clicked.
            case R.id.action_progress:
                // Checks if social fragment exists in the manager.
                progress = (Progress) fm.findFragmentByTag(TAG_PROGRESS);

                if (progress == null) {

                    // Create new instance of the Settings class.
//                    progress = Progress.newInstance("a", "z");
                    if (downloadedEntries != null && downloadedEntries.containsKey(KEY_STORE_ID)) {
                        if (downloadedEntries.getStringArray(KEY_STORE_ID).length != 0) {
                            // Pass in downloaded entries
                            progress = Progress.newInstance("a", "z", downloadedEntries);
                        }
                        else {
                            // Don't pass in entries
                            progress = Progress.newInstance("a", "z", null);
                        }
                    }
                    else {
                        // Don't pass in entries
                        progress = Progress.newInstance("a", "z", null);
                    }

                    // Add fragment to manager
                    fm.beginTransaction().replace(R.id.frag, progress, TAG_PROGRESS).commit();
                }
                break;

        }
        return true;
    }

    // Update the generic toast message upon request.
    public static void newToast(String message) {
        genToast.setText(message);
        genToast.show();
    }

    //MUST HAVE TWO CALLABACK METHODS HERE THAT RECEIVE DATA FROM PROGRESS.JAVA
    public void saveEntry(Progress.ProgressEntry entry) {

        editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putBoolean("saveentry", true);
        editor.commit();


        if (service == null)
            new SaveTask(this).execute(entry);
        else {
            // Start AsyncTask for saving an entry.
            Log.d("hw4", "starting SaveTask");
            service.stopServiceTask();
            new SaveTask(this).execute(entry);
            Log.d("hw4", "starting SaveTask");
            service.startServiceTask();
        }
//        // Start AsyncTask for saving an entry.
//        Log.d("hw4", "starting SaveTask");
//        service.stopServiceTask();
//        new SaveTask(this).execute(entry);
//        Log.d("hw4", "starting SaveTask");
//        service.startServiceTask();
    }

    public void deleteEntry(Progress.ProgressEntry entry) {

        editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putBoolean("deleteentry", true);
        editor.commit();


        if (service == null)
            new DeleteTask(this, entry).execute(entry.id);
        else {
            Log.d("hw4", "starting DeleteTask");
            service.stopServiceTask();
            new DeleteTask(this, entry).execute(entry.id);
            Log.d("hw4", "stopping DeleteTask");
            service.startServiceTask();
        }
//        // Start AsyncTask for deleting an entry.
//        Log.d("hw4", "starting DeleteTask");
//        service.stopServiceTask();
//        new DeleteTask(this, entry).execute(entry.id);
//        Log.d("hw4", "stopping DeleteTask");
//        service.startServiceTask();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Load the login screen.
    public void loadLogin () {
        // Start the login activity.
        Intent login = new Intent(AppActivity.this, LoginActivity.class);
        startActivityForResult(login, 0);
    }

    @Override
    public void startNewService() {
        loggedIn = true;
        if (!serviceIsStarted()) {
            this.startService(new Intent(AppActivity.this, ContinuousRefreshService.class));
        }
        bindWithService();
    }

    // Update the generic toast message upon request.
    private void makeToast(String message) {
        genToast.setText(message);
        genToast.show();
    }

    // Called by the Messages fragment on button click.
    private void connectWithRetainedFragment() {

        // Get possible retainedFragment instance.
        retainedFragment = (RetainedFragment) fm.findFragmentByTag(TAG_RETAIN);

        // Check fragment existance.
        if (retainedFragment == null) {

            // Initialize new retainedFragment instance.
            retainedFragment = RetainedFragment.newInstance("a", "z");
            retainedFragment.setRetainInstance(true);

            // Add retainedFragment to the fragment manager
            fm.beginTransaction().add(retainedFragment, TAG_RETAIN).commit();
            Log.d("hw4", "AppActivity started RetainedFragment");
        }
    }

    @Override
    public void startRefreshThread() {

        // Check if the retained fragment is already created.
        if (retainedFragment != null) {
            prefs = getSharedPreferences("usersession", MODE_PRIVATE);
            String user = prefs.getString(getString(R.string.sp_tag_session_username), "default");

            Log.d("hw4","starting refresh thread: user=" + user + " | cookie=" + this.session.getCookie());
            retainedFragment.initiateProgressLoad(
                    this.session.getCookie(),
                    user
            );
            Log.d("hw4", "called startRefreshTask()");
        } else {
            Log.d("hw4", "tried to start refresh task, but retainedfragment wasn't there.");
        }
    }

    private void sendMessagesData(int value, boolean showSpinner) {
        editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putString("retain_data", Integer.toString(value));
        editor.putBoolean("retain_spinner", showSpinner);
        editor.commit();
    }

    @Override
    public void onRand(int value) {

        // Check if the retained fragment is already created.
        if (messages != null) {
            if (messages.isVisible()) {
                messages.updateText(value);
            } else {
                // Commit the data to shared preferences so that it loads on next fragment load.
                sendMessagesData(value, false);
            }
        } else {
            // Commit the data to shared preferences so that it loads on next fragment load.
            sendMessagesData(value, false);
        }
    }

    public void newEntries(Bundle data) {

        Log.d("hw4","[activity] data bundle length: " + data.size());

        Log.d("hw4", "adding entries");

        if (progress == null) {
            Log.d("hw4", "Progress fragment was null.");
            for (int i = 0; i < data.getStringArray(KEY_STORE_ID).length; i++) {

                //Log the entries
                Log.d("hw4", "entry ["+Integer.toString(i)+"]: "
                                + "\nsteps=" + Integer.toString(data.getIntArray(KEY_STORE_STEPS)[i])
                                + "\nmiles=" + Float.toString(data.getFloatArray(KEY_STORE_MILES)[i])
                                + "\nminutes=" + Integer.toString(data.getIntArray(KEY_STORE_MINUTES)[i])
                                + "\ncups=" + Float.toString(data.getFloatArray(KEY_STORE_CUPS)[i])
                                + "\nid=" + data.getStringArray(KEY_STORE_ID)[i]
                                + "\ndate=" + data.getStringArray(KEY_STORE_DATE)[i]
                );

                downloadedEntries = data;
            }
        }

        // Progress fragment is currently open
        else {
            for (int i = 0; i < data.getStringArray(KEY_STORE_ID).length; i++) {

                progress.addEntry(
                        data.getIntArray(KEY_STORE_STEPS)[i],
                        data.getFloatArray(KEY_STORE_MILES)[i],
                        data.getIntArray(KEY_STORE_MINUTES)[i],
                        data.getFloatArray(KEY_STORE_CUPS)[i],
                        data.getStringArray(KEY_STORE_ID)[i],
                        data.getStringArray(KEY_STORE_DATE)[i]
                );

                //Log the entries
                Log.d("hw4", "entry ["+Integer.toString(i)+"]: "
                                + "\nsteps=" + Integer.toString(data.getIntArray(KEY_STORE_STEPS)[i])
                                + "\nmiles=" + Float.toString(data.getFloatArray(KEY_STORE_MILES)[i])
                                + "\nminutes=" + Integer.toString(data.getIntArray(KEY_STORE_MINUTES)[i])
                                + "\ncups=" + Float.toString(data.getFloatArray(KEY_STORE_CUPS)[i])
                                + "\nid=" + data.getStringArray(KEY_STORE_ID)[i]
                                + "\ndate=" + data.getStringArray(KEY_STORE_DATE)[i]
                );
            }
        }

//        for (int i = 0; i < data.getStringArray(KEY_STORE_ID).length; i++) {
//
//            //Log the entries
//            Log.d("hw4", "entry ["+Integer.toString(i)+"]: "
//                + "\nsteps=" + Integer.toString(data.getIntArray(KEY_STORE_STEPS)[i])
//                + "\nmiles=" + Float.toString(data.getFloatArray(KEY_STORE_MILES)[i])
//                + "\nminutes=" + Integer.toString(data.getIntArray(KEY_STORE_MINUTES)[i])
//                + "\ncups=" + Float.toString(data.getFloatArray(KEY_STORE_CUPS)[i])
//                + "\nid=" + data.getStringArray(KEY_STORE_ID)[i]
//                + "\ndate=" + data.getStringArray(KEY_STORE_DATE)[i]
//            );
//
//            downloadedEntries = data;
//        }
    }
}
