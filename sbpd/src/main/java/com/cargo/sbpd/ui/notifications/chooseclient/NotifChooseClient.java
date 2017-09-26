package com.cargo.sbpd.ui.notifications.chooseclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.cargo.sbpd.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by leb on 04/09/2017.
 */

public class NotifChooseClient extends AbstractNotifChooseClient {
    private final Context mContext;

    public NotifChooseClient(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void show() {
        Notification.Builder builder = new Notification.Builder(mContext);

        builder.setAutoCancel(false);
        builder.setContentTitle(mContext.getString(R.string.sbpd_check_network));
        builder.setSmallIcon(R.drawable.ic_folder_black_24dp);
        builder.setOngoing(true);
        builder.setSubText(mContext.getString(R.string.update));   //API level 16
        builder.setProgress(100, 0, true);

        Notification n = builder.build();

        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).notify(ID_NOTIFICATION_CHOOSE_CLIENT, n);
    }

    @Override
    public void cancel() {
        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).cancel(ID_NOTIFICATION_CHOOSE_CLIENT);
    }
}
