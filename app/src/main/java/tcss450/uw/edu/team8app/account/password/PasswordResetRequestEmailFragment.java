package tcss450.uw.edu.team8app.account.password;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.account.LoginFragment;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.WaitFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordResetRequestEmailFragment extends Fragment {

    private OnInitiateResetListener mListener;

    public PasswordResetRequestEmailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_password_reset_request_email, container, false);

        Button button = view.findViewById(R.id.requestemail_submit_button);
        button.setOnClickListener(this::submit);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginFragment.OnFragmentInteractionListener) {
            mListener = (OnInitiateResetListener) context;
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
        mListener.onWaitFragmentInteractionShow();
        EditText emailEditText = getActivity().findViewById(R.id.requestemail_email_edit);
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_account))
                .appendPath(getString(R.string.ep_recover))
                .appendPath(getString(R.string.ep_initiate))
                .build();
        JSONObject msg = new JSONObject();

        try {
            msg.put("email", emailEditText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleSubmitOnPre)
                .onPostExecute(this::handleSubmitOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
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
                mListener.onEmailSubmitSuccess(((EditText) getView().findViewById(R.id.requestemail_email_edit))
                        .getText().toString());
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                ((EditText) getView().findViewById(R.id.requestemail_email_edit))
                        .setError("Email not found");
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((EditText) getView().findViewById(R.id.requestemail_email_edit))
                    .setError("Unable to send password reset request");
        }
    }

    public interface OnInitiateResetListener extends WaitFragment.OnFragmentInteractionListener {
        void onEmailSubmitSuccess(String email);
    }

}
