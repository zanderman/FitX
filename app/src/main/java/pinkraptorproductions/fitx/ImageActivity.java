package pinkraptorproductions.fitx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class ImageActivity extends Activity implements View.OnClickListener {

    // Declare java objects.
    private Button b1, b2, b3, b4, b5, logout;
    private ImageView image;

    // Variable to store the name of the previous image used.
    private static final String KEY_IMAGE_TAG = "imageTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // Link java objects with XML element.
        b1 = (Button) findViewById(R.id.pictureButton1);
        b2 = (Button) findViewById(R.id.pictureButton2);
        b3 = (Button) findViewById(R.id.pictureButton3);
        b4 = (Button) findViewById(R.id.pictureButton4);
        b5 = (Button) findViewById(R.id.pictureButton5);
        logout = (Button) findViewById(R.id.logoutButton);
        image = (ImageView) findViewById(R.id.dynamicImageView);

        // Set listeners for button clicks.
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        b5.setOnClickListener(this);
        logout.setOnClickListener(this);

    }

    // Universal click method for all buttons.
    @Override
    public void onClick(View v) {

        // Determine which button was clicked
        switch (v.getId()) {

            case R.id.pictureButton1:
                // Set the image inside the picture view.
                updateImage(image, "a", R.drawable.a);
                break;
            case R.id.pictureButton2:
                // Set the image inside the picture view.
                updateImage(image, "b", R.drawable.b);
                break;
            case R.id.pictureButton3:
                // Set the image inside the picture view.
                updateImage(image, "c", R.drawable.c);
                break;
            case R.id.pictureButton4:
                // Set the image inside the picture view.
                updateImage(image, "d", R.drawable.d);
                break;
            case R.id.pictureButton5:
                // Set the image inside the picture view.
                updateImage(image, "e", R.drawable.e);
                break;
            case R.id.logoutButton:
                // Push logout flag to main activity.
                Intent data = new Intent();
                data.putExtra("flag", "logout");
                setResult(RESULT_OK, data);

                // Finish activity
                finish();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the tag of the current image.
        outState.putString(KEY_IMAGE_TAG, image.getTag().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Read values from the "savedInstanceState"-object and put them in your textview

        // Restore the previous image after rotating based on saved tag.
        switch (savedInstanceState.getString(KEY_IMAGE_TAG)) {
            case "a":
                updateImage(image, "a", R.drawable.a);
                break;
            case "b":
                updateImage(image, "b", R.drawable.b);
                break;
            case "c":
                updateImage(image, "c", R.drawable.c);
                break;
            case "d":
                updateImage(image, "d", R.drawable.d);
                break;
            case "e":
                updateImage(image, "e", R.drawable.e);
                break;
        }
    }

    // Method for updating
    private void updateImage(ImageView imageView, String tag, int resId) {
        imageView.setImageResource(resId);
        imageView.setTag(tag);
        imageView.invalidate();
    }
}
