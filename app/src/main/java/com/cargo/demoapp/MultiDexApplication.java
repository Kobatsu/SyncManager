package com.cargo.demoapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;

import com.cargo.sbpd.sync.SyncManager;

/**
 * Created by leb on 28/07/2017.
 */

public class MultiDexApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        SyncManager.initDatabaseInstance(base);
//        JobManager.create(this).addJobCreator(new SynchroTestJobCreator());
        if (ContextCompat.checkSelfPermission(base,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            SyncTestJob.scheduleJob();
        }
    }
}
