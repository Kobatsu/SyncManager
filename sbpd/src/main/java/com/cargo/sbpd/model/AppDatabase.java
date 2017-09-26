package com.cargo.sbpd.model;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.Environment;

import com.cargo.sbpd.model.dao.ActionDoneDao;
import com.cargo.sbpd.model.dao.ListingLogDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.ListingLog;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.model.dao.RemoteFileDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by leb on 27/07/2017.
 */

@Database(entities = {LocalFile.class, RemoteFile.class, ActionDone.class, ListingLog.class}, version = 12)
@TypeConverters({DatabaseTypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "sync_database";

//    public abstract RoomFileDao roomFileDao();

    public abstract LocalFileDao localFileDao();

    public abstract RemoteFileDao remoteFileDao();

    public abstract ActionDoneDao actionDoneDao();

    public abstract ListingLogDao listingLogDao();

    private static AppDatabase ourInstance;

    public static AppDatabase getInstance() {
        if (ourInstance == null) {
            throw new NullPointerException("initInstance not called");
        }
        return ourInstance;
    }

    public static void initInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).fallbackToDestructiveMigration()
//                    .allowMainThreadQueries()
                    .build();
        }
    }

    public static void exportDatabase(Context context, String name) {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String currentDBPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
                String backupDBPath = name;
                //previous wrong  code
                // **File currentDB = new File(data,currentDBPath);**
                // correct code
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}