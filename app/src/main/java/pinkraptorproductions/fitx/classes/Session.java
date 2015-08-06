package pinkraptorproductions.fitx.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pinkraptorproductions.fitx.R;

/**
 * Created by lndsharkfury on 8/2/15.
 */
public class Session {

    private SharedPreferences prefs;
    private Context context;
    private String cookie, user;
    private static String url;

    // Constructor with context
    public Session(Context context) {
        super();

        // Initialize parameters
        this.context = context;
        this.prefs = null;
        this.url = null;
        this.cookie = null;
        this.user = null;
    }

    // Constructor with url, cookie
    public Session(Context context, String url, String cookie) {
        super();

        // Initialize parameters
        this.context = context;
        this.prefs = null;
        this.url = url;
        this.cookie = cookie;
        this.user = null;
    }

    // Return shared preferences object.
    public SharedPreferences getPrefs() { return this.prefs; }

    // Return url.
    public String getUrl() { return this.url; }

    // Return cookie.
    public String getCookie() { return this.cookie; }

    // Return user.
    public String getUser() { return this.user; }

    // Return context.
    public Context getContext() { return this.context; }


    //perform login
    public String login(String myurl, String username, String password) throws IOException, JSONException {

        InputStream is = null;
        final String COOKIES_HEADER = "Set-Cookie";

        // Initialize session parameters.
        this.url = myurl;
        this.cookie = "empty cookie";
        this.user = username;
        this.prefs = this.context.getSharedPreferences(
                this.context.getString(R.string.sp_tag_session),
                context.MODE_PRIVATE
        );

        // Try network call
        try {

            String query = myurl + "/login";
            Log.d("hw4", "login query is: " + query);

            HttpURLConnection conn = (HttpURLConnection) ((new URL(query).openConnection()));
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.connect();


            JSONObject cred = new JSONObject();
            cred.put("username", username);
            cred.put("password", password);
            Log.d("hw4", "this is what gets sent JSON:" + cred.toString());
            // posting it
            Writer wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(cred.toString());
            wr.flush();
            wr.close();
            Log.d("hw4", " response from the server is: " + conn.getResponseCode());
            // handling the response
            StringBuilder sb = new StringBuilder();
            int HttpResult = conn.getResponseCode();
            is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();

            if (HttpResult == HttpURLConnection.HTTP_OK) {

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                //  for (String s : cookiesHeader) {

                Log.d("hw4", "cookies: " + cookiesHeader.get(0).substring(0, cookiesHeader.get(0).indexOf(";")));

                this.cookie =  cookiesHeader.get(0).substring(0, cookiesHeader.get(0).indexOf(";"));


            }

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {

            Log.d("vt", " and the exception is " + e);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        Log.d("hw4", "before exiting login, cookie = " + this.cookie);
        return this.cookie;
    }




    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }





    //checks if the session is valid
    public Boolean isUserLoggedIn(String user) throws IOException, JSONException {

        this.prefs = this.context.getSharedPreferences(
                this.context.getString(R.string.sp_tag_session),//"usersession",
                context.MODE_PRIVATE
        );

        Log.d("Session","Intialize preferences");

        if (this.prefs.contains(this.context.getString(R.string.sp_tag_session_id))) {
            this.cookie = this.prefs.getString(
                            this.context.getString(R.string.sp_tag_session_id),
                            "default"
            );
        }
        else this.cookie = "";

        Log.d("Session","Intialize cookie");

        Log.d("hw4","cookie in the onResume is "+this.cookie);
        InputStream is = null;

        try {

            String query = this.url + "/loggedin?username=" + user;
            Log.d("hw4", "isUserLoggedIn query is: " + query);

            HttpURLConnection conn = (HttpURLConnection) ((new URL(query).openConnection())); //this.url + "?" + user
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Cookie", this.cookie);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.connect();

            Log.d("Session", "connected");


            // handling the response
            StringBuilder sb = new StringBuilder();
            //     int HttpResult = conn.getResponseCode();
            is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();


            // TODO: what does "readIt()" do?
            String resp=readIt(is, 2).substring(0, 1);

            Log.d("hw4","readIT gets:"+resp);
            if (!resp.contains("0")) { //"0"
                return true;
            } else {
                Log.d("hw4","not logged  in and ...?");
                return false;
            }


            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {
            Log.d("vt", " and the exception is " + e);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return false;
    }
}
