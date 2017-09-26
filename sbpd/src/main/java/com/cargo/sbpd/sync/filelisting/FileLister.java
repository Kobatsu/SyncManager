package com.cargo.sbpd.sync.filelisting;

import android.util.Log;

import com.cargo.sbpd.bus.RxBus;
import com.cargo.sbpd.bus.RxMessage;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.FileNotification;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.sync.SyncManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leb on 27/07/2017.
 */

public class FileLister {

    /**
     * List the files of the folder f. Returns only the files (not directories) AND the EMPTY directories.
     *
     * @param f
     * @return
     */
    public static List<File> listFilesAndEmptyDirectoriesRecursively(File f, ListFilter listFilter) {
        List<File> list = new ArrayList<>();
        File[] listFiles = f.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile()) {
                    if (listFilter == null || listFilter.acceptLocal(file.getAbsolutePath())) {
                        list.add(file);
                    }
                }
                if (file.isDirectory()) {
                    List<File> fileList = listFilesAndEmptyDirectoriesRecursively(file, listFilter);

                    if (fileList.size() > 0) {
                        list.addAll(fileList);
                    } else {
                        if (listFilter == null || listFilter.acceptLocal(file.getAbsolutePath())) {
                            list.add(file);
                        }
                    }
                }
            }
        }
        return list;
    }

    public static List<LocalFile> loadLocalFiles(FolderToSync f, AbstractLocalConverter localConverter) {
        return localConverter.convertToRoomFiles(f,
                FileLister.listFilesAndEmptyDirectoriesRecursively(f.getFile(), f.getListFilter()));
    }

    /**
     * Ne fonctionne pas parfaitement. Si on a par exemple :
     * /Cats/Food/Baguette/fichier.jpg
     * <p>
     * 1er passage : fichier.jpg supprimé
     * 2ème passage : Baguette supprimé
     * 3ème passage : Food supprimé
     *
     * @param folder
     * @param list
     * @return
     */
    public static boolean deleteListOfFiles(FolderToSync folder, List<LocalFile> list, AbstractLocalConverter localFileConverter) {
        boolean success = true;
        for (LocalFile file : list) {
            ActionDone actionDone = new ActionDone();
            actionDone.setActionType(LocalFileDao.ActionType.DELETE);
            actionDone.setDestination(LocalFileDao.Destination.LOCAL);
            actionDone.setSize(file.getSize());
            actionDone.setLastModification(file.getLastModification());
            actionDone.setStartTransfert(System.currentTimeMillis());
            actionDone.setNetwork(SyncManager.getNetwork());
            File f = localFileConverter.getLocalFile(folder, file);
            actionDone.setLocalFolder(folder.getFile().getAbsolutePath());
            actionDone.setLocalFilePath(file.getName());
            RxBus.getRxBusSingleton().send(new RxMessage(FileLister.class.getSimpleName(), SyncManager.EVENT_FILE_SYNCING_STARTED,
                    RxMessage.Type.BROADCAST,
                    new FileNotification(actionDone, folder, 0, 0)));
            boolean result = deleteDir(f);
            Log.d("FileLister", f.getAbsolutePath() + " delete success = " + result);
            success &= result;
            if (success) {
                // TODO do we have to wait only log successfull delete?
            }
            actionDone.setEndTransfert(System.currentTimeMillis());
            RxBus.getRxBusSingleton().send(new RxMessage(FileLister.class.getSimpleName(), SyncManager.EVENT_FILE_SYNCING_ENDED,
                    RxMessage.Type.BROADCAST,
                    new FileNotification(actionDone, folder, 0, 0)));
        }
        return success;
    }


    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
