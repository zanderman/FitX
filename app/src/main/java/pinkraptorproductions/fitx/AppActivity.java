package pinkraptorproductions.fitx;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

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
import pinkraptorproductions.fitx.tasks.LoginTask;
import pinkraptorproductions.fitx.tasks.ValidateSessionTask;


public class AppActivity extends Activity implements ProgressInteractionListener,
        Dashboard.DashboardInteractionListener, Social.SocialInteractionListener,
        Settings.SettingsInteractionListener, MessagesInteractionListener,
        RetainedFragmentInteractionListener {

    // MAKE TAGS FOR THE FRAGMENTS
    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_MESSAGES = "messages";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_SOCIAL = "social";
    private static final String TAG_RETAIN = "retain";
    private static String TAG_CURRENT;

    private static final String KEY_STORE_ITEMID = "itemId";


    //MAKE OBJECTS FOR THE FRAGMENTS
    private Fragment dashboard, progress, settings, social;
    private Messages messages;
    private RetainedFragment retainedFragment;

    // Fragment manager
    private FragmentManager fm;

    // id of the selected ActionBar button
    private int itemId = 0;

    // Declare generic toast.
    private Toast genToast;

    // Shared Preferences
    SharedPreferences prefs;

    // Shared preferences editor.
    SharedPreferences.Editor editor;

    // Define the asynctask
//    SessionTask session;
    ValidateSessionTask validateSessionTask;

    // User Session Object
    Session session;

    // Length of session in milliseconds.
    private final int sleepLength = 500; // milliseconds

    // Flag to check if user credentials were already validated on last resume.
    private volatile boolean loggedIn;



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
                            bundle.getString("url"),
                            bundle.getString("cookie")
                    );

                    // Toast to screen.
                    makeToast("Login successful!");


//                    // Push the session token to SharedPreferences.
//                    editor = getSharedPreferences("usersession", MODE_PRIVATE).edit();
//                    editor.putInt("sessionid", Calendar.getInstance().get(Calendar.MINUTE));
//                    editor.commit();

//                    // Start the new session monitoring task.
//                    session = new SessionTask(this);
//                    session.execute();
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
        if (session == null) session = new Session(this);

        // Initialize generic toast.
        genToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        // Set initial login
        loggedIn = false;

        // Start the retained fragment.
        connectWithRetainedFragment();
    }


    // Run just before activity is running.
    @Override
    protected void onResume() {
        super.onResume();

        // Check user credentials on separate thread.
        prefs = getSharedPreferences("usersession", MODE_PRIVATE);
        new ValidateSessionTask(session).execute(prefs.getString("sessionUser", ""));

//        checkSession();
//
//        // Check if credentials were already validated.
//        if (!loggedIn) loadLogin();
    }

    @Override
    protected void onDestroy() {
//        if (session != null) session.onCancelled();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
                TAG_CURRENT = TAG_DASHBOARD;
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
                TAG_CURRENT = TAG_SETTINGS;
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
                TAG_CURRENT = TAG_SOCIAL;
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
                TAG_CURRENT = TAG_MESSAGES;
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
                TAG_CURRENT = TAG_PROGRESS;
                // Checks if social fragment exists in the manager.
                progress = fm.findFragmentByTag(TAG_PROGRESS);

                if (progress == null) {

                    // Create new instance of the Settings class.
                    progress = Progress.newInstance("a", "z");

                    // Add fragment to manager
                    fm.beginTransaction().replace(R.id.frag, progress, TAG_PROGRESS).commit();
                }
                break;

        }
        return true;
    }

    // Update the generic toast message upon request.
    private void newToast(String message) {
        genToast.setText(message);
        genToast.show();
    }

    //MUST HAVE TWO CALLABACK METHODS HERE THAT RECEIVE DATA FROM PROGRESS.JAVA
    public void saveEntry(Progress.ProgressEntry entry) {
        newToast("[activity] " + "Updated entry with:"
                + "\nMiles: " + entry.miles
                + "Cups: " + entry.cups
                + "\nMinutes: " + entry.minutes
                + "Steps: " + entry.steps);
    }

    public void deleteEntry(Progress.ProgressEntry entry) {
        newToast("[activity] " + "Deleted entry with:"
                + "\nMiles: " + entry.miles
                + "Cups: " + entry.cups
                + "\nMinutes: " + entry.minutes
                + "Steps: " + entry.steps);
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

    public void checkSession() {
        // Get time of onResume method call as save as the current token.
        int minuteCount = Calendar.getInstance().get(Calendar.MINUTE);

        // Get reference to SystemPreferences "usersession".
        prefs = getSharedPreferences("usersession", MODE_PRIVATE);

        // Get the user session id if available.
        int restoredKey = prefs.getInt("sessionid", 999); // returns 999 if this preference does not exits.

        boolean restoreLogin = prefs.getBoolean("restoreLogin", false);

        // Check validate user session id. (id = minute count)
        if ( (restoredKey != minuteCount) || (restoreLogin) ) {
            // Keys do not match, go to postExecute method.
            loggedIn = false;

            // Toast to screen
            makeToast("Session expired.");

            // Change the flag in shared preferences.
            editor = getSharedPreferences("usersession", MODE_PRIVATE).edit();
            editor.putBoolean("restoreLogin", false);
            editor.commit();
        } else {
            loggedIn = true;
        }
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
            Log.d("fragment", "AppActivity started RetainedFragment");
        }
    }

    @Override
    public void startRefreshThread() {

        // Check if the retained fragment is already created.
        if (retainedFragment != null) {
            retainedFragment.startRefreshTask();
            Log.d("AppActivity", "called startRefreshTask()");
        } else {
            Log.d("AppActivity", "tried to start refresh task, but retainedfragment wasn't there.");
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

//    // Task to handle user login session.
//    private class SessionTask extends AsyncTask<Void, Integer, Boolean> {
//
//         private AppActivity activity;
//
//        public SessionTask(AppActivity activity) {
//            this.activity = activity;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//
//            while (loggedIn) {
//                try {
//                    Thread.sleep(sleepLength);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                this.activity.checkSession();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onPostExecute(Boolean bool) {
//            super.onPostExecute(bool);
//
//            // Push the flag to SharedPreferences.
//            editor = getSharedPreferences("usersession", MODE_PRIVATE).edit();
//            editor.putBoolean("restoreLogin", true);
//            editor.commit();
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//
//        @Override
//        protected void onCancelled(Boolean bool) {
//            super.onCancelled(bool);
//        }
//
//        @Override
//        protected void onCancelled() {
//            super.onCancelled();
//        }
//    }
}
