package com.cargo.sbpd.ui.notifications.updating;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.cargo.sbpd.R;
import com.cargo.sbpd.jobs.CancelService;
import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.ui.activities.DialogProgressSyncActivity;
import com.cargo.sbpd.utils.FormatUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.cargo.sbpd.ui.notifications.estimatingdiff.AbstractNotifEstimatingDiff.ID_NOTIFICATION_ESTIMATING_DIFF;
import static com.cargo.sbpd.ui.notifications.updating.AbstractNotifUpdating.ID_NOTIFICATION_UPDATING;

/**
 * Created by leb on 04/09/2017.
 */

public class NotifUpdatingFactory extends AbstractNotifUpdatingFactory {


    private final Context mContext;

    public NotifUpdatingFactory(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public NotifUpdating create(List<FolderToSync> listFolders, int folderIndex, ActionDone actionDone,
                                long sizeTransferred, long sizeTotal, int smallIcon, int largeIcon) {

        return new NotifUpdating(mContext, listFolders, folderIndex, actionDone, sizeTransferred, sizeTotal,
                smallIcon, largeIcon);
    }

    @Override
    public void cancelAll() {
        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).cancel(ID_NOTIFICATION_UPDATING);
    }
}
