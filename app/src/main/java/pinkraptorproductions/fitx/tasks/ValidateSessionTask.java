package pinkraptorproductions.fitx.tasks;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;

import pinkraptorproductions.fitx.classes.Session;

/**
 * Created by lndsharkfury on 8/2/15.
 */
public class ValidateSessionTask extends AsyncTask<String, Integer, Boolean> {

    private Session session;

    public ValidateSessionTask(Session session) {
        super();

        // Intiialize attributes
        this.session = session;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        // Initialize user parameter.
        String user = params[0];

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
    }
}
