package com.cargo.demoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.sync.SyncManager;
import com.cargo.sbpd.sync.SyncManagerBuilder;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.ui.activities.DialogProgressSyncActivity;
import com.cargo.synchroedtftpj.EdtFTPjClient;
import com.cargo.synchroftpapache.ApacheFTPClient;
import com.cargo.synchroftpapache.ApacheFTPClientLaggy;
import com.cargo.synchroftpapache.ApacheFTPClientSuperLaggy;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;

import java.io.File;
import java.util.Set;

import static com.cargo.demoapp.MainActivity.REQ_EXTERNAL_STORAGE;

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final View btnJob = findViewById(R.id.launch_job);
        btnJob.setOnClickListener(view -> {
            //todo Doesn't work
            boolean jobRunning = false;
            Set<Job> jobs = JobManager.instance().getAllJobsForTag(SyncTestJob.TAG);
            for (Job j : jobs) {
                if (j.isFinished()) {
                    Log.d("JobFinished", "" + j.toString());
                } else {
                    Log.d("JobRunning", "" + j.toString());
                    jobRunning = true;
                    // don't launch job now
                }
            }
            if (!jobRunning) {
                JobManager.instance().cancelAllForTag(SyncTestJob.TAG);
                SyncTestJob.scheduleJob();
            } else {
                Toast.makeText(DemoActivity.this, "Job already running", Toast.LENGTH_SHORT).show();
            }
        });

        final View btnWork = findViewById(R.id.launch_synchro_manually);
        btnWork.setOnClickListener(view -> {
            btnJob.setEnabled(false);
            btnWork.setEnabled(false);
            final File f = new File(Environment.getExternalStorageDirectory(), "LibrarySynchro/");
            f.mkdirs();
            new Thread(() -> {
                SyncManagerBuilder builder = new SyncManagerBuilder(DemoActivity.this);

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
                syncManager.unregisterToNetworkChange(DemoActivity.this);

                runOnUiThread(() -> {
                    btnJob.setEnabled(true);
                    btnWork.setEnabled(true);
                });
            }).start();

        });

        findViewById(R.id.btn_progress_activity).setOnClickListener(view -> {
            Intent intent = new Intent(DemoActivity.this, DialogProgressSyncActivity.class);
            startActivity(intent);
        });


        if (ContextCompat.checkSelfPermission(DemoActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DemoActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(DemoActivity.this, "Permission not granted !", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
