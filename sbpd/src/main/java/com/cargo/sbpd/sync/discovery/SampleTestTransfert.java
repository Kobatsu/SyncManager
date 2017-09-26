package com.cargo.sbpd.sync.discovery;

import android.content.Context;
import android.os.Environment;

import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by leb on 08/08/2017.
 *
 * Class to test a synchro client on operations of upload, download and delete. It produces
 * statistics to be able to chose between synchro clients.
 */

public class SampleTestTransfert {

    public static boolean testSample(Context context, AbstractSynchroClient client, String network) {
        File f = createRandomFile(context, 512 * 1024);
        boolean success = false;
        IOException ioException = null;

        ActionDone actionDone = new ActionDone();
        actionDone.setLocalFilePath(f.getName());
        actionDone.setLocalFolder(f.getParentFile().getAbsolutePath());
        actionDone.setRemoteFolder("/");
        actionDone.setRemoteFilePath("/" + f.getName());
        actionDone.setSynchroClient(client.getClientName());
        actionDone.setActionType(LocalFileDao.ActionType.UPLOAD);
        actionDone.setDestination(LocalFileDao.Destination.REMOTE);
        actionDone.setSize(f.length());
        actionDone.setLastModification(f.lastModified());
        actionDone.setStartTransfert(System.currentTimeMillis());
        actionDone.setNetwork(network);
        actionDone.setIsSample(true);
        actionDone.setId(AppDatabase.getInstance().actionDoneDao().insert(actionDone));
        try {
            client.uploadWithoutLogs(f, "/" + f.getName());
            success = true;
        } catch (IOException e) {
            ioException = e;
        }
        actionDone.setEndTransfert(System.currentTimeMillis());
        actionDone.setSizeTransferred(f.length());
        if (ioException != null) {
            actionDone.setException(ioException.toString());
        }
        AppDatabase.getInstance().actionDoneDao().insert(actionDone);

        actionDone = new ActionDone();
        actionDone.setLocalFilePath(f.getName());
        actionDone.setLocalFolder(f.getParentFile().getAbsolutePath());
        actionDone.setRemoteFolder("/");
        actionDone.setRemoteFilePath("/" + f.getName());
        actionDone.setSynchroClient(client.getClientName());
        actionDone.setActionType(LocalFileDao.ActionType.DOWNLOAD);
        actionDone.setDestination(LocalFileDao.Destination.LOCAL);
        actionDone.setSize(f.length());
        actionDone.setLastModification(f.lastModified());
        actionDone.setStartTransfert(System.currentTimeMillis());
        actionDone.setNetwork(network);
        actionDone.setIsSample(true);
        actionDone.setId(AppDatabase.getInstance().actionDoneDao().insert(actionDone));
        try {
            client.downloadWithoutLogs(f, f.getName());
            if (success) {
                success = true;
            }
        } catch (IOException e) {
            ioException = e;
        }
        actionDone.setEndTransfert(System.currentTimeMillis());
        actionDone.setSizeTransferred(f.length());

        if (ioException != null) {
            actionDone.setException(ioException.toString());
        }
        AppDatabase.getInstance().actionDoneDao().insert(actionDone);


        actionDone = new ActionDone();
        actionDone.setLocalFilePath(f.getName());
        actionDone.setLocalFolder(f.getParentFile().getAbsolutePath());
        actionDone.setRemoteFolder("/");
        actionDone.setRemoteFilePath("/" + f.getName());
        actionDone.setSynchroClient(client.getClientName());
        actionDone.setActionType(LocalFileDao.ActionType.DELETE);
        actionDone.setDestination(LocalFileDao.Destination.REMOTE);
        actionDone.setSize(f.length());
        actionDone.setLastModification(f.lastModified());
        actionDone.setStartTransfert(System.currentTimeMillis());
        actionDone.setNetwork(network);
        actionDone.setIsSample(true);
        actionDone.setId(AppDatabase.getInstance().actionDoneDao().insert(actionDone));
        try {
            client.deleteWithoutLogs(f.getName());
            if (success) {
                success = true;
            }
        } catch (IOException e) {
            ioException = e;
        }
        actionDone.setEndTransfert(System.currentTimeMillis());
        actionDone.setSizeTransferred(f.length());
        if (ioException != null) {
            actionDone.setException(ioException.toString());
        }
        AppDatabase.getInstance().actionDoneDao().insert(actionDone);


        return true;
    }

    private static File createRandomFile(Context context, int size) {
        File file = new File(Environment.getExternalStorageDirectory(), "a.txt");
        RandomAccessFile newSparseFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            file.delete();
            StringBuilder sb = new StringBuilder(size);
            for (int i = 0; i < size; i++) {
                sb.append('a');
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(sb.toString().getBytes());


//            newSparseFile = new RandomAccessFile(file, "rw");
//            // create a 1MB file:
//            newSparseFile.setLength(size);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (newSparseFile != null) {
            try {
//                    newSparseFile.close();
                fileOutputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
//            }
        }
        return file;
    }


}
