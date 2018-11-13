package tcss450.uw.edu.team8app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tcss450.uw.edu.team8app.account.LoginFragment;
import tcss450.uw.edu.team8app.account.RegisterFragment;
import tcss450.uw.edu.team8app.account.VerifyAccountDialog;
import tcss450.uw.edu.team8app.account.password.PasswordResetRequestCodeFragment;
import tcss450.uw.edu.team8app.account.password.PasswordResetRequestEmailFragment;
import tcss450.uw.edu.team8app.account.password.PasswordResetFragment;
import tcss450.uw.edu.team8app.model.Credentials;
import tcss450.uw.edu.team8app.utils.WaitFragment;

/**
 * The entry activity for the application.
 *
 * @author Jim Phan akari0@uw.edu
 */
public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener,
    RegisterFragment.OnFragmentInteractionListener,
    PasswordResetRequestEmailFragment.OnInitiateResetListener,
    PasswordResetRequestCodeFragment.OnCodeCheckListener,
    PasswordResetFragment.OnResetPasswordListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            if(findViewById(R.id.main_fragment_container) != null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.main_fragment_container, new LoginFragment())
                        .commit();
            }
        }

//        Intent myIntent = new Intent(this, HomeActivity.class);
//        startActivity(myIntent);
    }

    @Override
    public void onRegisterClicked() {
        //should we add a backstack to the login page?
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, new RegisterFragment());
        transaction.commit();
    }

    @Override
    public void onResetPasswordClicked() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, new PasswordResetRequestEmailFragment());
        transaction.commit();
    }

    @Override
    public void onLoginSuccess(Credentials credentials) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Credentials.CREDIT_TAG, credentials);
        startActivity(intent);
        //End this activity and remove it from the Activity back stack.
        finish();
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_fragment_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();
    }

    @Override
    public void onRegisterSuccess(Credentials credentials) {
        tellUserToVerify(credentials);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_fragment_container, new LoginFragment()).addToBackStack(null)
                .commit();
    }

    @Override
    public void tellUserToVerify(Credentials credentials) {
        VerifyAccountDialog dialog = new VerifyAccountDialog();
        dialog.setWarningDialog(getString(R.string.notify_check_email));
        dialog.setEmail(credentials.getEmail());
        dialog.setUsername(credentials.getUsername());
        dialog.show(getSupportFragmentManager(), VerifyAccountDialog.DIALOG_TAG);
    }

    @Override
    public void onEmailSubmitSuccess(String email) {
        Bundle args = new Bundle();
        args.putString("email", email);

        Fragment next = new PasswordResetRequestCodeFragment();
        next.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, next);
        transaction.commit();
    }

    @Override
    public void onCodeSubmitSuccess(String email, String code) {
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("code", code);

        Fragment next = new PasswordResetFragment();
        next.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, next);
        transaction.commit();
    }

    @Override
    public void onPasswordResetSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //End this activity and remove it from the Activity back stack.
        finish();
    }
}
