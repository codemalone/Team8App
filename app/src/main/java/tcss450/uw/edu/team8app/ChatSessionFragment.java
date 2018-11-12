package tcss450.uw.edu.team8app;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.team8app.utils.MyFirebaseMessagingService;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatSessionFragment extends Fragment {

    private static final String TAG = "CHAT_FRAG";
    private static final String CHAT_ID = "1";
    private TextView mMessageOutputTextView;
    private EditText mMessageInputEditText;
    private String mEmail;
    private String mSendUrl;
    private String mGetAllMsgUrl;

    private FirebaseMessageReciever mFirebaseMessageReciever;

    public ChatSessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (prefs.contains(getString(R.string.keys_prefs_email))) {
            mEmail = prefs.getString(getString(R.string.keys_prefs_email), "");
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

        mGetAllMsgUrl = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_get_all))
                .build()
                .toString();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFirebaseMessageReciever == null) {
            mFirebaseMessageReciever = new FirebaseMessageReciever();
        }
        IntentFilter iFilter = new IntentFilter(MyFirebaseMessagingService.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mFirebaseMessageReciever, iFilter);

        //request all messages
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("chatId", CHAT_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mGetAllMsgUrl, messageJson)
                .onPostExecute(this::endOfGetAllMsgTask)
                .onCancelled(error -> Log.e(TAG, error))
                .build().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFirebaseMessageReciever != null){
            getActivity().unregisterReceiver(mFirebaseMessageReciever);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootLayout = inflater.inflate(R.layout.fragment_chat_session, container, false);
        mMessageOutputTextView = rootLayout.findViewById(R.id.text_chat_message_display);
        mMessageInputEditText = rootLayout.findViewById(R.id.edit_chat_message_input);
        rootLayout.findViewById(R.id.button_chat_send).setOnClickListener(this::handleSendClick);
        return rootLayout;
    }

    private void handleSendClick(final View theButton) {
        String msg = mMessageInputEditText.getText().toString();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("email", mEmail);
            messageJson.put("message", msg);
            messageJson.put("chatId", CHAT_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(error -> Log.e(TAG, error))
                .build().execute();
    }
    private void endOfSendMsgTask(final String result) {
        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            Log.i("chat response: ", res.toString());
            if(res.has("success") && res.getBoolean("success")) {
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
        Log.i("called getAllPostExec", "blah");

        try {
            JSONObject res = new JSONObject(result);
            Log.v("getAll", res.toString());





        } catch (JSONException e) {
            e.printStackTrace();
        }




    };




    /**
     * A BroadcastReceiver setup to listen for messages sent from
     MyFirebaseMessagingService
     * that Android allows to run all the time.
     */
    private class FirebaseMessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("FCM Chat Frag", "start onRecieve");
            if(intent.hasExtra("DATA")) {
                String data = intent.getStringExtra("DATA");
                JSONObject jObj = null;
                try {
                    jObj = new JSONObject(data);
                    if(jObj.has("message") && jObj.has("sender")) {
                        Log.i("inside data", intent.toString());

                        String sender = jObj.getString("sender");
                        String msg = jObj.getString("message");
                        mMessageOutputTextView.append(sender + ":" + msg);
                        mMessageOutputTextView.append(System.lineSeparator());
                        mMessageOutputTextView.append(System.lineSeparator());
                        Log.i("FCM Chat Frag", sender + " " + msg);
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