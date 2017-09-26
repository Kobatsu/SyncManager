package com.cargo.sbpd.model.objects;

import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.sync.filelisting.ListFilter;

import java.io.File;

/**
 * Created by leb on 02/08/2017.
 */

public class FolderToSync {
    private ListFilter mListFilter;
    private File mFile;
    private String mRemotePath;
    private @AbstractSynchroClient.Direction
    int mDirection;
    private String mDisplayName;

    public FolderToSync(String displayName, File file, String remotePath, @AbstractSynchroClient.Direction int direction) {
        mDisplayName = displayName;
        mFile = file;
        setRemotePath(remotePath);
        mDirection = direction;
    }

    public FolderToSync(String displayName, File file, String remotePath, @AbstractSynchroClient.Direction int direction, ListFilter listFilter) {
        mDisplayName = displayName;
        mFile = file;
        setRemotePath(remotePath);
        mDirection = direction;
        mListFilter = listFilter;
    }

    public ListFilter getListFilter() {
        return mListFilter;
    }

    public void setListFilter(ListFilter listFilter) {
        mListFilter = listFilter;
    }

    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public String getRemotePath() {
        return mRemotePath;
    }

    public void setRemotePath(String remotePath) {
        mRemotePath = "";
        if (!remotePath.startsWith("/")) {
            mRemotePath += "/";
        }
        mRemotePath += remotePath;
        if (!remotePath.endsWith("/")) {
            mRemotePath += "/";
        }
    }

    public @AbstractSynchroClient.Direction
    int getDirection() {
        return mDirection;
    }

    public void setDirection(@AbstractSynchroClient.Direction int direction) {
        mDirection = direction;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    @Override
    public String toString() {
        return "FolderToSync{" +
                "mListFilter=" + mListFilter +
                ", mFile=" + mFile +
                ", mRemotePath='" + mRemotePath + '\'' +
                ", mDirection=" + mDirection +
                ", mDisplayName='" + mDisplayName + '\'' +
                '}';
    }
}
