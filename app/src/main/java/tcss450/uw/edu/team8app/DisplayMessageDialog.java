package tcss450.uw.edu.team8app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class DisplayMessageDialog extends AppCompatDialogFragment {
    public static final String TAG = "display message";

    private String mMessage;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(mMessage == null) {
            mMessage = "";
        }
        if(getArguments() != null) {
            mMessage = (String) getArguments().getSerializable(TAG);
        }
        return new AlertDialog.Builder(getActivity()).setTitle("Information")
                .setMessage(mMessage)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
    }
}
