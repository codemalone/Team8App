package tcss450.uw.edu.team8app.connections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Connection;
import tcss450.uw.edu.team8app.utils.SendPostAsyncTask;
import tcss450.uw.edu.team8app.utils.WaitFragment;

public class ConnectionsPagerAdapter extends FragmentStatePagerAdapter implements WaitFragment.OnFragmentInteractionListener {

    private List<Fragment> mFragmentList;
    private Context mContext;
    private ArrayList<Connection> mConnections;
    private RecyclerView mRecyclerView;
    private ConstraintLayout mLayout;
    private ConnectionsRecyclerViewAdapter mAdapter;
    private ViewGroup mContainer;

    public ConnectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFragmentList = new ArrayList<Fragment>();
        mContext = context;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return o == view;
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
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

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                Toast.makeText(mContext,
                        "Selected page position: " + position, Toast.LENGTH_SHORT).show();
                //return ConnectionsPagerFragment.newInstance(0, "Page # 1");
                return new ConnectionsPagerFragment();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                Toast.makeText(mContext,
                        "Selected page position: " + position, Toast.LENGTH_SHORT).show();
                //return ConnectionsPagerFragment.newInstance(1, "Page # 2");
                return new ConnectionsPagerFragment();
            case 2: // Fragment # 1 - This will show SecondFragment
                Toast.makeText(mContext,
                        "Selected page position: " + position, Toast.LENGTH_SHORT).show();
                //return ConnectionsPagerFragment.newInstance(2, "Page # 3");
                return new ConnectionsPagerFragment();
            default:
                return null;
        }
    }

//    @NonNull
//    @Override
//    public Object instantiateItem(@NonNull final ViewGroup container, int position) {
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        mLayout = (ConstraintLayout) inflater.inflate(R.layout.fragment_connections_tab, null, false);
//        mConnections = new ArrayList<>();
//        mContainer = container;
//        Log.e("POSITION POSITION", "" + position);
//        Uri.Builder uriBuilder = new Uri.Builder()
//                .scheme(mContext.getString(R.string.ep_scheme))
//                .encodedAuthority(mContext.getString(R.string.ep_base_url))
//                .appendPath(mContext.getString(R.string.ep_connections))
//                .appendPath(mContext.getString(R.string.ep_get));
//        if (position == 0) {
//            uriBuilder.appendPath(mContext.getString(R.string.ep_active));
//        } else if (position == 1) {
//            uriBuilder.appendPath(mContext.getString(R.string.ep_pending));
//        } else if (position == 2) {
//            uriBuilder.appendPath(mContext.getString(R.string.ep_received));
//        }
//        Uri uri = uriBuilder.build();
//        JSONObject msg = new JSONObject();
//        try {
//            msg.put("token", FirebaseInstanceId.getInstance().getToken());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        new SendPostAsyncTask.Builder(uri.toString(), msg)
//                .onPreExecute(this::handleGetOnPre)
//                .onPostExecute(this::handleGetOnPost)
//                .onCancelled(this::handleErrorsInTask)
//                .build().execute();
//
//        return mLayout;
//    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        onWaitFragmentInteractionHide();
    }

    private void handleGetOnPre() {
        onWaitFragmentInteractionShow();
    }

    private void handleGetOnPost(String result) {
        try {
            JSONObject json = new JSONObject(result);
            JSONArray data = json.getJSONArray("data");
            int myID = json.getInt("id");
            //Log.e("TEST", "" + data);
            for (int i = 0; i < data.length(); i++) {
                JSONObject currentMember = data.getJSONObject(i);
                int verified = 0;
                int sender = 0;
                if (!currentMember.isNull("verified")){
                    verified = currentMember.getInt("verified");
                    if (currentMember.getInt("memberid_a") == myID) {
                        sender = 1;
                    } else {
                        sender = 2;
                    }
                }
                mConnections.add(new Connection(currentMember.getString("firstname"), currentMember.getString("lastname"), currentMember.getString("username"), currentMember.getString("email"), verified, sender));
                mRecyclerView = new RecyclerView(mContext);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

                mAdapter = new ConnectionsRecyclerViewAdapter(mConnections, mContext);
                mRecyclerView.setAdapter(mAdapter);
                mLayout.addView(mRecyclerView);

                mContainer.removeAllViews();
                mContainer.addView(mLayout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWaitFragmentInteractionShow() {

    }

    @Override
    public void onWaitFragmentInteractionHide() {

    }
}
