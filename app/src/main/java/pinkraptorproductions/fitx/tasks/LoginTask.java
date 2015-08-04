package pinkraptorproductions.fitx.tasks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pinkraptorproductions.fitx.classes.Session;

/**
 * Created by lndsharkfury on 8/2/15.
 */
public class LoginTask extends AsyncTask<String, Integer, String>{

    private Session session;

    // Constructor
    public LoginTask(Session session) {
        super();

        // Initialize attributes
        this.session = session;
    }

    // Do login method in background.
    @Override
    protected String doInBackground(String... params) {

        // Initialize parameters.
        String myurl, username, password;
        myurl = params[0];
        username = params[1];
        password = params[2];

        // Login the current user.
        try {
            return this.session.login(myurl, username, password);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return null if exceptions were thrown
        return null;
    }

    // Run after user has logged in.
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        // Push the cookie to shared preferences
        SharedPreferences.Editor editor = this.session.getPrefs().edit();
//        editor.putString("sessionid", s);
        editor.putInt("sessionid", Integer.parseInt(s));
        editor.putString("sessionUser", this.session.getUser());
        editor.commit();
    }
}
