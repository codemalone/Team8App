package tcss450.uw.edu.team8app.connections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Connection;

public class ConnectionsPagerAdapter extends PagerAdapter {

    private Context mContext;

    ConnectionsPagerAdapter(Context context) {
        super();
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

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        @SuppressLint("InflateParams") ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_connections_tab, null, false);

        RecyclerView recyclerView = new RecyclerView(mContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        ArrayList<Connection> testArray = new ArrayList<>();
        for (int i =0; i < 10; i++) {
            int test = (int) Math.round(Math.random());
            //Toast.makeText(getActivity(), "" + i, Toast.LENGTH_SHORT).show();
            if (test == 1) {
                testArray.add(new Connection("firstname" + i, "lastname" + i, "username" + i, "email" + i, (int) Math.round(Math.random()), (int)(Math.random() * 3)));
            } else {
                testArray.add(new Connection("firstname" + i, "lastname" + i, "username" + i, "email" + i, (int) Math.round(Math.random()), (int)(Math.random() * 3)));
            }
        }

        ConnectionsRecyclerViewAdapter mAdapter = new ConnectionsRecyclerViewAdapter(testArray);
        recyclerView.setAdapter(mAdapter);
        layout.addView(recyclerView);

        container.addView(layout);
        return layout;
    }


//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        //Toast.makeText(mContext, "TEST", Toast.LENGTH_SHORT).show();
//        super.destroyItem(container, position, object);
//    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout)object);
    }

}
