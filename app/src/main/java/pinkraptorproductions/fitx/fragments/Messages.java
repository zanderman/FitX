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
    private static final String TAG_RETAIN = "retain";

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


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Messages.
     */
    // TODO: Rename and change types and number of parameters
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
        if (sp != null && sp.getBoolean("flag", false) == true) {
            spinner.setVisibility(View.VISIBLE);
            editor.putBoolean("flag", false);
            editor.commit();
        }
        else
            // Initially hide the progress bar.
            spinner.setVisibility(View.INVISIBLE);

        // Set the text as the last number used.
        if (sp != null && !sp.getString("data", null).equals(null) && !sp.getString("data", "").equals(""))
            text.setText(sp.getString("data", ""));
        else
            // Set initial hint...
            text.setHint("refresh me...");

        // Set the onClickListener for the button.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isRefreshing = true;

                // Show the progress bar.
                spinner.setVisibility(View.VISIBLE);

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

        isRefreshing = false;

        // Hide the progress bar.
        spinner.setVisibility(View.INVISIBLE);

        text.setText(Integer.toString(value));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the data to SharedPreferences.
        editor.putString("data", text.getText().toString());
        editor.putBoolean("flag", isRefreshing);
//        editor.putBoolean("restore", true);
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
//        editor.putBoolean("restore", false);
//        editor.commit();
        super.onDestroy();
    }

}
