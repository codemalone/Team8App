package tcss450.uw.edu.team8app;

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
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.team8app.model.Credentials;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;


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
        if(mListener != null) {
            View v = this.getView();
            boolean error = false;
            EditText email = v.findViewById(R.id.register_email_edit);
            if(email.getText().toString().chars().filter(ch -> ch == '@').count() != 1) {
                error = true;
                email.setError("Field must contain a valid email address.");
            } else if(email.getText().toString().length() == 0) {
                error = true;
                email.setError("Field must not be empty");
            }
            EditText first = v.findViewById(R.id.register_firstname_edit);
            if(first.getText().toString().length() == 0) {
                error = true;
                first.setError("Field must not be empty");
            }
            EditText last = v.findViewById(R.id.register_lastname_edit);
            if(last.getText().toString().length() == 0) {
                error = true;
                last.setError("Field must not be empty");
            }
            EditText username = v.findViewById(R.id.register_username_edit);
            if(username.getText().toString().length() == 0) {
                error = true;
                username.setError("Field must not be empty");
            }
            EditText password1 = v.findViewById(R.id.register_password1_edit);
            if(password1.getText().toString().length() == 0) {
                error = true;
                password1.setError("Field must not be empty");
            }
            EditText password2 = v.findViewById(R.id.register_password2_edit);
            if(password2.getText().toString().length() == 0) {
                error = true;
                password2.setError("Field must not be empty");
            } else if(!password1.getText().toString().equals(password2.getText().toString())) {
                error = true;
                password2.setError("Password must match");
            }
            if(!error) {
                Credentials.Builder builder = new Credentials.Builder(email.getText().toString(),
                        password1.getText().toString());
                builder.addFirstName(first.getText().toString());
                builder.addLastName(last.getText().toString());
                builder.addUsername(username.getText().toString());
                Credentials credentials = builder.build();
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
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
            if(success) {
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
    public interface OnFragmentInteractionListener extends WaitFragment.OnFragmentInteractionListener{
        void onRegisterSuccess(Credentials credentials);
    }
}
