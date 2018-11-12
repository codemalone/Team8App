package tcss450.uw.edu.team8app;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionsFragment extends Fragment {
    ViewPager mViewPager;
    ConnectionsPagerAdapter mPagerAdapter;

    public ConnectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Connections");
        View v = inflater.inflate(R.layout.fragment_connections, container, false);

        mViewPager = v.findViewById(R.id.viewPager_connections);
        mPagerAdapter = new ConnectionsPagerAdapter(getActivity());
        TabLayout tabLayout = v.findViewById(R.id.tabLayout_connections);
        mViewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.connections, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connections_add:
                //Toast.makeText(getActivity(), "Calls Icon Click", Toast.LENGTH_SHORT).show();
                loadFragment(new ConnectionsAddFragment());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_home_container, frag)
                .addToBackStack(null);
        transaction.commit();
    }

}
