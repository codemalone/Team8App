package tcss450.uw.edu.team8app.model;

/**
 * This class stores information about recent notifications.
 *
 * @author Jim Phan akari0@uw.edu
 */
public class RecentNotification {

    public static final String TAG = "notification";
    private String mMessage;
    private String mNotificationType;
    private String mUsername;

    /**
     * Return the message of the recent notification.
     *
     * @return The message of the notification.
     */
    public String getMessage() {
        return mMessage;
    }

    public String getNotificationType() {
        return mNotificationType;
    }

    public String getUsername() {
        return mUsername;
    }
}
