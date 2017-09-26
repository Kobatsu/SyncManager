package com.cargo.sbpd.ui.notifications.estimatingdiff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cargo.sbpd.R;
import com.cargo.sbpd.jobs.CancelService;
import com.cargo.sbpd.model.objects.FolderToSync;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by leb on 04/09/2017.
 */

public class NotifEstimatingDiff extends AbstractNotifEstimatingDiff {

    private final Context mContext;
    private final List<FolderToSync> mListFolders;
    private final int mFolderIndex;

    public NotifEstimatingDiff(Context context, List<FolderToSync> listFolders, int folderIndex) {
        super(context, listFolders, folderIndex);
        mContext = context;
        mListFolders = listFolders;
        mFolderIndex = folderIndex;
    }

    @Override
    public void show() {
        Notification.Builder builder = new Notification.Builder(mContext);

        builder.setAutoCancel(false);
        String contentTitle = mListFolders.get(mFolderIndex).getDisplayName();
        builder.setContentTitle(contentTitle);
        builder.setContentText(mContext.getString(R.string.sbpd_calcul_diff));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setOngoing(true);
        builder.setSubText(mContext.getString(R.string.update) + " (" + (mFolderIndex + 1) + "/" + mListFolders.size() + ")");   //API level 16
        builder.setProgress(100, 0, true);

        Intent iAction1 = new Intent(mContext, CancelService.class);
        iAction1.setAction(CancelService.ACTION_CANCEL);
        PendingIntent piAction1 = PendingIntent.getService(mContext, 0, iAction1, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_close_black_24dp_vector, mContext.getString(android.R.string.cancel), piAction1);

        Notification n = builder.build();

        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).notify(ID_NOTIFICATION_ESTIMATING_DIFF, n);

    }

    @Override
    public void cancel() {
        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).cancel(ID_NOTIFICATION_ESTIMATING_DIFF);
    }
}
