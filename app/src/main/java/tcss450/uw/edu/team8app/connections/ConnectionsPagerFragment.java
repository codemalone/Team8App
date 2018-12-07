package tcss450.uw.edu.team8app.connections;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.connections.ConnectionsFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.team8app.model.Connection;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.WaitFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionsPagerFragment extends Fragment implements WaitFragment.OnFragmentInteractionListener, OnConnectionInteractionListener {
    private ArrayList<Connection> mConnections;
    ConnectionsRecyclerViewAdapter mAdapter;
    private OnConnectionInteractionListener mListener;
    private OnListFragmentInteractionListener mListListener;
    protected FragmentActivity mActivity;
    View view;

    public static ConnectionsPagerFragment init(int position) {
        ConnectionsPagerFragment frag = new ConnectionsPagerFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connections_pager, container,
                false);

        getConnectionsList();
        return view;
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        onWaitFragmentInteractionHide();
    }

    private void getConnectionsList() {
        mConnections = new ArrayList<>();
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections))
                .appendPath(getString(R.string.ep_get));
        if (getArguments() != null) {
            if (getArguments().getInt("position") == 0) {
                uriBuilder.appendPath(getString(R.string.ep_pending));
            } else if (getArguments().getInt("position") == 1) {
                uriBuilder.appendPath(getString(R.string.ep_active));
            } else if (getArguments().getInt("position") == 2) {
                uriBuilder.appendPath(getString(R.string.ep_received));
            }
            Uri uri = uriBuilder.build();
            JSONObject msg = new JSONObject();
            try {
                msg.put("token", FirebaseInstanceId.getInstance().getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleGetOnPre)
                    .onPostExecute(this::handleGetOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }
    }

    private void handleGetOnPre() {
        if (mActivity != null) {
            onWaitFragmentInteractionShow();
        }
    }

    private void handleGetOnPost(String result) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_connections_pager);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        try {
            JSONObject json = new JSONObject(result);
            JSONArray data = json.getJSONArray("data");
            int myID = json.getInt("id");
            //Log.e("TEST", "" + data);
            for (int i = 0; i < data.length(); i++) {
                JSONObject currentMember = data.getJSONObject(i);
                int verified = 0;
                int sender = 0;
                if (!currentMember.isNull("verified")) {
                    verified = currentMember.getInt("verified");
                    if (currentMember.getInt("memberid_a") == myID) {
                        sender = 1;
                    } else {
                        sender = 2;
                    }
                }
                mConnections.add(new Connection(currentMember.getString("firstname"), currentMember.getString("lastname"), currentMember.getString("username"), currentMember.getString("email"), verified, sender));
            }
            mAdapter = new ConnectionsRecyclerViewAdapter(mConnections, getContext(), mListener, mListListener);
            recyclerView.setAdapter(mAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mActivity != null) {
            onWaitFragmentInteractionHide();
        }
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        Objects.requireNonNull(mActivity).getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_home_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        Objects.requireNonNull(mActivity).getSupportFragmentManager()
                .beginTransaction()
                .remove(Objects.requireNonNull(mActivity.getSupportFragmentManager().findFragmentByTag("WAIT")))
                .commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mListener = (OnConnectionInteractionListener) this;
        if (context instanceof OnListFragmentInteractionListener) {
            mListListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mListListener = null;
    }

    @Override
    public void OnConnectionInteraction(Connection item) {
        getConnectionsList();
        mAdapter.notifyDataSetChanged();
    }
}
