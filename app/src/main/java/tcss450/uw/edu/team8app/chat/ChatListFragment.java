package tcss450.uw.edu.team8app.chat;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.connections.ConnectionsAddFragment;
import tcss450.uw.edu.team8app.connections.OnConnectionInteractionListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private OnConnectionInteractionListener mListener;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Messages");
        //onCreateOptionsMenu();
        //setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.e("AGSAGSAGSAGS", "AGSAGSAGSGASGS");
        inflater.inflate(R.menu.messages, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chat_create:
                //loadFragment(new ConnectionsAddFragment());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
