package com.cargo.demoapp;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.sync.SyncManager;
import com.cargo.sbpd.sync.SyncManagerBuilder;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.synchroedtftpj.EdtFTPjClient;
import com.cargo.synchroftpapache.ApacheFTPClient;
import com.cargo.synchroftpapache.ApacheFTPClientLaggy;
import com.cargo.synchroftpapache.ApacheFTPClientSuperLaggy;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by leb on 10/08/2017.
 */

public class SyncTestJob extends Job {
    protected static final String TAG = SyncTestJob.class.getSimpleName();


    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        launchSynchro(getContext());

        return Result.SUCCESS;
    }

    private void launchSynchro(Context context) {
        final File f = new File(Environment.getExternalStorageDirectory(), "LibrarySynchro/");
        f.mkdirs();
        new Thread(() -> {
            SyncManagerBuilder builder = new SyncManagerBuilder(context);

            ApacheFTPClient client = new ApacheFTPClient(Credentials.FTP_ADDRESS, Credentials.FTP_USER, Credentials.FTP_PASSWORD);
            ApacheFTPClientLaggy clientLaggy = new ApacheFTPClientLaggy(Credentials.FTP_ADDRESS, Credentials.FTP_USER, Credentials.FTP_PASSWORD);
            ApacheFTPClientSuperLaggy clientSuperLaggy = new ApacheFTPClientSuperLaggy(Credentials.FTP_ADDRESS, Credentials.FTP_USER, Credentials.FTP_PASSWORD);
            EdtFTPjClient edtFTPjClient = new EdtFTPjClient(Credentials.FTP_ADDRESS, Credentials.FTP_USER, Credentials.FTP_PASSWORD);

            builder.addClient(edtFTPjClient);
//                builder.addClient(client);
//                builder.addClient(clientLaggy);
//                builder.addClient(clientSuperLaggy);

            builder.addFolder(new FolderToSync("Some food", new File(f, "Food"), "/Food/", AbstractSynchroClient.Direction.REMOTE_TO_LOCAL));
            builder.addFolder(new FolderToSync("Beautiful cats", new File(f, "Cats"), "/Cats/", AbstractSynchroClient.Direction.REMOTE_TO_LOCAL));
            builder.addFolder(new FolderToSync("Sport", new File(f, "Sport"), "/Sport/", AbstractSynchroClient.Direction.REMOTE_TO_LOCAL));
            builder.addFolder(new FolderToSync("SportUpload", new File(f, "Sport"), "/SportCopy", AbstractSynchroClient.Direction.LOCAL_TO_REMOTE));
            builder.addFolder(new FolderToSync("Others", new File(f, "Others"), "/Others", AbstractSynchroClient.Direction.LOCAL_TO_REMOTE));
//                builder.setLocalFileConverter(new CustomLocalFileConverter());
//                builder.setRemoteFileConverter(new CustomRemoteFileConverter());

            SyncManager syncManager = builder.build();

            syncManager.sync();

            client.disconnect();
            syncManager.unregisterToNetworkChange(context);

        }).start();
    }

    public static int scheduleJob() {
        if (JobManager.instance().getAllJobRequestsForTag(SyncTestJob.TAG).size() == 0) {
            int jobId = new JobRequest.Builder(SyncTestJob.TAG)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                    .setPersisted(true)
                    .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                    .build()
                    .schedule();
            return jobId;
        } else {
            return -1;
        }
    }

}
