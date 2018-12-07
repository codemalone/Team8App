package tcss450.uw.edu.team8app.chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.chat.ConversationFragment.OnListFragmentInteractionListener;
import tcss450.uw.edu.team8app.model.Conversation;

/**
 * {@link RecyclerView.Adapter} that can display a conversation and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyConversationRecyclerViewAdapter extends RecyclerView.Adapter<MyConversationRecyclerViewAdapter.ViewHolder> {

    private final List<Conversation> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyConversationRecyclerViewAdapter(List<Conversation> items, OnListFragmentInteractionListener listener) {
        mValues = items;

        for (int i = 0; i < mValues.size(); i++) {
            if (mValues.get(i).getUsers().isEmpty())
                mValues.remove(i);
        }

        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mUsersView.setText(mValues.get(position).parsedUsers(20));
        //holder.mNumbersView.setText("Other Users: " + mValues.get(position).getUsers().size());
        holder.mLastMessageView.setText(mValues.get(position).getLastMessage());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onConversationInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUsersView;
        //public final TextView mNumbersView;
        public final TextView mLastMessageView;
        public Conversation mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsersView = view.findViewById(R.id.conversation_user_names);
            //mNumbersView = view.findViewById(R.id.conversation_user_number);
            mLastMessageView = view.findViewById(R.id.conversation_last_message);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLastMessageView.getText() + "'";
        }
    }
}
