package tcss450.uw.edu.team8app.settings;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Credentials mCredentials;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button button = view.findViewById(R.id.setting_username_button);
        //button.setOnClickListener(null); Add listeners here
        button = view.findViewById(R.id.setting_password_button);
        button.setOnClickListener(this::openPassword);
        button = view.findViewById(R.id.setting_theme_button);
        button.setOnClickListener(this::openTheme);
        return view;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        if(getArguments() != null) {
            mCredentials = (Credentials) getArguments().getSerializable(Credentials.CREDIT_TAG);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Opens the theme fragment.
     *
     * @param view The view.
     */
    private void openTheme(View view) {
        if(mListener != null) {
            mListener.clickedChangeTheme();
        }
    }

    private void openPassword(View view) {
        if(mListener != null) {
            mListener.clickedChangePassword();
        }
    }

    public interface OnFragmentInteractionListener {
        void clickedChangeTheme();
        void clickedChangePassword();
    }
}
