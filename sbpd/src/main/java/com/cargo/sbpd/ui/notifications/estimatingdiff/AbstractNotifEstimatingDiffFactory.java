package com.cargo.sbpd.ui.notifications.estimatingdiff;

import android.content.Context;

import com.cargo.sbpd.model.objects.FolderToSync;

import java.util.List;

/**
 * Created by leb on 04/09/2017.
 */

public abstract class AbstractNotifEstimatingDiffFactory {

    private final Context mContext;

    public AbstractNotifEstimatingDiffFactory(Context context) {
        mContext = context;
    }

    public abstract AbstractNotifEstimatingDiff create(Context context, List<FolderToSync> listFolders, int folderIndex);

    public abstract void cancelAll();
}
