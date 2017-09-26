package com.cargo.sbpd.sync.clients;

import android.support.annotation.IntDef;

import com.cargo.sbpd.bus.RxBus;
import com.cargo.sbpd.bus.RxMessage;
import com.cargo.sbpd.jobs.CancelService;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.FileNotification;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.sync.SyncManager;
import com.cargo.sbpd.sync.filelisting.AbstractLocalConverter;
import com.cargo.sbpd.sync.filelisting.AbstractRemoteConverter;
import com.cargo.sbpd.sync.filelisting.LocalConverter;
import com.cargo.sbpd.sync.filelisting.RemoteConverter;
import com.cargo.sbpd.utils.FormatUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by leb on 17/07/2017.
 */

public abstract class AbstractSynchroClient {

    //TODO dispose this object when not needed anymore, maybe on connect/disconnect
    private final Disposable mSubscription;
    protected AbstractRemoteConverter mRemoteFileConverter;
    protected AbstractLocalConverter mLocalFileConverter;
    private boolean mCancel;
    protected ActionDone mActionDone;
    protected FolderToSync mFolderToSync;
    private long mCurrentFileSizeTransferred;
    private long mTotalSize;
    private long mSizeFilesAlreadyTransferred;

    @IntDef({Direction.LOCAL_TO_REMOTE, Direction.REMOTE_TO_LOCAL, Direction.FUSION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
        int LOCAL_TO_REMOTE = 0;
        int REMOTE_TO_LOCAL = 1;
        int FUSION = 2;
    }

    public AbstractSynchroClient() {
        this(new LocalConverter(), new RemoteConverter());
    }

    public AbstractSynchroClient(AbstractLocalConverter localConverter, AbstractRemoteConverter remoteConverter) {
        mCancel = false;
        mSubscription = RxBus.getRxBusSingleton().asFlowable().observeOn(Schedulers.newThread())
                .subscribe(o -> {
                    if (o instanceof RxMessage) {
                        RxMessage rxRequest = (RxMessage) o;
                        if (rxRequest.getRequest().equals(CancelService.ACTION_CANCEL)) {
                            mCancel = true;
                            cancel();
                        }
                    }
                });
        mRemoteFileConverter = remoteConverter == null ? new RemoteConverter() : remoteConverter;
        mLocalFileConverter = localConverter == null ? new LocalConverter() : localConverter;
    }

    /**
     * @return Name of the class
     */
    public String getClientName() {
        return this.getClass().getSimpleName();
    }

    /**
     * List the files of the folder f. Returns only the files (not directories) AND the EMPTY directories.
     *
     * @param path
     * @return
     */
    public abstract List<RemoteFile> listFilesAndEmptyDirectoriesRecursively(String path, FolderToSync folderLocal) throws IOException;

    //region download

    /**
     * folder must contains the name of the local folder on which you want to store the files and
     * the remote folder to sync.
     * <p>
     * Ex :
     * <p>
     * +------------------+--------+-------------------------------------+
     * | Local            | Remote | Result                              |
     * +------------------+--------+-------------------------------------+
     * | /Sync/Test/      | /      | /Sync/Test/[Content of /]           |
     * +------------------+--------+-------------------------------------+
     * | /Sync/Test/      | /Cats/ | /Sync/Test/[Content of /Cats/]      |
     * +------------------+--------+-------------------------------------+
     * | /Sync/Test/Cats/ | /Cats/ | /Sync/Test/Cats/[Content of /Cats/] |
     * +------------------+--------+-------------------------------------+
     *
     * @param folder
     * @param toDownload
     * @throws IOException
     */
    public void downloadListOfFiles(FolderToSync folder, List<RemoteFile> toDownload) throws IOException {
        for (RemoteFile remote : toDownload) {
            mTotalSize += remote.getSize();
        }

        for (RemoteFile remote : toDownload) {
            if (isCancelled()) {
                break;
            }

            boolean result = download(mLocalFileConverter.getLocalFile(folder, remote),
                    mRemoteFileConverter.getPath(folder, remote),
                    remote.getSize(),
                    remote.getLastModification(),
                    SyncManager.getNetwork(),
                    folder);
        }
    }

    /**
     * Download a file to the specified location
     *
     * @param file
     * @param pathFtp
     * @return
     */
    public abstract boolean downloadWithoutLogs(File file, String pathFtp) throws IOException;

    /**
     * Download a file to the specified location
     *
     * @param file
     * @param pathFtp
     * @return
     */
    public boolean download(File file, String pathFtp, long sizeFtp, long lastModification, String network, FolderToSync folderToSync) throws IOException {
        if (mTotalSize == 0) {
            mTotalSize = sizeFtp;
        }

        IOException ioException = null;
        boolean result = false;

        ActionDone actionDone = new ActionDone();
        actionDone.setLocalFilePath(file.getAbsolutePath().substring(folderToSync.getFile().getAbsolutePath().length()));
        actionDone.setRemoteFilePath(pathFtp.substring(folderToSync.getRemotePath().length()));
        actionDone.setLocalFolder(folderToSync.getFile().getAbsolutePath());
        actionDone.setRemoteFolder(folderToSync.getRemotePath());
        actionDone.setSynchroClient(getClientName());
        actionDone.setActionType(LocalFileDao.ActionType.DOWNLOAD);
        actionDone.setDestination(LocalFileDao.Destination.LOCAL);
        actionDone.setSize(sizeFtp);
        actionDone.setLastModification(lastModification);
        actionDone.setStartTransfert(System.currentTimeMillis());
        actionDone.setNetwork(network);
        mActionDone = actionDone;
        mFolderToSync = folderToSync;

        RxBus.getRxBusSingleton().send(new RxMessage(this.getClientName(), SyncManager.EVENT_FILE_SYNCING_STARTED,
                RxMessage.Type.BROADCAST,
                new FileNotification(actionDone, folderToSync, mSizeFilesAlreadyTransferred, mTotalSize)));

        try {
            result = downloadWithoutLogs(file, pathFtp);
        } catch (IOException e) {
            ioException = e;
        }
        actionDone.setEndTransfert(System.currentTimeMillis());
        actionDone.setSizeTransferred(file.length());
        if (ioException != null) {
            actionDone.setException(ioException.toString());
        }
        mSizeFilesAlreadyTransferred += actionDone.getSizeTransferred();
        RxBus.getRxBusSingleton().send(new RxMessage(this.getClientName(), SyncManager.EVENT_FILE_SYNCING_ENDED,
                RxMessage.Type.BROADCAST,
                new FileNotification(actionDone, folderToSync, mSizeFilesAlreadyTransferred, mTotalSize)));
        return result;
    }

    //endregion

    //region upload
    public void uploadListOfFiles(FolderToSync folder, List<LocalFile> files) throws IOException {
        for (LocalFile local : files) {
            mTotalSize += local.getSize();
        }

        for (LocalFile local : files) {
            if (isCancelled()) {
                break;
            }
            boolean result = upload(mLocalFileConverter.getLocalFile(folder, local),
                    mRemoteFileConverter.getPath(folder, local),
                    local.getSize(),
                    local.getLastModification(),
                    SyncManager.getNetwork(),
                    folder);
        }
    }

    public abstract boolean uploadWithoutLogs(File file, String pathFtp) throws IOException;

    public boolean upload(File file, String pathFtp, long size, long lastModification, String network, FolderToSync folderToSync) throws IOException {
        if (mTotalSize == 0) {
            mTotalSize = size;
        }

        IOException ioException = null;
        boolean result = false;

        ActionDone actionDone = new ActionDone();
        actionDone.setLocalFilePath(file.getAbsolutePath().substring(folderToSync.getFile().getAbsolutePath().length()));
        actionDone.setRemoteFilePath(pathFtp.substring(folderToSync.getRemotePath().length()));
        actionDone.setLocalFolder(folderToSync.getFile().getAbsolutePath());
        actionDone.setRemoteFolder(folderToSync.getRemotePath());
        actionDone.setSynchroClient(getClientName());
        actionDone.setActionType(LocalFileDao.ActionType.UPLOAD);
        actionDone.setDestination(LocalFileDao.Destination.REMOTE);
        actionDone.setSize(size);
        actionDone.setLastModification(lastModification);
        actionDone.setStartTransfert(System.currentTimeMillis());
        actionDone.setNetwork(network);
        mActionDone = actionDone;
        mFolderToSync = folderToSync;

        RxBus.getRxBusSingleton().send(new RxMessage(this.getClientName(), SyncManager.EVENT_FILE_SYNCING_STARTED,
                RxMessage.Type.BROADCAST,
                new FileNotification(actionDone, folderToSync, mSizeFilesAlreadyTransferred, mTotalSize)));

        try {
            result = uploadWithoutLogs(file, pathFtp);
            actionDone.setSizeTransferred(file.length());
        } catch (IOException e) {
            ioException = e;
            e.printStackTrace();
        }
        actionDone.setEndTransfert(System.currentTimeMillis());
        actionDone.setSizeTransferred(actionDone.getSizeTransferred());
        if (ioException != null) {
            actionDone.setException(ioException.toString());
        }
        mSizeFilesAlreadyTransferred += actionDone.getSizeTransferred();
        RxBus.getRxBusSingleton().send(new RxMessage(this.getClientName(), SyncManager.EVENT_FILE_SYNCING_ENDED,
                RxMessage.Type.BROADCAST,
                new FileNotification(actionDone, folderToSync, mSizeFilesAlreadyTransferred, mTotalSize)));
        return result;
    }
    //endregion

    //region delete
    public void deleteListOfFiles(FolderToSync folder, List<RemoteFile> files) throws IOException {
        for (RemoteFile remote : files) {
            if (isCancelled()) {
                break;
            }
            delete(folder, remote.getName());
        }
    }

    public abstract boolean deleteWithoutLogs(String path) throws IOException;

    public boolean delete(FolderToSync folderToSync, String path) throws IOException {
        boolean result = false;
        IOException ioException = null;

        ActionDone actionDone = new ActionDone();
        actionDone.setRemoteFilePath(path);
        actionDone.setLocalFolder(folderToSync.getFile().getAbsolutePath());
        actionDone.setRemoteFolder(folderToSync.getRemotePath());
        actionDone.setSynchroClient(getClientName());
        actionDone.setActionType(LocalFileDao.ActionType.DELETE);
        actionDone.setDestination(LocalFileDao.Destination.REMOTE);
        actionDone.setSize(getSize(FormatUtils.addSlashes(folderToSync.getRemotePath(), path)));
        actionDone.setLastModification(getLastModification(FormatUtils.addSlashes(folderToSync.getRemotePath(), path)));
        actionDone.setStartTransfert(System.currentTimeMillis());
        actionDone.setNetwork(SyncManager.getNetwork());
        mActionDone = actionDone;
        mFolderToSync = folderToSync;

        RxBus.getRxBusSingleton().send(new RxMessage(this.getClientName(), SyncManager.EVENT_FILE_SYNCING_STARTED,
                RxMessage.Type.BROADCAST,
                // total size and size transferred are null for delete operations
                new FileNotification(actionDone, mFolderToSync, mSizeFilesAlreadyTransferred, mTotalSize)));

        try {
            result = deleteWithoutLogs(FormatUtils.addSlashes(folderToSync.getRemotePath(), path));
        } catch (IOException e) {
            ioException = e;
        }
        actionDone.setEndTransfert(System.currentTimeMillis());
        if (ioException != null) {
            actionDone.setException(ioException.toString());
        }
        RxBus.getRxBusSingleton().send(new RxMessage(this.getClientName(), SyncManager.EVENT_FILE_SYNCING_ENDED,
                RxMessage.Type.BROADCAST,
                new FileNotification(actionDone, mFolderToSync, 0, 0)));
        return result;
    }
    //endregion

    public abstract long getSize(String path);

    public abstract long getLastModification(String path);

    public abstract boolean isResumeSupported();

    @Direction
    public abstract int directionSupported();


    //region update progress
    /**
     * This method should call notifyDataTransferred(actionDone)
     *
     * @param totalBytesTransferred number of bytes transferred
     */
    public abstract void onDataTransferred(long totalBytesTransferred);

    public void notifyDataTransferred(ActionDone actionDone) {
        mCurrentFileSizeTransferred = actionDone.getSizeTransferred();
        RxBus.getRxBusSingleton().send(new RxMessage(this.getClientName(), SyncManager.EVENT_FILE_SYNCING_UPDATED,
                RxMessage.Type.BROADCAST,
                new FileNotification(actionDone, mFolderToSync, mCurrentFileSizeTransferred + mSizeFilesAlreadyTransferred, mTotalSize)));
//        AppDatabase.getInstance().actionDoneDao().insert(actionDone);
    }
    //endregion

    public abstract void cancel();

    public boolean isCancelled() {
        return mCancel;
    }

    public abstract boolean connect();

    public abstract boolean disconnect();


    public void setRemoteFileConverter(AbstractRemoteConverter remoteFileConverter) {
        mRemoteFileConverter = remoteFileConverter;
    }

    public void setLocalFileConverter(AbstractLocalConverter localFileConverter) {
        mLocalFileConverter = localFileConverter;
    }

    public AbstractRemoteConverter getRemoteFileConverter() {
        return mRemoteFileConverter;
    }

    public AbstractLocalConverter getLocalFileConverter() {
        return mLocalFileConverter;
    }
}
