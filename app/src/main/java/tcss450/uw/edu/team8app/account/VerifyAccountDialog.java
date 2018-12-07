package tcss450.uw.edu.team8app.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.utils.GetAsyncTask;

public class VerifyAccountDialog extends AppCompatDialogFragment {
    public static final String DIALOG_TAG = "verify dialog";

    private String warningDialog;
    private String email;
    private String username;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (warningDialog == null) {
            warningDialog = "";
        }

        return new AlertDialog.Builder(getActivity()).setTitle("Email Not Verified")
                .setMessage(warningDialog)
                .setNeutralButton("OK", (dialog, which) -> {})
                .setPositiveButton("RESEND EMAIL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri.Builder uri = new Uri.Builder()
                                .scheme(getString(R.string.ep_scheme))
                                .encodedAuthority(getString(R.string.ep_base_url))
                                .appendPath(getString(R.string.ep_account))
                                .appendPath(getString(R.string.ep_verification))
                                .appendPath(getString(R.string.ep_send))
                                .appendQueryParameter("response", "json");

                        if (email == null) {
                            uri.appendQueryParameter(getString(R.string.ep_username),
                                    username);
                        } else {
                            uri.appendQueryParameter(getString(R.string.ep_email), email);
                        }

                        new GetAsyncTask.Builder(uri.build().toString())
                                .onPreExecute(this::handleVerificationSendOnPre)
                                .onPostExecute(this::handleVerificationSendOnPost)
                                .onCancelled(this::handleErrorsInTask)
                                .build().execute();
                    }

                    private void handleVerificationSendOnPre() {
                    }

                    private void handleVerificationSendOnPost(String result) {
                    }

                    private void handleErrorsInTask(String result) {
                    }
                })
                .create();
    }

    public void setWarningDialog(String message) {
        warningDialog = message;
    }

    public void setEmail(String theEmail) {
        email = theEmail;
    }

    public void setUsername(String theUsername) {
        username = theUsername;
    }
}
