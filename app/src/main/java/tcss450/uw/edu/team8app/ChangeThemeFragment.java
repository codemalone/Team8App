package tcss450.uw.edu.team8app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tcss450.uw.edu.team8app.utils.Themes;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChangeThemeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ChangeThemeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public ChangeThemeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_theme, container, false);
        Button button = view.findViewById(R.id.theme_default_button);
        final String buttonText = button.getText().toString();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selectTheme(Themes.getTheme(buttonText));
            }
        });
        button = view.findViewById(R.id.theme_dark_button);
        final String buttonText2 = button.getText().toString();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selectTheme(Themes.getTheme(buttonText2));
            }
        });
        button = view.findViewById(R.id.theme_fruit_button);
        final String buttonText3 = button.getText().toString();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selectTheme(Themes.getTheme(buttonText3));
            }
        });
        return view;
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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void clickedChangeTheme();
        void selectTheme(Themes theme);
    }
}
