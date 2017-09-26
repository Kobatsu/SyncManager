package com.cargo.sbpd.ui.notifications.updating;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.cargo.sbpd.R;
import com.cargo.sbpd.jobs.CancelService;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.ui.activities.DialogProgressSyncActivity;
import com.cargo.sbpd.utils.FormatUtils;

import java.io.File;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.cargo.sbpd.sync.SyncManager.REQUEST_NOTIFICATION_SYNCHRO;

/**
 * Created by leb on 04/09/2017.
 */

public class NotifUpdating extends AbstractNotifUpdating {
    private final Context mContext;
    private final List<FolderToSync> mListFolders;
    private final int mFolderIndex;
    private final ActionDone mActionDone;
    private final long mSizeTransferred;
    private final long mSizeTotal;
    private final int mSmallIcon;
    private final int mLargeIcon;


    public NotifUpdating(Context context, List<FolderToSync> listFolders, int folderIndex,
                         ActionDone actionDone, long sizeTransferred, long sizeTotal,
                         int smallIcon, int largeIcon) {
        super(context, listFolders, folderIndex, actionDone, sizeTransferred, sizeTotal, smallIcon, largeIcon);
        mContext = context;
        mListFolders = listFolders;
        mFolderIndex = folderIndex;
        mActionDone = actionDone;
        mSizeTransferred = sizeTransferred;
        mSizeTotal = sizeTotal;
        mSmallIcon = smallIcon;
        mLargeIcon = largeIcon;
    }

    @Override
    public void show() {
        Intent intent = new Intent(mContext, DialogProgressSyncActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, REQUEST_NOTIFICATION_SYNCHRO, intent, 0);

        Notification.Builder builder = new Notification.Builder(mContext);

        builder.setAutoCancel(false);
        String contentTitle = mListFolders.get(mFolderIndex).getDisplayName();
        builder.setContentTitle(contentTitle);
        String content = new File(mActionDone.getLocalFilePath() != null ? mActionDone.getLocalFilePath() : mActionDone.getRemoteFilePath()).getName();
        builder.setSmallIcon(mSmallIcon);
        switch(mActionDone.getActionType()){
            case LocalFileDao.ActionType.DOWNLOAD:
                builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_download_black_48dp));
                builder.setContentText(content + " - " + FormatUtils.formatPourcentage(((double) mSizeTransferred / (double) mSizeTotal) * 100, 2));
                break;
            case LocalFileDao.ActionType.UPLOAD:
                builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_upload_black_48dp));
                builder.setContentText(content + " - " + FormatUtils.formatPourcentage(((double) mSizeTransferred / (double) mSizeTotal) * 100, 2));
                break;
            case LocalFileDao.ActionType.DELETE:
                builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_close_black_48dp));
                builder.setContentText(content);
                break;
        }
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        // Pourcentage

        builder.setSubText(mContext.getString(R.string.update) + " (" + (mFolderIndex + 1) + "/" + mListFolders.size() + ")");   //API level 16
        if (mActionDone.getActionType().equals(LocalFileDao.ActionType.DOWNLOAD) || mActionDone.getActionType().equals(LocalFileDao.ActionType.UPLOAD)) {
            int max = 10000;
            int current = 0;
            try {
                current = (int) (((double) mSizeTransferred * max) / (double) mSizeTotal);
            } catch (ArithmeticException ex) {
                ex.printStackTrace();
            }
            builder.setProgress(max, current, false);
        } else {
            builder.setProgress(100, 0, true);
        }
//        builder.setNumber(100);

        Intent iAction1 = new Intent(mContext, CancelService.class);
        iAction1.setAction(CancelService.ACTION_CANCEL);
        PendingIntent piAction1 = PendingIntent.getService(mContext, 0, iAction1, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_close_black_24dp_vector, mContext.getString(android.R.string.cancel), piAction1);

        Notification n = builder.build();

        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).notify(ID_NOTIFICATION_UPDATING, n);
    }

    @Override
    public void cancel() {
        ((NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE)).cancel(ID_NOTIFICATION_UPDATING);
    }
}
