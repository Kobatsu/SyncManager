package com.cargo.sbpd.model.objects;

/**
 * Created by leb on 14/09/2017.
 */

public class FileNotification {
    private final long mSizeFolderTransferred;
    private final long mSizeFolderTotal;
    private final ActionDone mActionDone;
    private final FolderToSync mFolderToSync;

    public FileNotification(ActionDone actionDone, FolderToSync folderToSync, long sizeFolderTransferred, long sizeFolderTotal) {
        mActionDone = actionDone;
        mFolderToSync = folderToSync;
        mSizeFolderTransferred = sizeFolderTransferred;
        mSizeFolderTotal = sizeFolderTotal;
    }

    public long getSizeFolderTransferred() {
        return mSizeFolderTransferred;
    }

    public long getSizeFolderTotal() {
        return mSizeFolderTotal;
    }

    public ActionDone getActionDone() {
        return mActionDone;
    }

    public FolderToSync getFolderToSync() {
        return mFolderToSync;
    }
}
