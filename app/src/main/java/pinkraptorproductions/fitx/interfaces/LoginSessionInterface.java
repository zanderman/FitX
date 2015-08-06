package pinkraptorproductions.fitx.interfaces;

/**
 * Created by lndsharkfury on 8/5/15.
 */
public interface LoginSessionInterface {
    public void loginPass(String cookie);
    public void loginFailed(String message);
}
