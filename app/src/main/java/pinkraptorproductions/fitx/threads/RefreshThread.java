package pinkraptorproductions.fitx.threads;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import pinkraptorproductions.fitx.tasks.RefreshTask;

/**
 * Created by lndsharkfury on 7/24/15.
 */
public class RefreshThread extends Thread {

    private Handler handler;

    @Override
    public void run() {

        try {
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        } catch (Throwable t) {
            Log.e("thread", "RefreshThread halted due to an error.", t);
        }
    }

    // Put the task on the thread queue.
    public synchronized void enqueTask(final RefreshTask rt) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    rt.run();
                } catch (Exception e) {
                    Log.e("RefreshThread", "RefreshTask execution error.");
                }
            }
        });
    }
}
