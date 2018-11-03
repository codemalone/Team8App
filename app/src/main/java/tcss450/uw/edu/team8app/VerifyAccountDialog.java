package tcss450.uw.edu.team8app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class VerifyAccountDialog extends AppCompatDialogFragment {
    public static final String DIALOG_TAG = "verify dialog";

    private String warningDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(warningDialog == null) {
            warningDialog = "";
        }
        return new AlertDialog.Builder(getActivity()).setTitle("Email Verification")
                .setMessage(warningDialog)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
    }

    public void setWarningDialog(String message) {
        warningDialog = message;
    }
}
