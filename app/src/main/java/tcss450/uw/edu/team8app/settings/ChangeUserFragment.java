package tcss450.uw.edu.team8app.settings;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Credentials;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChangeUserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ChangeUserFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private TextView mUsernameTextView;
    private Credentials mCredentials;

    public ChangeUserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_user, container, false);
        mUsernameTextView = view.findViewById(R.id.change_username_edit);
        Button button = view.findViewById(R.id.change_username_button);
        button.setOnClickListener(this::submitChangeUsername);
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
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        if (getArguments() != null) {
            mCredentials = (Credentials) getArguments().getSerializable(Credentials.CREDIT_TAG);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void submitChangeUsername(View view) {
        if (mListener != null) {
            String newUsername = mUsernameTextView.getText().toString();
            boolean error = false;
            if (TextUtils.isEmpty(newUsername)) {
                error = true;
                mUsernameTextView.setError("Field cannot be empty");
            }
            if (!error) {
                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.ep_scheme))
                        .encodedAuthority(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_account))
                        .appendPath(getString(R.string.ep_username))
                        .appendPath(getString(R.string.ep_change))
                        .build();
                JSONObject msgObject = new JSONObject();
                try {
                    msgObject.put("token", FirebaseInstanceId.getInstance().getToken());
                    msgObject.put("newUsername", newUsername);
                } catch (JSONException e) {
                    Log.e("ERROR!", e.getMessage());
                    e.printStackTrace();
                }
                new SendPostAsyncTask.Builder(uri.toString(), msgObject)
                        .onPreExecute(this::handleSubmitOnPre)
                        .onPostExecute(this::handleSubmitOnPost)
                        .onCancelled(this::handleErrorsInTask)
                        .build().execute();
            }
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        mListener.onWaitFragmentInteractionHide();
    }

    private void handleSubmitOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    private void handleSubmitOnPost(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            System.out.println(result);
            if (success) {
                mListener.onSuccessChangeUsername(mUsernameTextView.getText().toString());
            } else {
                mUsernameTextView.setError(getString(R.string.change_username_error));
            }
        } catch (JSONException e) {
            Log.e("ERROR!", e.getMessage());
            e.printStackTrace();
            mListener.onWaitFragmentInteractionHide();
        }
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
        void onWaitFragmentInteractionShow();

        void onWaitFragmentInteractionHide();

        void onSuccessChangeUsername(String newUsername);
    }
}
