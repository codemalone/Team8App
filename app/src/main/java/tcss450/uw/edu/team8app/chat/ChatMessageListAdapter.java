package tcss450.uw.edu.team8app.chat;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Message;

public class ChatMessageListAdapter extends RecyclerView.Adapter<ChatMessageListAdapter.ViewHolder> {
    private List<Message> mValues;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public MyViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatMessageListAdapter(List<Message> myDataset) {
        mValues = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mEmail.setText(mValues.get(position).getEmail());
        holder.mEmail.setTypeface(null, Typeface.BOLD);
        holder.mMessage.setText(mValues.get(position).getMessage());
        holder.mTimestamp.setText(mValues.get(position).getmTimestamp());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mEmail;
        public final TextView mMessage;
        public final TextView mTimestamp;
        public Message mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            mEmail = (TextView) view.findViewById(R.id.msg_sender);
            mMessage = (TextView) view.findViewById(R.id.msg_message);
            mTimestamp = (TextView) view.findViewById(R.id.msg_timestamp);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mEmail.getText() + "'";
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mValues.size();
    }
}