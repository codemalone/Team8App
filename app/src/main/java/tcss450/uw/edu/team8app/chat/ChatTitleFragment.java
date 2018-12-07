package tcss450.uw.edu.team8app.chat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tcss450.uw.edu.team8app.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatTitleFragment extends Fragment {

    public ChatTitleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_title, container, false);
    }

}
