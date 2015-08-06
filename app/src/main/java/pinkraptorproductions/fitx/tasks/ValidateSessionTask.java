package pinkraptorproductions.fitx.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import pinkraptorproductions.fitx.AppActivity;
import pinkraptorproductions.fitx.classes.Session;
import pinkraptorproductions.fitx.interfaces.ValidateSessionInterface;

/**
 * Created by lndsharkfury on 8/2/15.
 */
public class ValidateSessionTask extends AsyncTask<String, Integer, Boolean> {


    private Session session;
    private Activity activity;
    private ValidateSessionInterface sessionInterface;

    public ValidateSessionTask(Activity activity, Session session) {
        super();

        // Intiialize attributes
        this.session = session;
        this.activity = activity;
        this.sessionInterface = (ValidateSessionInterface) activity;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        // Initialize user parameter.
        String user = params[0];
        Log.d("vst", user);

        // Check if the current user is logged in.
        try {
            return this.session.isUserLoggedIn(user);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return null if exceptions were thrown
        return null;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);

        if (!s) {
            Log.d("vst", "user is not logged in");
            sessionInterface.loadLogin();
        }
        else Log.d("vst", "user is already logged in");
    }
}
