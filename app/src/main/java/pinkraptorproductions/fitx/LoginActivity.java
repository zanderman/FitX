package pinkraptorproductions.fitx;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Calendar;

import pinkraptorproductions.fitx.classes.Session;
import pinkraptorproductions.fitx.interfaces.LoginSessionInterface;
import pinkraptorproductions.fitx.tasks.LoginTask;


public class LoginActivity extends Activity implements
        View.OnClickListener,
        LoginSessionInterface
{

    // XML elements
    private Button login;
    private Button exit;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageView logo;

    // String Variables
    private String username, password;

    // Counter and flag for easter egg.
    private int count = 0;
    private boolean flag = false;

    // Declare generic toast.
    private Toast genToast;

    // Variable to store the name of the previous text values used.
    private static final String KEY_TEXT_CONTENT = "textContent";

    // Animation object
    private Animation anim;

    // Login AsyncTask object.
    LoginTask loginTask;

    // Session object
    Session session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // New session object with parent activity context.
        session = new Session(getApplicationContext());

        // Link java objects to XML elements.
        login = (Button) findViewById(R.id.loginButton);
        exit = (Button) findViewById(R.id.exitButton);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        logo = (ImageView) findViewById(R.id.logoImageView);

        anim = AnimationUtils.loadAnimation(this.getApplicationContext(), R.anim.fade_in);
        login.startAnimation(anim);
        exit.startAnimation(anim);
        usernameEditText.startAnimation(anim);
        passwordEditText.startAnimation(anim);
        logo.startAnimation(anim);

        // Setup click listeners for buttons.
        login.setOnClickListener(this);
        exit.setOnClickListener(this);

        // Set up easter egg listener.
        logo.setOnClickListener(this);

        // Initialize generic toast.
        genToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    // Check user entries for errors.
    public Boolean validateEntry(String username, String password) {
        if (username.length() != 0 && password.length() != 0) return true;
        else return false;
    }

    // Callback for when login button is pressed.
    @Override
    public void onClick(View view) {

        // Determine which button was clicked.
        switch (view.getId()) {

            // Callback for "login" button.
            case R.id.loginButton:
                // Get user credentials after the button is clicked.
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();

                // Only run if fields are valid.
                if (validateEntry(username, password)) {

                    // Validate credentials on the network.
                    new LoginTask(this, session).execute(new String[]{AppActivity.BASE_URL, username, password});

                } else {
                    if (username.length() == 0) {
                        usernameEditText.setError("field empty!");
                    }
                    if (password.length() == 0) {
                        passwordEditText.setError("field empty!");
                    }
                }
                break;

            // Callback for "exit" button.
            case R.id.exitButton:
                // Show exit message.
                update("FitX exited.");

                // Exit the app.
                Intent result = new Intent();
                result.putExtra("login", "exit");
                setResult(RESULT_OK, result);
                finish();
                break;

            // Easter egg.
            case R.id.logoImageView:

                // if flag is true, reset the image.
                if (flag == true) {
                    logo.setBackgroundResource(R.drawable.logo);
                    flag = false;
                }

                // If flag is false, allow easter egg.
                else {
                    // Increment counter
                    count++;

                    // Determine number of presses.
                    switch (count) {
                        case 3:
                            update("Almost there...");
                            break;
                        case 6:
                            count = 0;
                            update("Yay!");
                            logo.setBackgroundResource(R.drawable.meme);
                            flag = true;
                            break;
                    }
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current content of the text fields.
        outState.putStringArray(KEY_TEXT_CONTENT, new String[] {usernameEditText.getText().toString(),passwordEditText.getText().toString()});
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Update the text fields with previously entered text.
        String[] myArray = savedInstanceState.getStringArray(KEY_TEXT_CONTENT);
        usernameEditText.setText(myArray[0]);
        usernameEditText.invalidate();
        passwordEditText.setText(myArray[1]);
        passwordEditText.invalidate();
    }

    // Update the generic toast message upon request.
    private void update(String message) {
        genToast.setText(message);
        genToast.show();
    }

    // Run actions based on passed login.
    @Override
    public void loginPass(String cookie) {

        // Push the cookie to shared preferences
        SharedPreferences.Editor editor = session.getPrefs().edit();
        editor.putString("sessionid", cookie);
        editor.putString("sessionUser", session.getUser());
        editor.commit();

        // Save the session data in a bundle.
        Bundle bundle = new Bundle();
        bundle.putString("cookie", session.getCookie());

        // Pass information to the parent activity.
        Intent result = new Intent();
        result.putExtra("login", "success");
        result.putExtra("sessionInfo", bundle);
        setResult(RESULT_OK, result);
        finish();
    }

    // Run actions based on failed login.
    @Override
    public void loginFailed(String message) {

        if (message.equals("no network connection"))
            Toast.makeText(this, "No network connection.", Toast.LENGTH_SHORT).show();
        if (message.equals("invalid credentials")) {
            usernameEditText.setError("invalid");
            passwordEditText.setError("invalid");
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
