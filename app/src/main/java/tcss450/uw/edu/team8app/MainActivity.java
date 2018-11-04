package tcss450.uw.edu.team8app;

import android.content.Intent;
import android.net.Credentials;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * The entry activity for the application.
 *
 * @author Jim Phan akari0@uw.edu
 */
public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener,
    RegisterFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if(savedInstanceState == null) {
//            if(findViewById(R.id.main_fragment_container) != null) {
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.main_fragment_container, new LoginFragment())
//                        .commit();
//            }
//        }

        Intent myIntent = new Intent(this, HomeActivity.class);
        startActivity(myIntent);
    }

    private void loginSuccess(boolean success) {
        if(success) {

        } else {

        }
    }

    @Override
    public void onRegisterClicked() {

    }

    @Override
    public void onLoginSuccess(tcss450.uw.edu.team8app.model.Credentials credentials) {

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
    public void onRegisterSuccess(tcss450.uw.edu.team8app.model.Credentials credentials) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.login_fragment, new LoginFragment())
                .commit();
    }
}
