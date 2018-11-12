package tcss450.uw.edu.team8app.model;

import java.io.Serializable;

/**
 * Class to encapsulate a message. Required fields are email, message, timestamp.
 *
 * Optional fields include username.
 *
 *
 * @author Charles Bryan
 * @author Jared Malone
 * @version 14 September 2018
 */
public class Message implements Serializable {

    private final String mEmail;
    private final String mUsername;
    private final String mMessage;
    private final String mTimestamp;

    /**
     * Helper class for building a message.
     *
     * @author Charles Bryan
     * @author Jared Malone
     */
    public static class Builder {
        private final String mEmail;
        private final String mMessage;
        private final String mTimestamp;
        private  String mUsername = "";


        /**
         * Constructs a new Builder.
         *
         * @param email the email of the author
         * @param message the message body
         * @param timestamp the timestamp of the message
         */
        public Builder(String email, String message, String timestamp) {
            this.mEmail = email;
            this.mMessage = message;
            this.mTimestamp = timestamp;
        }

        /**
         * Add an optional author username (display name)
         * @param username optional
         * @return the Builder of this BlogPost
         */
        public Builder addUsername(final String username) {
            mUsername = username;
            return this;
        }

        public Message build() {
            return new Message(this);
        }

    }

    private Message(final Builder builder) {
        this.mEmail = builder.mEmail;
        this.mUsername = builder.mUsername;
        this.mMessage = builder.mMessage;
        this.mTimestamp = builder.mTimestamp;
    }

    public String getEmail() { return mEmail; }

    public String getUsername() { return mUsername; }

    public String getMessage() { return mMessage; }

    public String getmTimestamp() { return mTimestamp; }

}
