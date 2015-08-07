package pinkraptorproductions.fitx.tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import pinkraptorproductions.fitx.AppActivity;
import pinkraptorproductions.fitx.fragments.Progress;

/**
 * Created by lndsharkfury on 7/25/15.
 */
public class RefreshTask implements Runnable {

    private static final String KEY_STORE_MILES = "miles";
    private static final String KEY_STORE_MINUTES = "minutes";
    private static final String KEY_STORE_CUPS = "cups";
    private static final String KEY_STORE_STEPS = "steps";
    private static final String KEY_STORE_DATE = "date";
    private static final String KEY_STORE_ID = "_id";

    private String cookie, user, url;
    private Handler handler;

    // Task constructor
    public RefreshTask(String cookie, String user, String url, Handler handler) {
        this.cookie = cookie;
        this.user = user;
        this.url = url;
        this.handler = handler;
    }


    @Override
    public void run() {

        // Generate a random number.
        Bundle bundle = new Bundle();
        try {
            bundle = loadUserProgress(this.user);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Generate new message object and populate with bundle.
        Message message = new Message();
        message.setData(bundle);

        // Send the message with bundle stored inside it.
        this.handler.sendMessage(message);
        Log.d("RefreshTask", "sent handler message");
    }

    //loads user's progress.
    private Bundle loadUserProgress(String user) throws IOException, JSONException {


        InputStream is = null;
        Bundle bundle = new Bundle();

        try {
            String query = this.url + "/rest/progress?username=" + user;
            Log.d("hw4", "[refresh] query = " + query);

            HttpURLConnection conn = (HttpURLConnection) ((new URL(query).openConnection()));
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Cookie", this.cookie);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.connect();


            // handling the response
            StringBuilder sb = new StringBuilder();
            int HttpResult = conn.getResponseCode();
            is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();


            //handle response
            JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
            ArrayList<Progress.ProgressEntry> entries = new ArrayList<Progress.ProgressEntry>();
            reader.beginArray();
            while (reader.hasNext()) {
                entries.add(convertToProgress(reader));
            }

            bundle.putBundle("data", convertToBundle(entries));

            reader.endArray();

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {
            Log.d("hw4", " and the exception is " + e);
            bundle.putBoolean("result", false);
            return bundle;

        } finally {
            if (is != null) {
                is.close();
            }
        }

        bundle.putBoolean("result", true);
        return bundle;
    }



    // --------------




    //converter method
    private Progress.ProgressEntry convertToProgress(JsonReader reader) throws IOException {
        String _id = null;
        String date = "";
        double miles = -1;
        int steps = -1;
        int minutes = -1;
        double cups = -1;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("cups")) {
                cups = reader.nextDouble();
            } else if (name.equals("miles")) {
                miles = reader.nextDouble();
            } else if (name.equals("steps")) {
                steps = reader.nextInt();
            } else if (name.equals("duration")) {
                minutes = reader.nextInt();
            } else if (name.equals("_id")) {
                _id = reader.nextString();
            } else if (name.equals("date")) {
                date = reader.nextString();
            }

        }
        reader.endObject();
        if (_id != null) {

            return new Progress.ProgressEntry(steps, (float) miles, minutes, (float) cups, _id, date);
        }
        return null;
    }

    //----------

    private Bundle convertToBundle(ArrayList<Progress.ProgressEntry> entries) {
        float[] store_miles = new float[entries.size()];
        float[] store_cups = new float[entries.size()];
        int[] store_steps = new int[entries.size()];
        int[] store_minutes = new int[entries.size()];
        String[] store_id = new String[entries.size()];
        String[] store_date = new String[entries.size()];

        // Go through each list entry and populate the save arrays.
        for (int i = 0; i < entries.size(); i++) {
            Log.d("hw4", "[download]: id["+i+"] = "
                + entries.get(i).id
            );

            store_miles[i] = entries.get(i).miles;
            store_cups[i] = entries.get(i).cups;
            store_steps[i] = entries.get(i).steps;
            store_minutes[i] = entries.get(i).minutes;
            store_id[i] = entries.get(i).id;
            store_date[i] = entries.get(i).date;
        }

        Bundle bundle = new Bundle();

        // Put the save arrays into the outState bundle.
        bundle.putFloatArray(KEY_STORE_MILES, store_miles);
        bundle.putFloatArray(KEY_STORE_CUPS, store_cups);
        bundle.putIntArray(KEY_STORE_STEPS, store_steps);
        bundle.putIntArray(KEY_STORE_MINUTES, store_minutes);
        bundle.putStringArray(KEY_STORE_DATE, store_date);
        bundle.putStringArray(KEY_STORE_ID, store_id);

        return bundle;
    }
}
