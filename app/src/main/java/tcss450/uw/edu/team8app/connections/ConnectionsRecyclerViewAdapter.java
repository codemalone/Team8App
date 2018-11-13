package tcss450.uw.edu.team8app.connections;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import tcss450.uw.edu.team8app.R;
import tcss450.uw.edu.team8app.model.Connection;

public class ConnectionsRecyclerViewAdapter  extends RecyclerView.Adapter<ConnectionsRecyclerViewAdapter.ViewHolder>{

    private final List<Connection> mData;

    public ConnectionsRecyclerViewAdapter(List<Connection> data) {
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_connection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mData.get(position);
        holder.mFirstView.setText(mData.get(position).getFirstName());
        holder.mLastView.setText(mData.get(position).getLastName());
        holder.mUsernameView.setText(mData.get(position).getUsername());
        holder.mEmailView.setText(mData.get(position).getEmail());
        holder.mVerifiedView.setText("" + mData.get(position).getVerified());
        holder.mSenderView.setText("" + mData.get(position).getSender());
        if (mData.get(position).getVerified() == 1) {
            holder.mButton.setText("Send Message");
            holder.mDeleteButton.setOnClickListener(this::deleteContact);
        } else {
            if (mData.get(position).getSender() == 1) {
                holder.mButton.setText("Cancel Invite");
                holder.mDeleteButton.setVisibility(View.INVISIBLE);
            } else if (mData.get(position).getSender() == 2) {
                holder.mButton.setText("Accept Invite");
                holder.mDeleteButton.setOnClickListener(this::declineInvite);
            } else {
                holder.mDeleteButton.setVisibility(View.INVISIBLE);
            }
        }
        holder.mButton.setOnClickListener(this::primaryButtonOnClick);
    }

    private void primaryButtonOnClick(View view) {
        Log.e(view.toString(), "TESTEST");
    }

    private void deleteContact(View view) {
        Log.e(view.toString(), "TESTEST");
    }

    private void declineInvite(View view) {
        Log.e(view.toString(), "TESTEST");
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mFirstView;
        final TextView mLastView;
        final TextView mUsernameView;
        final TextView mEmailView;
        final TextView mVerifiedView;
        final TextView mSenderView;
        final Button mButton;
        final ImageButton mDeleteButton;
        Connection mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mFirstView = view.findViewById(R.id.textView_recyclerView_connection_firstName);
            mLastView = view.findViewById(R.id.textView_recyclerView_connection_lastName);
            mUsernameView = view.findViewById(R.id.textView_recyclerView_connection_username);
            mEmailView = view.findViewById(R.id.textView_recyclerView_connection_email);
            mVerifiedView = view.findViewById(R.id.textView_recyclerView_connection_verified);
            mSenderView = view.findViewById(R.id.textView_recyclerView_connection_sender);
            mButton = view.findViewById(R.id.button_recyclerView_connection);
            mDeleteButton = view.findViewById(R.id.imageButton_recyclerView_connection_delete);
        }
    }
}
