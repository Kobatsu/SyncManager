package com.cargo.sbpd.ui.notifications.chooseclient;

import android.app.NotificationManager;
import android.content.Context;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.cargo.sbpd.ui.notifications.chooseclient.AbstractNotifChooseClient.ID_NOTIFICATION_CHOOSE_CLIENT;

/**
 * Created by leb on 04/09/2017.
 */

public class NotifChooseClientFactory extends AbstractNotifChooseClientFactory {

    private final Context mContext;

    public NotifChooseClientFactory(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public NotifChooseClient create(Context context) {
        return new NotifChooseClient(context);
    }

    @Override
    public void cancelAll() {
        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).cancel(ID_NOTIFICATION_CHOOSE_CLIENT);
    }
}
