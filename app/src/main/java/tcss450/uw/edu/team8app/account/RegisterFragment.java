package tcss450.uw.edu.team8app.account;

import android.content.Context;
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

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.utils.WaitFragment;
import tcss450.uw.edu.team8app.model.Credentials;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.ValidationUtils;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RegisterFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Credentials mCredientials;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        Button button = view.findViewById(R.id.register_register_button);
        button.setOnClickListener(this::attemptRegister);
        return view;
    }

    private void attemptRegister(View view) {
        if (mListener != null) {
            View v = this.getView();

            EditText emailEditText = v.findViewById(R.id.register_email_edit);
            EditText firstNameEditText = v.findViewById(R.id.register_firstname_edit);
            EditText lastNameEditText = v.findViewById(R.id.register_lastname_edit);
            EditText usernameEditText = v.findViewById(R.id.register_username_edit);
            EditText password1EditText = v.findViewById(R.id.register_password1_edit);
            EditText password2EditText = v.findViewById(R.id.register_password2_edit);

            String email = emailEditText.getText().toString().trim();
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password1 = password1EditText.getText().toString().trim();
            String password2 = password2EditText.getText().toString().trim();

            boolean error = false;

            if (TextUtils.isEmpty(email)) {
                error = true;
                emailEditText.setError("Field must not be empty");
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                error = true;
                emailEditText.setError("Field must contain a valid email address");
            }

            if (TextUtils.isEmpty(firstName)) {
                error = true;
                firstNameEditText.setError("Field must not be empty");
            }

            if (TextUtils.isEmpty(lastName)) {
                error = true;
                lastNameEditText.setError("Field must not be empty");
            }

            if (TextUtils.isEmpty(username)) {
                error = true;
                usernameEditText.setError("Field must not be empty");
            } else if (!ValidationUtils.USERNAME.matcher(username).matches()) {
                error = true;
                usernameEditText.setError("Field must consist of alphanumeric characters only");
            }

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
                Credentials.Builder builder = new Credentials.Builder(emailEditText.getText().toString(),
                        password1EditText.getText().toString())
                        .addFirstName(firstNameEditText.getText().toString())
                        .addLastName(lastNameEditText.getText().toString())
                        .addUsername(usernameEditText.getText().toString());
                Credentials credentials = builder.build();
                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.ep_scheme))
                        .encodedAuthority(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_account))
                        .appendPath(getString(R.string.ep_register))
                        .build();
                JSONObject msg = credentials.asJSONObject();
                mCredientials = credentials;

                new SendPostAsyncTask.Builder(uri.toString(), msg)
                        .onPreExecute(this::handleRegisterOnPre)
                        .onPostExecute(this::handleRegisterOnPost)
                        .onCancelled(this::handleErrorsInTask)
                        .build().execute();
            }
        }
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     *
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the website.
     */
    private void handleRegisterOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    private void handleRegisterOnPost(String result) {
        try {
            Log.d("JSON result", result);
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("success");
            mListener.onWaitFragmentInteractionHide();
            if (success) {
                mListener.onRegisterSuccess(mCredientials);
            } else {
                ((TextView) getView().findViewById(R.id.register_email_edit)).setError("Registration Failure");
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.register_email_edit)).setError("Registration Failure");
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
        void onRegisterSuccess(Credentials credentials);
    }
}
