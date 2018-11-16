package tcss450.uw.edu.team8app.connections;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

import tcss450.uw.edu.team8app.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionsFragment extends Fragment {
    static final int ITEMS = 10;
    MyAdapter mAdapter;
    ViewPager mPager;

    public ConnectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Connections");
        View v = inflater.inflate(R.layout.fragment_connections, container, false);

        mAdapter = new MyAdapter(getActivity().getSupportFragmentManager());
        mPager = (ViewPager) v.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        return v;
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
            // Generate title based on item position
            switch (position) {
                case 0:
                    return "Active";
                case 1:
                    //return mContext.getString(R.string.category_places);
                    return "Pending";
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
