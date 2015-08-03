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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by lndsharkfury on 8/2/15.
 */
public class Session {

    private SharedPreferences prefs;
    private Context context;
    private String url, cookie, user;

    // Constructor with context
    public Session(Context context) {
        super();

        // Initialize parameters
        this.context = context;
        this.prefs = null;
        this.url = null;
        this.cookie = null;
    }

    // Constructor with url, cookie
    public Session(Context context, String url, String cookie) {
        super();

        // Initialize parameters
        this.context = context;
        this.prefs = null;
        this.url = url;
        this.cookie = cookie;
    }

    // Return shared preferences object.
    public SharedPreferences getPrefs() { return this.prefs; }

    // Return url.
    public String getUrl() { return this.url; }

    // Return cookie.
    public String getCookie() { return this.cookie; }

    // Return user.
    public String getUser() { return this.user; }


    //perform login
    public String login(String myurl, String username, String password) throws IOException, JSONException {

        InputStream is = null;
        final String COOKIES_HEADER = "Set-Cookie";

        // Initialize session parameters.
        this.url = myurl;
        this.cookie = "empty cookie";
        this.user = username;

        // Try network call
        try {
            HttpURLConnection conn = (HttpURLConnection) ((new URL(myurl).openConnection()));
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
            Log.d("hw4", " response from the server is" + conn.getResponseCode());
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
        return this.cookie;
    }

    //checks if the session is valid
    public Boolean isUserLoggedIn(String user) throws IOException, JSONException {

        this.prefs = this.context.getSharedPreferences("usersession", context.MODE_PRIVATE);
        this.cookie = this.prefs.getString("sessionid", "");
        Log.d("hw4","cookie in the onResume is "+this.cookie);
        InputStream is = null;

        try {
            HttpURLConnection conn = (HttpURLConnection) ((new URL(this.url + "?" + user).openConnection()));
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Cookie",this.cookie );
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.connect();


            // handling the response
            StringBuilder sb = new StringBuilder();
            //     int HttpResult = conn.getResponseCode();
            is = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();


            // TODO: what does "readIt()" do?
            String resp=readIt(is, 2).substring(0, 1);

            Log.d("hw4","readIT gets:"+resp);
            if (!resp.contains("0")) {
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
