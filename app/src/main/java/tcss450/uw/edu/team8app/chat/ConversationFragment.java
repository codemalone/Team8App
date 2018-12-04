package tcss450.uw.edu.team8app.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Conversation;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ConversationFragment extends Fragment {

    public static final String TAG = "conversation list";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<Conversation> mConversationList;
    private JSONArray mPossible;
    private int mChatId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConversationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null && getArguments().getSerializable(TAG) != null) {
            mConversationList = new ArrayList<>(Arrays.asList((Conversation[]) getArguments().getSerializable(TAG)));
        } else {
            mConversationList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyConversationRecyclerViewAdapter(mConversationList, mListener));
        }
        generatePossible();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messages, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chat_create:
                //loadFragment(new ConnectionsAddFragment());
                openAddDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
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
                .appendPath(getString(R.string.ep_connections))
                .appendPath(getString(R.string.ep_get))
                .appendPath(getString(R.string.ep_active));
        Uri uri = uriBuilder.build();
        JSONObject msg = new JSONObject();
        try {
            msg.put("token", FirebaseInstanceId.getInstance().getToken());
            //msg.put("chatId", mChatId);
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
            //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            //builderSingle.setIcon(R.drawable.ic_launcher);
            //builder.setTitle("Add a user:");
            Dialog dialog;

            final ArrayList<String> items = new ArrayList<String>();
            for (int i = 0; i < mPossible.length(); i++) {
                try {
                    items.add(mPossible.getJSONObject(i).getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //String[] asArray = (String[]) items.toArray();
            String[] asArray = items.toArray(new String[items.size()]);
            final ArrayList itemsSelected = new ArrayList();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Add user(s):");
            builder.setMultiChoiceItems(asArray, null,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedItemId,
                                            boolean isSelected) {
                            if (isSelected) {
                                itemsSelected.add(selectedItemId);
                            } else if (itemsSelected.contains(selectedItemId)) {
                                itemsSelected.remove(Integer.valueOf(selectedItemId));
                            }
                        }
                    })
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            for (int i = 0; i < itemsSelected.size(); i++) {
                                if (i == 0) {
                                    createChatWithUser((Integer) itemsSelected.get(i));
                                } else {
                                    addUserToChat((Integer) itemsSelected.get(i));
                                }
                            }
                            // chats/add (token, theirEmail, chatId)
                            AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                            //Log.e()
                            builderInner.setMessage("Chat created with " + itemsSelected.size() + " user(s).");
                            builderInner.setTitle("Success");
                            builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builderInner.show();
                            generatePossible();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            dialog = builder.create();
            //((AlertDialog) dialog).setView(getLayoutInflater().inflate(R.layout.scrollview_add_dialog, null));
            dialog.show();
        }
    }

    private void createChatWithUser(Integer index) {
        try {
           // mAddedUsername = mPossible.getJSONObject(index).getString("username");
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    //.onPreExecute(this::handleSearchOnPre)
                    .onPostExecute(this::handleCreateChatOnPost)
                    //.onCancelled(this::handleErrorsInTask)
                    .build().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleCreateChatOnPost(String result) {
        try {
            JSONObject json = new JSONObject(result);
            Log.e("TEST", result);
            Log.e("TEST", "" + json.getJSONObject("data").getInt("chatId"));
            mChatId = json.getJSONObject("data").getInt("chatId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addUserToChat(int index) {
        try {
            //mAddedUsername = mPossible.getJSONObject(index).getString("username");
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
                    //.onPreExecute(this::handleSearchOnPre)
                    //.onPostExecute(this::handleAddUserOnPost)
                    //.onCancelled(this::handleErrorsInTask)
                    .build().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onConversationInteraction(Conversation item);
    }
}
