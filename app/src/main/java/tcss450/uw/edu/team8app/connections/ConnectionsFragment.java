package tcss450.uw.edu.team8app.connections;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Connection;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionsFragment extends Fragment {
    private MyAdapter mAdapter;
    private ViewPager mPager;
    private View v;

    public ConnectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Connections");
        v = inflater.inflate(R.layout.fragment_connections, container, false);

        onResume();
        if (getArguments() != null) {
            if (getArguments().getBoolean("from_connection_notification")) {
                mPager.setCurrentItem(2);
            }
        }
        mAdapter = new MyAdapter(getChildFragmentManager());
        mPager = v.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                //Log.e("INVITEACCEPTED", "" + prefs.getBoolean("inviteAccepted", false));
                if (prefs.getBoolean("inviteAccepted", false)) {
                    mAdapter.notifyDataSetChanged();
                    prefs.edit().putBoolean("inviteAccepted", false).apply();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mPager.setCurrentItem(1);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ConnectionsPagerFragment.init(position);
                case 1:
                    return ConnectionsPagerFragment.init(position);
                default:
                    return ConnectionsPagerFragment.init(position);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Pending";
                case 1:
                    return "Active";
                case 2:
                    return "Received";
                default:
                    return null;
            }
        }
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

    public interface OnListFragmentInteractionListener {
        void onStartChatInteraction(Connection connection);
    }
}
