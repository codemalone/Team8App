package tcss450.uw.edu.team8app.account.password;


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
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.ValidationUtils;
import tcss450.uw.edu.team8app.utils.WaitFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordResetFragment extends Fragment {

    private OnResetPasswordListener mListener;
    private String mEmail;
    private String mCode;

    public PasswordResetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        Bundle args = getArguments();
        if (args != null) {
            mEmail = args.getString("email");
            mCode = args.getString("code");
        }

        Button button = view.findViewById(R.id.resetpassword_submit_button);
        button.setOnClickListener(this::submit);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResetPasswordListener) {
            mListener = (OnResetPasswordListener) context;
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

    private void submit(View view) {
        EditText password1EditText = getActivity().findViewById(R.id.resetpassword_password1_edit);
        EditText password2EditText = getActivity().findViewById(R.id.resetpassword_password2_edit);

        String password1 = password1EditText.getText().toString();
        String password2 = password2EditText.getText().toString();

        boolean error = false;

        if (TextUtils.isEmpty(password1)) {
            error = true;
            password1EditText.setError("Field must not be empty");
        } else if (!ValidationUtils.PASSWORD.matcher(password1).matches()) {
            error = true;
            password1EditText.setError("Field must consist of alphanumeric, period, hyphen, "
                    + "underscore, and apostrophe characters only");
        }

        if (TextUtils.isEmpty(password2)) {
            error = true;
            password2EditText.setError("Field must not be empty");
        } else if (!ValidationUtils.PASSWORD.matcher(password2).matches()) {
            error = true;
            password1EditText.setError("Field must consist of alphanumeric, period, hyphen, "
                    + "underscore, and apostrophe characters only");
        } else if (!password2.equals(password1)) {
            error = true;
            password2EditText.setError("Password must match");
        }

        if (!error) {
            mListener.onWaitFragmentInteractionShow();
            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme(getString(R.string.ep_scheme))
                    .encodedAuthority(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_account))
                    .appendPath(getString(R.string.ep_password))
                    .appendPath(getString(R.string.ep_reset))
                    .build();
            JSONObject msg = new JSONObject();

            try {
                msg.put("email", mEmail);
                msg.put("code", mCode);
                msg.put("newPassword", password1EditText.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleSubmitOnPre)
                    .onPostExecute(this::handleSubmitOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     *
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        mListener.onWaitFragmentInteractionHide();
    }

    /**
     * Handle the setup of the UI before the HTTP call to the website.
     */
    private void handleSubmitOnPre() {
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     *
     * @param result the JSON formatted String response from the web service
     */
    private void handleSubmitOnPost(String result) {
        try {
            Log.d("JSON result", result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                mListener.onPasswordResetSuccess();
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                ((EditText) getView().findViewById(R.id.resetpassword_password1_edit))
                        .setError("Code not found");
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((EditText) getView().findViewById(R.id.resetpassword_password1_edit))
                    .setError("Unable to send code check request");
        }
    }

    public interface OnResetPasswordListener extends WaitFragment.OnFragmentInteractionListener {
        void onPasswordResetSuccess();
    }

}
