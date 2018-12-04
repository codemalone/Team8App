package tcss450.uw.edu.team8app.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GcmKeepAlive {

    private Context mContext;
    private Intent gTalkHeartBeatIntent;
    private Intent mcsHeartBeatIntent;

    public GcmKeepAlive(Context context) {
        mContext = context;
        gTalkHeartBeatIntent = new Intent(
                "com.google.android.intent.action.GTALK_HEARTBEAT");
        mcsHeartBeatIntent = new Intent(
                "com.google.android.intent.action.MCS_HEARTBEAT");
    }

    public void broadcastIntents() {
        Log.d("GCM KEEP ALIVE", "sending heart beat to keep gcm alive");
        mContext.sendBroadcast(gTalkHeartBeatIntent);
        mContext.sendBroadcast(mcsHeartBeatIntent);
    }

}