package pinkraptorproductions.fitx.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
public class DeleteTask extends AsyncTask<String, Integer, Void> {

    private Context context;
    private Progress.ProgressEntry entry;

    public DeleteTask(Context context, Progress.ProgressEntry entry) {
        super();
        this.context = context;
        this.entry = entry;
    }

    @Override
    protected Void doInBackground(String... params) {

        // Get shared preferences.
        SharedPreferences prefs = this.context.getSharedPreferences(
                this.context.getString(R.string.sp_tag_session),
                context.MODE_PRIVATE
        );

        // Get the current username.
        String username = prefs.getString(
                this.context.getString(R.string.sp_tag_session_username),
                ""
        );

        try {

            String query = AppActivity.BASE_URL + "/dashboard/entrydelete";
            Log.d("hw4", "[delete] query = " + query);

            HttpURLConnection conn = (HttpURLConnection) ((new URL(query).openConnection()));
            conn.setDoOutput(true);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.connect();


            JSONObject todelete = new JSONObject();
            todelete.put("id", params[0]);
            todelete.put("username", username);
            Log.d("hw4", "this is what gets sent JSON:" + todelete.toString());
            // posting it
            Writer wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(todelete.toString());
            wr.flush();
            wr.close();
            Log.d("vt", " response from the server is" + conn.getResponseCode());
            // handling the response


            if (conn.getResponseCode() < 400) {
                Log.d("hw4", "deleted entry:" + params[0] + " from:" + username);
            } else {

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

        AppActivity.newToast("[activity] " + "Deleted entry with:"
                + "\nMiles: " + this.entry.miles
                + "Cups: " + this.entry.cups
                + "\nMinutes: " + this.entry.minutes
                + "Steps: " + this.entry.steps);
    }
}
