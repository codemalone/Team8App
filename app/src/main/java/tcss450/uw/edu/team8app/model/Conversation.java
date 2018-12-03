package tcss450.uw.edu.team8app.model;

import java.util.List;

/**
 * The object that stores the conversation information.
 *
 * @author Jim Phan
 */
public class Conversation {

    /**
     * Tag used for conversation related activities.
     */
    public static final String TAG = "conversation";

    /**
     * The id of the chat.
     */
    private String mChatID;

    /**
     * List of users.
     */
    private List<String> mUsers;

    /**
     * The last message of the conversation.
     */
    private String mLastMessage;

    public Conversation(String chatId, List<String> users, String lastMessage) {
        mChatID = chatId;
        mUsers = users;
        mLastMessage = lastMessage;
    }

    /**
     * Return the chat id.
     *
     * @return The chat id.
     */
    public String getChatID() {
        return mChatID;
    }

    /**
     * Return a list of users.
     *
     * @return the list of users.
     */
    public List<String> getUsers() {
        return mUsers;
    }

    public String parsedUsers(int characters) {
        String users = "";
        for(String user : mUsers) {
            users += user + ",";
        }
        char[] parse = users.toCharArray();
        String parsedString = "";
        for(int i = 0; i < characters && i < parse.length; i++) {
            parsedString += parse[i];
        }
        if(characters < parse.length) {
            parsedString += "...";
        }
        return parsedString;
    }

    /**
     * Return the last message.
     *
     * @return The last message.
     */
    public String getLastMessage() {
        return mLastMessage;
    }
}
