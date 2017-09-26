package com.cargo.sbpd.ui.notifications.updating;

import android.content.Context;

import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.ui.notifications.AbstractNotification;

import java.util.List;

/**
 * Created by leb on 04/09/2017.
 */

public abstract class AbstractNotifUpdating extends AbstractNotification {
    public static final int ID_NOTIFICATION_UPDATING = 332;

    public AbstractNotifUpdating(Context context, List<FolderToSync> listFolders, int folderIndex,
                                 ActionDone actionDone, long sizeTransferred, long sizeTotal,
                                 int smallIcon, int largeIcon) {
    }
}
