package pinkraptorproductions.fitx.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import pinkraptorproductions.fitx.R;
import pinkraptorproductions.fitx.interfaces.MessagesInteractionListener;


public class Messages extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG_RETAIN_SPINNER = "retain_spinner";
    private static final String TAG_LOAD_DATA = "retain_data";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MessagesInteractionListener talkToActivity;

    // GUI objects.
    private TextView text;
    private Button button;
    private ProgressBar spinner;

    // Flags
    private boolean isRefreshing;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    // Method for creating a new instance.
    public static Messages newInstance(String param1, String param2) {
        Messages fragment = new Messages();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Messages() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Initialize the shared preferences object.
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        // Initialize the editor.
        editor = sp.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the view.
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Declare textview.
        text = (TextView) view.findViewById(R.id.randomTextView);
        button = (Button) view.findViewById(R.id.randomButton);
        spinner = (ProgressBar) view.findViewById(R.id.progressBar);

        // Check if was refhreshing before last reset.
        if (sp != null && sp.getBoolean(TAG_RETAIN_SPINNER, false)) {
            spinner.setVisibility(View.VISIBLE);

            // Reset the retain spinner flag.
            editor.putBoolean(TAG_RETAIN_SPINNER, false);
            editor.commit();
        }
        else
            // Initially hide the progress bar.
            spinner.setVisibility(View.INVISIBLE);

        // Set the text as the last number used.
        if (sp != null && sp.getString(TAG_LOAD_DATA, null) != null && !sp.getString(TAG_LOAD_DATA, "").equals(""))
//            text.setText(sp.getString(TAG_LOAD_DATA, ""));
            updateText(Integer.parseInt(sp.getString(TAG_LOAD_DATA, "")));
        else
            // Set initial hint...
            text.setHint("refresh me...");

        // Set the onClickListener for the button.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Refresh is happening. Update the flag.
                isRefreshing = true;

                // Show the progress bar.
                updateSpinner(isRefreshing);

                // Tell the main activty to start the retained fragment.
                talkToActivity.startRefreshThread();
                Log.d("fragment", "Messages told AppActivity to start the retain fragment.");
            }
        });

        // Return the view.
        return view;
    }

    // Set the text of the textview.
    public void updateText(int value) {

        // Change the refreshing flag.
        isRefreshing = false;

        // Hide the progress bar.
        updateSpinner(isRefreshing);

        // Update the textView.
        text.setText(Integer.toString(value));
    }

    // Simple method for updating the spinner progress bar.
    public void updateSpinner(boolean flag) {
        if (flag) spinner.setVisibility(View.VISIBLE);
        else spinner.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the data to SharedPreferences.
        editor.putString(TAG_LOAD_DATA, text.getText().toString());
        editor.putBoolean(TAG_RETAIN_SPINNER, isRefreshing);
        editor.commit();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            talkToActivity = (MessagesInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MessagesInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        talkToActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
