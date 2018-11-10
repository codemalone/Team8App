package tcss450.uw.edu.team8app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.team8app.model.Credentials;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.ValidationUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LoginFragment extends Fragment {
    public static final String FRAGMENT_TAG = "login fragment";

    private OnFragmentInteractionListener mListener;
    private Credentials mCredentials;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //retrieve the stored credentials from SharedPrefs
        if (prefs.contains(getString(R.string.keys_prefs_email)) &&
                prefs.contains(getString(R.string.keys_prefs_password))) {
            final String email = prefs.getString(getString(R.string.keys_prefs_email), "");
            final String password = prefs.getString(getString(R.string.keys_prefs_password), "");
            //Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.login_email_edit);
            emailEdit.setText(email);
            EditText passwordEdit = getActivity().findViewById(R.id.login_password_edit);
            passwordEdit.setText(password);

            if (!email.isEmpty() && !password.isEmpty()) {
                boolean useEmail = true;

                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    useEmail = true;
                } else if (ValidationUtils.USERNAME.matcher(email).matches()) {
                    useEmail = false;
                }

                doLogin(email, password, useEmail);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button button = view.findViewById(R.id.login_login_button);
        button.setOnClickListener(this::attemptLogin);
        button = view.findViewById(R.id.login_register_button);
        button.setOnClickListener(this::registerButton);
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

    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_email), credentials.getEmail()).apply();
        prefs.edit().putString(getString(R.string.keys_prefs_password), credentials.getPassword()).apply();
    }

    private void registerButton(View view) {
        if(mListener != null) {
            mListener.onRegisterClicked();
        }
    }

    private void attemptLogin(final View button) {
        View v = getView();

        EditText emailEditText = v.findViewById(R.id.login_email_edit);
        EditText passwordEditText = v.findViewById(R.id.login_password_edit);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean error = false;
        boolean useEmail = true;

        if (TextUtils.isEmpty(email)) {
            error = true;
            emailEditText.setError("Field must not be empty");
        } else {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                useEmail = true;
            } else if (ValidationUtils.USERNAME.matcher(email).matches()) {
                useEmail = false;
            } else {
                error = true;
                emailEditText.setError("Field must contain a valid email address or username");
            }
        }

        if (TextUtils.isEmpty(password)) {
            error = true;
            passwordEditText.setError("Field must not be empty");
        } else if (!ValidationUtils.PASSWORD.matcher(password).matches()) {
            error = true;
            passwordEditText.setError("Field must consist of alphanumeric, period, hyphen, "
                    + "underscore, and apostrophe characters only");
        }

        if(!error) {
            doLogin(email, password, useEmail);
        }
    }

    private void doLogin(String email, String password, boolean useEmail) {
        Credentials.Builder credentialsBuilder = new Credentials.Builder(useEmail ? email : null, password);
        //build the web service URL
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(getString(R.string.ep_scheme))
                .encodedAuthority(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_account))
                .appendPath(getString(R.string.ep_login));

        if (useEmail) {
            uriBuilder.appendPath(getString(R.string.ep_email));
        } else {
            credentialsBuilder.addUsername(email);
            uriBuilder.appendPath(getString(R.string.ep_username));
        }

        Credentials credentials = credentialsBuilder.build();
        Uri uri = uriBuilder.build();
        JSONObject msg = credentials.asJSONObject();
        mCredentials = credentials;

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleLoginOnPre)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        mListener.onWaitFragmentInteractionHide();
    }

    /**
     * Handle the setup of the UI before the HTTP call to the website.
     */
    private void handleLoginOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            Log.d("JSON result",result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                if (resultsJSON.has("user")) {
                    JSONObject userJSON = resultsJSON.getJSONObject("user");
                    mCredentials = new Credentials.Builder(userJSON.getString("email"), mCredentials.getPassword())
                            .addFirstName(userJSON.getString("first"))
                            .addLastName(userJSON.getString("last"))
                            .addUsername(userJSON.getString("username"))
                            .build();
                }

                //Login was successful. Inform the Activity so it can do its thing.
                saveCredentials(mCredentials);
                mListener.onLoginSuccess(mCredentials);
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                ((TextView) getView().findViewById(R.id.login_email_edit))
                        .setError("Login Unsuccessful");

                //get error message
                JSONObject error = resultsJSON.getJSONObject("message");

                if (error.getInt("code") == 207) {
                    mListener.tellUserToVerify(mCredentials);
                }

            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.login_email_edit))
                    .setError("Login Unsuccessful");
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
    public interface OnFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener {
        void onRegisterClicked();
        void onLoginSuccess(Credentials credentials);
        void tellUserToVerify(Credentials credentials);
    }
}
