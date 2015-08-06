package pinkraptorproductions.fitx.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.os.Handler;

import pinkraptorproductions.fitx.AppActivity;
import pinkraptorproductions.fitx.R;
import pinkraptorproductions.fitx.interfaces.RetainedFragmentInteractionListener;
import pinkraptorproductions.fitx.tasks.RefreshTask;
import pinkraptorproductions.fitx.threads.RefreshThread;


public class RetainedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RetainedFragmentInteractionListener listener;

    private RefreshThread refreshThread;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RetainedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RetainedFragment newInstance(String param1, String param2) {
        RetainedFragment fragment = new RetainedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RetainedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Create the new thread and start it so that it's ready to go.
        refreshThread = new RefreshThread();
        refreshThread.start();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (RetainedFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RetainedFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // Handler object with message handling.
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.getData().getBoolean("result"))
                Log.d("hw4", "data bundle length: " + Integer.toString(msg.getData().getBundle("data").size()));
                listener.newEntries(
                        msg.getData().getBundle("data")
                );
        }
    };

    //initiates refresh task from inside the retained fragment
    public void initiateProgressLoad(String cookie, String user) {
        refreshThread.enqueTask(new RefreshTask(
                cookie,
                user,
                AppActivity.BASE_URL,
                handler
        ));
        Log.d("hw4", "enqueued the task");
        //updates messages
        //to-do, next homework
    }
}
