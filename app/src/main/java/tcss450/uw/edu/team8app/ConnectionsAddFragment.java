package tcss450.uw.edu.team8app;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import tcss450.uw.edu.team8app.model.Connection;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionsAddFragment extends Fragment implements WaitFragment.OnFragmentInteractionListener {

    TextView mSearchInput;
    ConnectionsRecyclerViewAdapter mAdapter;
    View view;

    public ConnectionsAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Add a connection");
        view = inflater.inflate(R.layout.fragment_connections_add, container, false);
        Button button = view.findViewById(R.id.button_connections_add_return);
        button.setOnClickListener(this::returnToConnections);
        ImageButton imageButton = view.findViewById(R.id.imageButton_connections_add_search);
        imageButton.setOnClickListener(this::searchUsers);
        mSearchInput = view.findViewById(R.id.editText_connections_add_search_bar);

        return view;
    }

    private void returnToConnections(final View button) {
        FragmentTransaction transaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_home_container, new ConnectionsFragment())
                .addToBackStack(null);
        transaction.commit();
    }

    private void searchUsers(final View button) {
        if (!mSearchInput.getText().toString().isEmpty()) {
            Uri.Builder uriBuilder = new Uri.Builder()
                    .scheme(getString(R.string.ep_scheme))
                    .encodedAuthority(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_connections))
                    .appendPath(getString(R.string.ep_connections_search));
            Uri uri = uriBuilder.build();
            JSONObject msg = new JSONObject();
            try {
                msg.put("token", FirebaseInstanceId.getInstance().getToken());
                msg.put("string", mSearchInput.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleSearchOnPre)
                    .onPostExecute(this::handleSearchOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        onWaitFragmentInteractionHide();
    }

    private void handleSearchOnPre() {
        onWaitFragmentInteractionShow();
    }

    private void handleSearchOnPost(String result) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_connections_add_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        try {
            JSONObject json = new JSONObject(result);
            JSONArray data = json.getJSONArray("data");
            int myID = json.getInt("id");
            Log.e("TEST", "" + data);
            ArrayList<Connection> connectionsList = new ArrayList<Connection>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject currentMember = data.getJSONObject(i);
                int verified = 0;
                int sender = 0;
                if (!currentMember.isNull("verified")){
                    verified = currentMember.getInt("verified");
                    if (currentMember.getInt("memberid_a") == myID) {
                        sender = 1;
                    } else if (currentMember.getInt("memberid_b") == myID) {
                        sender = 2;
                    }
                }
                connectionsList.add(new Connection(currentMember.getString("firstname"), currentMember.getString("lastname"), currentMember.getString("username"), currentMember.getString("email"), verified, sender));
            }
            mAdapter = new ConnectionsRecyclerViewAdapter(connectionsList);
            recyclerView.setAdapter(mAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        for (int i =0; i < 10; i++) {
//            int test = (int) Math.round(Math.random());
//            if (test == 1) {
//                testArray.add(new Connection("firstname" + i, "lastname" + i, "username" + i, "email" + i, (int) Math.round(Math.random()), (int)(Math.random() * 3)));
//            } else {
//                testArray.add(new Connection("firstname" + i, "lastname" + i, "username" + i, "email" + i, (int) Math.round(Math.random()), (int)(Math.random() * 3)));
//            }
//        }
    }

    @Override
    public void onWaitFragmentInteractionShow() {

    }

    @Override
    public void onWaitFragmentInteractionHide() {

    }

}
