package com.cargo.sbpd.model.objects;

import java.util.List;

/**
 * Created by leb on 09/08/2017.
 */

public class ListFoldersToSyncState {
    private int mPosition;
    private List<FolderToSync> mFoldersToSync;

    public ListFoldersToSyncState(int position, List<FolderToSync> foldersToSync) {
        mPosition = position;
        mFoldersToSync = foldersToSync;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public List<FolderToSync> getFoldersToSync() {
        return mFoldersToSync;
    }

    public void setFoldersToSync(List<FolderToSync> foldersToSync) {
        mFoldersToSync = foldersToSync;
    }

    @Override
    public String toString() {
        return "ListFoldersToSyncState{" +
                "mPosition=" + mPosition +
                ", mFoldersToSync=" + mFoldersToSync +
                '}';
    }
}
