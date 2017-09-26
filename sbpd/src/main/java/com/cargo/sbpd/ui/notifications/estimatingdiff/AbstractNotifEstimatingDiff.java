package com.cargo.sbpd.ui.notifications.estimatingdiff;

import android.content.Context;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.ui.notifications.AbstractNotification;

import java.util.List;

/**
 * Created by leb on 04/09/2017.
 */

public abstract class AbstractNotifEstimatingDiff extends AbstractNotification {

    public static final int ID_NOTIFICATION_ESTIMATING_DIFF = 331;

    public AbstractNotifEstimatingDiff(Context context, List<FolderToSync> listFolders,
                                       int folderIndex) {
    }
}
