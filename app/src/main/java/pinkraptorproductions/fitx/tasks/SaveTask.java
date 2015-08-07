package pinkraptorproductions.fitx.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import pinkraptorproductions.fitx.AppActivity;
import pinkraptorproductions.fitx.R;
import pinkraptorproductions.fitx.fragments.Progress;

/**
 * Created by lndsharkfury on 8/5/15.
 */
public class SaveTask extends AsyncTask<Progress.ProgressEntry, Integer, Void> {

    private Context context;
    private Progress.ProgressEntry entry;

    public SaveTask(Context context) {
        super();

        this.context = context;
    }

    @Override
    protected Void doInBackground(Progress.ProgressEntry... params) {

        // Save the entry for on post execute.
        this.entry = params[0];

        // Get shared preferences.
        SharedPreferences prefs = this.context.getSharedPreferences(
                this.context.getString(R.string.sp_tag_session),
                context.MODE_PRIVATE
        );

        // Get the current username.
        String username = prefs.getString(
                this.context.getString(R.string.sp_tag_session_username),
                "default"
        );

        try {

            String query = AppActivity.BASE_URL + "/dashboard/entryupdate";
            Log.d("hw4", "[save] query = " + query);

            HttpURLConnection conn = (HttpURLConnection) ((new URL(query).openConnection()));
            conn.setDoOutput(true);
            conn.setReadTimeout(2000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.connect();


            JSONObject toupdate = new JSONObject();
            toupdate.put("username",username);
            toupdate.put("_id", params[0].id);
            toupdate.put("cups", params[0].cups);
            toupdate.put("miles", params[0].miles);
            toupdate.put("duration", params[0].minutes);
            toupdate.put("steps", params[0].steps);
            toupdate.put("date", params[0].date);
            Log.d("hw4", "[update]this is what gets sent JSON:" + toupdate.toString());
            // posting it
            Writer wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(toupdate.toString());
            wr.flush();
            wr.close();
            Log.d("vt", " response from the server is" + conn.getResponseCode());
            // handling the response


            if (conn.getResponseCode() < 400) {
                Log.d("hw4", "update entry:" + params[0] + " from:" + username);
            } else {
                Log.d("hw4", "didn't update");
            }


            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {
            Log.d("vt", " and the exception is " + e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("saveentry", false);
        editor.commit();

        AppActivity.newToast("[activity] " + "Updated entry with:"
                + "\nMiles: " + this.entry.miles
                + "Cups: " + this.entry.cups
                + "\nMinutes: " + this.entry.minutes
                + "Steps: " + this.entry.steps);
    }
}
