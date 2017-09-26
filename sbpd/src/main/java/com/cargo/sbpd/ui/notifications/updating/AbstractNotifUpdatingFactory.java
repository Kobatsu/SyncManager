package com.cargo.sbpd.ui.notifications.updating;

import android.content.Context;

import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.FolderToSync;

import java.util.List;

/**
 * Created by leb on 04/09/2017.
 */

public abstract class AbstractNotifUpdatingFactory {
    public AbstractNotifUpdatingFactory(Context context) {
    }

    public abstract NotifUpdating create(List<FolderToSync> listFolders, int folderIndex,
                                         ActionDone actionDone, long sizeTransferred, long sizeTotal,
                                         int smallIcon, int largeIcon);

    public abstract void cancelAll();

}
