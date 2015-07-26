package pinkraptorproductions.fitx.interfaces;

import pinkraptorproductions.fitx.fragments.Progress;

/**
 * Created by lndsharkfury on 7/17/15.
 */
public interface ProgressInteractionListener {

    public void saveEntry(Progress.ProgressEntry entry);
    public void deleteEntry(Progress.ProgressEntry entry);
}
