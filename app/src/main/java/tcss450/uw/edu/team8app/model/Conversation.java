package tcss450.uw.edu.team8app.model;

import java.io.Serializable;
import java.util.List;

/**
 * The object that stores the conversation information.
 *
 * @author Jim Phan
 */
public class Conversation implements Serializable {

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
        int index = 0;

        // if only one other then add their name
        if (mUsers.size() == 1) {
            users += mUsers.get(0);
        } else if (mUsers.size() > 0) {
            while (users.length() < 30 && index < mUsers.size()) {
                users += mUsers.get(index++) + ", ";
            }

            // remove last comma and add "& you"

            users = users.substring(0, users.length() - 2);
        }


        // if end of list add "& you" otherwise add count
        if (mUsers.size() > 1) {
            if (index == mUsers.size()) {
                users += " & you";
            } else {
                int remaining = mUsers.size() - index;
                users += " & " + remaining + " more";
            }
        }
//
//        char[] parse = users.toCharArray();
//        String parsedString = "";
//        for(int i = 0; i < characters && i < parse.length; i++) {
//            parsedString += parse[i];
//        }
//        if(characters < parse.length) {
//            parsedString += "...";
//        }
        return users;
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
