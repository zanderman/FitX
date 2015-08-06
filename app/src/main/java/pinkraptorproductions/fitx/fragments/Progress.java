package pinkraptorproductions.fitx.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pinkraptorproductions.fitx.AppActivity;
import pinkraptorproductions.fitx.R;
import pinkraptorproductions.fitx.interfaces.ProgressInteractionListener;


public class Progress extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private static final String KEY_STORE_MILES = "miles";
    private static final String KEY_STORE_MINUTES = "minutes";
    private static final String KEY_STORE_CUPS = "cups";
    private static final String KEY_STORE_STEPS = "steps";
    private static final String KEY_STORE_DATE = "date";
    private static final String KEY_STORE_ID = "_id";

    private boolean FLAG_ADD;
    private boolean FLAG_DELETE;



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ProgressInteractionListener talkToActivity;

    // Link XML elements to java objects.
    private Button add, save, delete;
    private EditText milesEntry, minutesEntry, cupsEntry, stepsEntry;
    private TextView dateView;
    private ListView entries;

    private ProgressEntryAdapter adapter;
    private int counter = 0;

    // Animation object.
    private Animation anim;

    // Shared Preferences
    SharedPreferences prefs;

    Bundle downloadedEntries;


    // TODO: Rename and change types and number of parameters
    public static Progress newInstance(String param1, String param2, Bundle downloadedEntries) {
        Progress fragment = new Progress();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putBundle(ARG_PARAM3, downloadedEntries);
        fragment.setArguments(args);
        return fragment;
    }

    public Progress() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            downloadedEntries = getArguments().getBundle(ARG_PARAM3);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        float[] store_miles = new float[adapter.getCount()];
        float[] store_cups = new float[adapter.getCount()];
        int[] store_steps = new int[adapter.getCount()];
        int[] store_minutes = new int[adapter.getCount()];
        String[] store_id = new String[adapter.getCount()];
        String[] store_date = new String[adapter.getCount()];

        // Go through each list entry and populate the save arrays.
        for (int i = 0; i < adapter.getCount() ; i++) {
            store_miles[i] = adapter.getItem(i).miles;
            store_cups[i] = adapter.getItem(i).cups;
            store_steps[i] = adapter.getItem(i).steps;
            store_minutes[i] = adapter.getItem(i).minutes;
            store_id[i] = adapter.getItem(i).id;
            store_date[i] = adapter.getItem(i).date;
        }

        // Put the save arrays into the outState bundle.
        outState.putFloatArray(KEY_STORE_MILES, store_miles);
        outState.putFloatArray(KEY_STORE_CUPS, store_cups);
        outState.putIntArray(KEY_STORE_STEPS, store_steps);
        outState.putIntArray(KEY_STORE_MINUTES, store_minutes);
        outState.putStringArray(KEY_STORE_DATE, store_date);
        outState.putStringArray(KEY_STORE_ID, store_id);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // a temporary placeholder for the hardcoded data
        ArrayList<ProgressEntry> temp = new ArrayList<ProgressEntry>();

        // If there is a saved state, recreate stored objects.
        if (savedInstanceState != null) {
            for (int i = 0; i < savedInstanceState.getStringArray(KEY_STORE_ID).length; i++) {
                temp.add(new ProgressEntry(
                        savedInstanceState.getIntArray(KEY_STORE_STEPS)[i],
                        savedInstanceState.getFloatArray(KEY_STORE_MILES)[i],
                        savedInstanceState.getIntArray(KEY_STORE_MINUTES)[i],
                        savedInstanceState.getFloatArray(KEY_STORE_CUPS)[i],
                        savedInstanceState.getStringArray(KEY_STORE_ID)[i],
                        savedInstanceState.getStringArray(KEY_STORE_DATE)[i]
                ));
            }
        }
        if (downloadedEntries != null) {
            for (int i = 0; i < downloadedEntries.getStringArray(KEY_STORE_ID).length; i++) {
                temp.add(new ProgressEntry(
                        downloadedEntries.getIntArray(KEY_STORE_STEPS)[i],
                        downloadedEntries.getFloatArray(KEY_STORE_MILES)[i],
                        downloadedEntries.getIntArray(KEY_STORE_MINUTES)[i],
                        downloadedEntries.getFloatArray(KEY_STORE_CUPS)[i],
                        downloadedEntries.getStringArray(KEY_STORE_ID)[i],
                        downloadedEntries.getStringArray(KEY_STORE_DATE)[i]
                ));
            }
            // Nullify the bundle so that they can't be re-added.
            downloadedEntries = null;
        }

        //linking  java side with the xml side for the ListView
        entries = (ListView) view.findViewById(R.id.progressList);
        entries.setDivider(null);
        entries.setDividerHeight(0);

        //feeding data into the array adapter
        adapter = new ProgressEntryAdapter(getActivity().getApplicationContext(), temp);

        entries.setAdapter(adapter);
        // Inflate the layout for this fragment

        // Initialize add button and set onClickListener.
        add = (Button) view.findViewById(R.id.addEntry);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Add a new entry
                addEntry(0, 0, 0, 0, "tempid", "__/__/__ @ __:__:__");
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            talkToActivity = (ProgressInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ProgressInteractionListener");
        }
    }

    // Add entry to entry adapter.
    public void addEntry(int steps, float miles, int minutes, float cups, String id, String date) {
        FLAG_ADD = true;
        adapter.add(new ProgressEntry(steps, miles, minutes, cups, id, date));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        talkToActivity = null;
    }


    // Custom adapter that allows you to handle interactions with the listview views
    private class ProgressEntryAdapter extends ArrayAdapter<ProgressEntry> implements
            View.OnClickListener {

        private final Context context;
        //values that will be displaed
        private final ArrayList<ProgressEntry> values;


        // constructor that takes the values
        public ProgressEntryAdapter(Context context, ArrayList<ProgressEntry> values) {
            super(context, R.layout.progress_entry_layout, values);
            this.context = context;
            this.values = values;
        }

        public boolean isInt(String number) {
            try {
                Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        public boolean isFloat(String number) {
            try {
                Float.parseFloat(number);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }



        // draws each visible view
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Initialize a layout inflater.
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Inflate the current row.
            final View rowView = inflater.inflate(R.layout.progress_entry_layout,
                    parent, false);

            // Determine if adding a new entry.
            if (position == (entries.getCount()-1) && FLAG_ADD == true) {

                // Assign sliding up animation.
                anim = AnimationUtils.loadAnimation(this.getContext(), R.anim.slide_up);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        FLAG_ADD = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                rowView.startAnimation(anim);
            }

            // Default animation for no additional entries.
            if (FLAG_ADD == false && FLAG_DELETE == false) {

                // Assign fading in animation.
                anim = AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_in);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                rowView.startAnimation(anim);
            }

            // Reset delete flag on last inflate of delete sequence.
            if (position == (entries.getCount()-1) && FLAG_DELETE == true) {
                FLAG_DELETE = false;
            }
            
            // Initialize editTexts
            milesEntry = (EditText) rowView.findViewById(R.id.milesEntry);
            cupsEntry = (EditText) rowView.findViewById(R.id.cupsEntry);
            stepsEntry = (EditText) rowView.findViewById(R.id.stepsEntry);
            minutesEntry = (EditText) rowView.findViewById(R.id.minutesEntry);
            dateView = (TextView) rowView.findViewById(R.id.dateView);

            // Set text to the editTexts.
            milesEntry.setText(String.valueOf(values.get(position).miles));
            cupsEntry.setText(String.valueOf(values.get(position).cups));
            stepsEntry.setText(String.valueOf(values.get(position).steps));
            minutesEntry.setText(String.valueOf(values.get(position).minutes));
            dateView.setText("Save date: " + String.valueOf(values.get(position).date));

            // Set default hint
            milesEntry.setHint("0.0");
            cupsEntry.setHint("0.0");
            stepsEntry.setHint("0");
            minutesEntry.setHint("0");

            // Initialize Buttons.
            save = (Button) rowView.findViewById(R.id.saveEntry);
            delete = (Button) rowView.findViewById(R.id.deleteEntry);

            // The code below sets tags to your buttons so that you can detect which one was pressed
            save.setTag(new String[]{((Integer) position).toString(), "save"});
            delete.setTag(new String[]{((Integer) position).toString(), "delete"});

            // Set onClickListeners for buttons.
            save.setOnClickListener(this);
            delete.setOnClickListener(this);

            // Return the new view.
            return rowView;
        }


        // this will happen whenever 'save' or 'delete' is pressed
        @Override
        public void onClick(View view) {

            // Declare java objects
            String type, id, date, index;
            final int indexInAdapter, firstVisible;
            int stepsFromTheEditText, minutesFromTheEditText;
            float milesFromTheEditText, cupsFromTheEditText;
            Calendar now;
            View row;
            boolean dataIsValid = true;

            // Type of button that was clicked.
            type = ((String[]) view.getTag())[1];
            index = ((String[]) view.getTag())[0];

            // this is the index of the entry to be saved
            indexInAdapter = Integer.parseInt(index);

            // index of the first visible listview view
            firstVisible = entries.getFirstVisiblePosition();

            // Get list item row index.
            row = entries.getChildAt(indexInAdapter - firstVisible);

            // Initialize the ID from the adapter.
            id = adapter.getItem(indexInAdapter).id;

            // Temporary EditText objects.
            EditText temp_miles, temp_cups, temp_steps, temp_min;
            temp_miles = ((EditText) row.findViewById(R.id.milesEntry));
            temp_cups = ((EditText) row.findViewById(R.id.cupsEntry));
            temp_steps = ((EditText) row.findViewById(R.id.stepsEntry));
            temp_min = ((EditText) row.findViewById(R.id.minutesEntry));


            // Verify each element is of the correct type.
            if (isFloat(temp_miles.getText().toString()))
                milesFromTheEditText = Float.parseFloat(temp_miles.getText().toString());
            else {
                temp_miles.setError("Invalid format!");
                dataIsValid = false;
                milesFromTheEditText = 0;

            }
            if (isFloat(temp_cups.getText().toString()))
                cupsFromTheEditText = Float.parseFloat(temp_cups.getText().toString());
            else {
                temp_cups.setError("Invalid format!");
                dataIsValid = false;
                cupsFromTheEditText = 0;

            }
            if (isInt(temp_min.getText().toString()))
                minutesFromTheEditText = Integer.parseInt(temp_min.getText().toString());
            else {
                temp_min.setError("Invalid format!");
                dataIsValid = false;
                minutesFromTheEditText = 0;
            }
            if (isInt(temp_steps.getText().toString()))
                stepsFromTheEditText = Integer.parseInt(temp_steps.getText().toString());
            else {
                temp_steps.setError("Invalid format!");
                dataIsValid = false;
                stepsFromTheEditText = 0;
            }

            // Create a format for the date.
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy @ HH:mm:ss");

            // Set the date field for the entry.
            now = Calendar.getInstance();
            date = sdf.format(now.getTime());

            // Create a temporary entry.
            ProgressEntry newEntry = new ProgressEntry(stepsFromTheEditText, milesFromTheEditText,
                    minutesFromTheEditText, cupsFromTheEditText, id, date);

            // if 'delete' was pressed
            if (type == "delete") {

                // this will show you which button from which row was pressed
                Toast.makeText(context, type + " pressed @" + indexInAdapter, Toast.LENGTH_SHORT).show();

                FLAG_DELETE = true;

                //COMMUNICATE TO ACTIVITY WHICH PROGRESS ENTRY WAS DELETED
                talkToActivity.deleteEntry(newEntry);

                // Animate the deletion of the selected entry.
                anim = AnimationUtils.loadAnimation(row.getContext(), R.anim.slide_right);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // "this" refers to the ArayAdapter. You can remove things from ArrayAdpter by saying .remove(<object>)
                        adapter.remove(adapter.getItem(indexInAdapter));

                        // This is necessary to make GUI glitchless
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                row.startAnimation(anim);
            }

            // Only save if the data is valid.
            if (type == "save" && dataIsValid) {

                // this will show you which button from which row was pressed
                Toast.makeText(context, type + " pressed @" + indexInAdapter, Toast.LENGTH_SHORT).show();

                // Set the date in the dateView.
                ((TextView) row.findViewById(R.id.dateView)).setText("Save date: " + date);

                //updating values in the arrayAdapter
                this.getItem(indexInAdapter).miles = milesFromTheEditText;
                this.getItem(indexInAdapter).cups = cupsFromTheEditText;
                this.getItem(indexInAdapter).steps = stepsFromTheEditText;
                this.getItem(indexInAdapter).minutes = minutesFromTheEditText;
                this.getItem(indexInAdapter).date = date;
                this.getItem(indexInAdapter).id = id;

                // COMMUNICATE TO ACTIVITY WHICH PROGRESS ENTRY WAS UPDATED
                talkToActivity.saveEntry(this.getItem(indexInAdapter));
            }
        }
    }


    // Class to represent data in the progress list.
    public static class ProgressEntry {

        public int steps;
        public float miles;
        public int minutes;
        public float cups;
        public String id;
        public String date;

        public ProgressEntry(int steps, float miles, int minutes, float cups, String id, String date) {

            this.minutes = minutes;
            this.cups = cups;
            this.miles = miles;
            this.steps = steps;
            this.id = id;
            this.date = date;
        }
    }
}
