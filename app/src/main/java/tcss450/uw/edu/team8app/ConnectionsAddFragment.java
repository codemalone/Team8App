package tcss450.uw.edu.team8app;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

    public ConnectionsAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Add a connection");
        View view = inflater.inflate(R.layout.fragment_connections_add, container, false);
        Button button = view.findViewById(R.id.button_connections_add_return);
        button.setOnClickListener(this::returnToConnections);
        ImageButton imageButton = view.findViewById(R.id.imageButton_connections_add_search);
        imageButton.setOnClickListener(this::addConnection);
        mSearchInput = view.findViewById(R.id.editText_connections_add_search_bar);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_connections_add_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayList<Connection> testArray = new ArrayList<Connection>();
        for (int i =0; i < 10; i++) {
            int test = (int) Math.round(Math.random());
            if (test == 1) {
                testArray.add(new Connection("firstname" + i, "lastname" + i, "username" + i, "email" + i, (int) Math.round(Math.random()), (int)(Math.random() * 3)));
            } else {
                testArray.add(new Connection("firstname" + i, "lastname" + i, "username" + i, "email" + i, (int) Math.round(Math.random()), (int)(Math.random() * 3)));
            }
        }

        mAdapter = new ConnectionsRecyclerViewAdapter(testArray);
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    private void returnToConnections(final View button) {
        FragmentTransaction transaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_home_container, new ConnectionsFragment())
                .addToBackStack(null);
        transaction.commit();
    }

    private void addConnection(final View button) {
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connections))
                .appendPath(getString(R.string.ep_connections_add));
        Uri uri = uriBuilder.build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("token", "123456");
            msg.put("email", mSearchInput.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleAddOnPre)
                .onPostExecute(this::handleAddOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        onWaitFragmentInteractionHide();
    }

    private void handleAddOnPre() {
        onWaitFragmentInteractionShow();
    }

    private void handleAddOnPost(String result) {

    }

    @Override
    public void onWaitFragmentInteractionShow() {

    }

    @Override
    public void onWaitFragmentInteractionHide() {

    }

}
