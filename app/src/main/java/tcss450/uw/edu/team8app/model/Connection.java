package tcss450.uw.edu.team8app.model;

public class Connection {

    private final String mFirstName;
    private final String mLastName;
    private final String mUsername;
    private final String mEmail;
    private final int mVerified;
    private final int mSender;

    public Connection(String firstName, String lastName, String username, String email, int verified, int sender) {
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mUsername = username;
        this.mEmail = email;
        this.mVerified = verified;
        this.mSender = sender;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public int getVerified() {
        return mVerified;
    }

    public int getSender() {
        return mSender;
    }

}
