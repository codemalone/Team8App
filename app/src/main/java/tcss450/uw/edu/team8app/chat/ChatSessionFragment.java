package tcss450.uw.edu.team8app.chat;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Conversation;
import tcss450.uw.edu.team8app.model.Message;
import tcss450.uw.edu.team8app.utils.MyFirebaseMessagingService;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.WaitFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatSessionFragment extends Fragment {

    public static final String ARG_MESSAGE_LIST = "messages";
    public static final String TAG = "CHAT_FRAG";
    private EditText mMessageInputEditText;
    private RecyclerView mMessageDisplay;
    private RecyclerView.LayoutManager mMessageLayoutManager;
    private ChatMessageListAdapter mMessageListAdapter;
    private List<Message> mMessages;
    private String mChatId;
    private String mUsername;
    private JSONArray mPossible;
    private String mAddedUsername;
    private ChatSessionListener mListener;

    private String mEmail;
    private String mSendUrl;

    private FirebaseMessageReciever mFirebaseMessageReciever;

    public ChatSessionFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Chat");
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        if (prefs.contains(getString(R.string.keys_prefs_email))) {
            mEmail = prefs.getString(getString(R.string.keys_prefs_email), "");
            mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        } else {
            throw new IllegalStateException("No EMAIL in prefs!");
        }

        //We will use this url every time the user hits send. Let's only build it once, ya?
        mSendUrl = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_send))
                .build()
                .toString();

        generatePossible();
        openAddDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ChatSessionListener) {
            mListener = (ChatSessionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void generatePossible() {
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats))
                .appendPath(getString(R.string.ep_users))
                .appendPath(getString(R.string.ep_possible));
        Uri uri = uriBuilder.build();
        JSONObject msg = new JSONObject();

        try {
            msg.put("token", FirebaseInstanceId.getInstance().getToken());
            msg.put("chatId", mChatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                //.onPreExecute(this::handleSearchOnPre)
                .onPostExecute(this::handleGetPossibleOnPost)
                //.onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleGetPossibleOnPost(String result) {
        try {
            JSONObject json = new JSONObject(result);
            mPossible = json.getJSONArray("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openAddDialog() {
        if (mPossible != null) {
            Dialog dialog;

            final ArrayList<String> items = new ArrayList<String>();

            for (int i = 0; i < mPossible.length(); i++) {
                try {
                    items.add(mPossible.getJSONObject(i).getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String[] asArray = items.toArray(new String[items.size()]);
            final ArrayList itemsSelected = new ArrayList();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Add user(s):");
            builder.setMultiChoiceItems(asArray, null,
                    (dialog1, selectedItemId, isSelected) -> {
                        if (isSelected) {
                            itemsSelected.add(selectedItemId);
                        } else if (itemsSelected.contains(selectedItemId)) {
                            itemsSelected.remove(Integer.valueOf(selectedItemId));
                        }
                    })
                    .setPositiveButton("Add", (dialog12, id) -> {
                        for (int i = 0; i < itemsSelected.size(); i++) {
                            addUserToChat((Integer) itemsSelected.get(i));
                        }

                        AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());

                        if (itemsSelected.size() == 1) {
                            builderInner.setMessage("1 user has been added to the chat.");
                        } else {
                            builderInner.setMessage(itemsSelected.size() + " users have been added to the chat.");
                        }

                        builderInner.setTitle("Success");
                        builderInner.setPositiveButton("Ok", (dialog121, which) -> dialog121.dismiss());
                        builderInner.show();
                        generatePossible();
                    })
                    .setNegativeButton("Cancel", (dialog13, id) -> {});
            dialog = builder.create();
            dialog.show();
        }
    }

    private void addUserToChat(int index) {
        try {
            mAddedUsername = mPossible.getJSONObject(index).getString("username");
            String theirEmail = mPossible.getJSONObject(index).getString("email");
            Uri.Builder uriBuilder = new Uri.Builder()
                    .scheme(getString(R.string.ep_scheme))
                    .encodedAuthority(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_chats))
                    .appendPath(getString(R.string.ep_add));
            Uri uri = uriBuilder.build();
            JSONObject msg = new JSONObject();

            try {
                msg.put("token", FirebaseInstanceId.getInstance().getToken());
                msg.put("theirEmail", theirEmail);
                msg.put("chatId", mChatId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .build().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMessages = new ArrayList<Message>(
                    Arrays.asList((Message[]) getArguments().getSerializable(ARG_MESSAGE_LIST)));
            mChatId = (String) getArguments().getSerializable(TAG);
        } else {
            mMessages = new ArrayList<Message>();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mFirebaseMessageReciever == null) {
            mFirebaseMessageReciever = new FirebaseMessageReciever();
        }

        IntentFilter iFilter = new IntentFilter(MyFirebaseMessagingService.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mFirebaseMessageReciever, iFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mFirebaseMessageReciever != null) {
            getActivity().unregisterReceiver(mFirebaseMessageReciever);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootLayout = inflater.inflate(R.layout.fragment_chat_session, container, false);

        mMessageDisplay = (RecyclerView) rootLayout.findViewById(R.id.recycler_view_chat_session);
        mMessageLayoutManager = new LinearLayoutManager(this.getActivity());
        ((LinearLayoutManager) mMessageLayoutManager).setReverseLayout(true);
        mMessageDisplay.setLayoutManager(mMessageLayoutManager);

        mMessageListAdapter = new ChatMessageListAdapter(mMessages);
        mMessageDisplay.setAdapter(mMessageListAdapter);

        mMessageInputEditText = rootLayout.findViewById(R.id.edit_chat_message_input);
        rootLayout.findViewById(R.id.button_chat_send).setOnClickListener(this::handleSendClick);
        return rootLayout;
    }

    private void handleSendClick(final View theButton) {
        String msg = mMessageInputEditText.getText().toString();
        JSONObject messageJson = new JSONObject();

        try {
            //messageJson.put("email", mEmail);
            messageJson.put("message", msg);
            messageJson.put("chatId", mChatId);
            messageJson.put("token", FirebaseInstanceId.getInstance().getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(error -> Log.e(TAG, error))
                .build().execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manage_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manage_add_user:
                openAddDialog();
                return true;
            case R.id.manage_leave:
                leaveConversation();
                //returnConversation();
                //loadFragment(new ConversationFragment());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface ChatSessionListener {
        void returnToChatList();
    }


    private void leaveConversation() {
        Uri uri = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats))
                .appendPath(getString(R.string.ep_users))
                .appendPath(getString(R.string.ep_remove))
                .build();
        JSONObject msgObject = new JSONObject();

        try {
            msgObject.put("token", FirebaseInstanceId.getInstance().getToken());
            msgObject.put("chatId", mChatId);
        } catch (JSONException e) {
            Log.e("ERROR!", e.getMessage());
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msgObject)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleLeaveConversationPost)
                .onCancelled(error -> Log.e("ERROR!", error))
                .build()
                .execute();
    }

    public void handleLeaveConversationPost(String result) {
        onWaitFragmentInteractionHide();
        mListener.returnToChatList();
    }

    private void handleMessageListGetOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);

            if (root.has("success") && root.getBoolean("success")) {
                if (root.has("data")) {
                    JSONArray jsonArray = root.getJSONArray("data");
                    List<Conversation> conversations = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String chatid = object.getString("chatid");
                        JSONArray users = object.getJSONArray("users");
                        List<String> userString = new ArrayList<>();

                        for (int j = 0; j < users.length(); j++) {
                            if (!users.getString(j).equalsIgnoreCase(mUsername)
                                    && !users.getString(j).equalsIgnoreCase(mEmail)) {
                                userString.add(users.getString(j));
                            }
                        }

                        String lastMessage = object.getString("recentMessage");
                        Conversation conversation = new Conversation(chatid, userString, lastMessage);
                        conversations.add(conversation);
                    }

                    Bundle bundle = new Bundle();
                    Conversation[] conversationsAsArray = new Conversation[conversations.size()];
                    conversationsAsArray = conversations.toArray(conversationsAsArray);
                    bundle.putSerializable(ConversationFragment.TAG, conversationsAsArray);
                    Fragment frag = new ConversationFragment();
                    frag.setArguments(bundle);
                    onWaitFragmentInteractionHide();
                    Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Message");
                    loadFragment(frag);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            onWaitFragmentInteractionHide();
        }
    }

    public void onWaitFragmentInteractionShow() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_home_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    public void onWaitFragmentInteractionHide() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(getActivity().getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();
    }

    private void loadFragment(Fragment frag) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_home_container, frag, "messages")
                .addToBackStack(null)
                .commit();
    }

    private void endOfSendMsgTask(final String result) {
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            Log.i("chat response: ", res.toString());

            if (res.has("success") && res.getBoolean("success")) {
                //The web service got our message. Time to clear out the input EditText
                mMessageInputEditText.setText("");
                //its up to you to decide if you want to send the message to the output here
                //or wait for the message to come back from the web service.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void endOfGetAllMsgTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            Log.v("getAll", res.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A BroadcastReceiver setup to listen for messages sent from
     * MyFirebaseMessagingService
     * that Android allows to run all the time.
     */
    private class FirebaseMessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("DATA")) {
                String data = intent.getStringExtra("DATA");
                Log.i("msg received", data);
                JSONObject jObj = null;

                try {
                    jObj = new JSONObject(data);

                    if (jObj.has("message") && jObj.has("sender")) {

                        String sender = jObj.getString("sender");
                        String body = jObj.getString("message");
                        String timestamp = new Date().toString();

                        Message msg = new Message.Builder(sender, body, timestamp).build();

                        mMessages.add(0, msg);
                        mMessageListAdapter.notifyItemInserted(0);
                        mMessageDisplay.scrollToPosition(0);
                        Log.i("notified", msg.toString());
                    }
                } catch (JSONException e) {
                    Log.e("JSON PARSE", e.toString());
                }
            } else {
                Log.i("no data", intent.toString());
            }
        }
    }

}
