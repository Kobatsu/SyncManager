package com.cargo.sbpd.ui.notifications.chooseclient;

import android.content.Context;

/**
 * Created by leb on 04/09/2017.
 *
 * The usage of the T thing may be unnecessary
 */

public abstract class AbstractNotifChooseClientFactory {

    public AbstractNotifChooseClientFactory(Context context) {
    }

    public abstract AbstractNotifChooseClient create(Context context);

    public abstract void cancelAll();
}
