package pinkraptorproductions.fitx.tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Random;

/**
 * Created by lndsharkfury on 7/25/15.
 */
public class RefreshTask implements Runnable {

    private String type;
    private Handler handler;

    // Task constructor
    public RefreshTask(String type, Handler handler) {
        this.type = type;
        this.handler = handler;
    }


    @Override
    public void run() {

        // Generate a random number.
        int rand = new Random().nextInt(999);

        // Store random number inside a bundle.
        Bundle bundle = new Bundle();
        bundle.putInt(this.type, rand);

        // Sleep for 5 seconds.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Generate new message object and populate with bundle.
        Message message = new Message();
        message.setData(bundle);

        // Send the message with bundle stored inside it.
        this.handler.sendMessage(message);
        Log.d("RefreshTask", "sent handler message");
    }
}
