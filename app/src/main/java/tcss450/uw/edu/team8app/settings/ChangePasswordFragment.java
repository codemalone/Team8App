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
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Credentials;
import tcss450.uw.edu.team8app.utils.ValidationUtils;
import tcss450.uw.edu.team8app.utils.WaitFragment;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChangePasswordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ChangePasswordFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mConfirmPassword;
    private Credentials mCredentials;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        mOldPassword = view.findViewById(R.id.change_password_old);
        mNewPassword = view.findViewById(R.id.change_password_new);
        mConfirmPassword = view.findViewById(R.id.change_password_match);
        Button button = view.findViewById(R.id.change_password_submit);
        button.setOnClickListener(this::submitChangePassword);
        return view;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        if (getArguments() != null) {
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

    private void submitChangePassword(View view) {
        if (mListener != null) {
            String oldPassword = mOldPassword.getText().toString();
            String newPassword = mNewPassword.getText().toString();
            String confirmPassword = mConfirmPassword.getText().toString();

            boolean error = false;

            if (TextUtils.isEmpty(oldPassword)) {
                error = true;
                mOldPassword.setError("Field cannot be empty");
            }

            if (TextUtils.isEmpty(newPassword)) {
                error = true;
                mNewPassword.setError("Field cannot be empty");
            } else if (!ValidationUtils.PASSWORD.matcher(newPassword).matches()) {
                error = true;
                mNewPassword.setError("Field must consist of alphanumeric, period, hyphen, "
                        + "underscore, and apostrophe characters only");
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                error = true;
                mConfirmPassword.setError("Field cannot be empty");
            } else if (!confirmPassword.equals(newPassword)) {
                error = true;
                mNewPassword.setError("Passwords do not match");
            }

            if (!error) {
                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.ep_scheme))
                        .encodedAuthority(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_account))
                        .appendPath(getString(R.string.ep_password))
                        .appendPath(getString(R.string.ep_change))
                        .build();

                JSONObject msgObject = new JSONObject();
                try {
                    msgObject.put("email", mCredentials.getEmail());
                    msgObject.put("oldPassword", mOldPassword.getText().toString());
                    msgObject.put("newPassword", mNewPassword.getText().toString());
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

    private void handleSubmitOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    private void handleSubmitOnPost(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                mListener.changePasswordSuccess(mNewPassword.getText().toString());
            } else {
                mOldPassword.setError("Password was incorrect");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            mListener.onWaitFragmentInteractionHide();
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        mListener.onWaitFragmentInteractionHide();
    }

    public interface OnFragmentInteractionListener {
        void onWaitFragmentInteractionShow();

        void onWaitFragmentInteractionHide();

        void changePasswordSuccess(String newPassword);
    }
}
