package com.cargo.sbpd.ui.notifications.estimatingdiff;

import android.app.NotificationManager;
import android.content.Context;

import com.cargo.sbpd.model.objects.FolderToSync;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.cargo.sbpd.ui.notifications.estimatingdiff.AbstractNotifEstimatingDiff.ID_NOTIFICATION_ESTIMATING_DIFF;

/**
 * Created by leb on 04/09/2017.
 */

public class NotifEstimatingDiffFactory extends AbstractNotifEstimatingDiffFactory {

    private final Context mContext;

    public NotifEstimatingDiffFactory(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public NotifEstimatingDiff create(Context context, List<FolderToSync> listFolders, int folderIndex) {
        return new NotifEstimatingDiff(context, listFolders, folderIndex);
    }

    @Override
    public void cancelAll() {
        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).cancel(ID_NOTIFICATION_ESTIMATING_DIFF);
    }
}
