package com.cargo.sbpd.model.objects;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

import com.cargo.sbpd.sync.clients.AbstractSynchroClient;

/**
 * Created by leb on 28/07/2017.
 *
 * name = /sport 01.jpg
 * size = 12342
 * lastModification = 42305723
 * folderToSync = /storage/emulated/0/SyncProtocol/Test/Sport
 * direction = download
 *
 * String path = origin.getRemotePath() + name; // folderToSync là pour la jointure
 */
@Entity(tableName = "T_RemoteFiles", primaryKeys = {"name", "folderLocal", "folderRemote"})
public class RemoteFile {
    @ColumnInfo(name = "name")
    private String mName;

    @ColumnInfo(name = "size")
    private long mSize;

    @ColumnInfo(name = "lastModification")
    private long mLastModification;

    @ColumnInfo(name = "folderLocal")
    private String mFolderLocal;

    @ColumnInfo(name = "folderRemote")
    private String mFolderRemote;

    @ColumnInfo(name = "direction")
    private @AbstractSynchroClient.Direction
    int mDirection;

    public RemoteFile(String name, long size, long lastModification, String folderLocal,
                      String folderRemote, @AbstractSynchroClient.Direction int direction) {
        mName = name;
        mSize = size;
        mLastModification = lastModification;
        mFolderLocal = folderLocal;
        mFolderRemote = folderRemote;
        mDirection = direction;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public long getLastModification() {
        return mLastModification;
    }

    public void setLastModification(long lastModification) {
        mLastModification = lastModification;
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int direction) {
        mDirection = direction;
    }

    public String getFolderLocal() {
        return mFolderLocal;
    }

    public void setFolderLocal(String folderLocal) {
        mFolderLocal = folderLocal;
    }

    public String getFolderRemote() {
        return mFolderRemote;
    }

    public void setFolderRemote(String folderRemote) {
        mFolderRemote = folderRemote;
    }

    @Override
    public String toString() {
        return "RemoteFile{" +
                "mName='" + mName + '\'' +
                ", mSize=" + mSize +
                ", mLastModification=" + mLastModification +
                ", mFolderLocal='" + mFolderLocal + '\'' +
                ", mFolderRemote='" + mFolderRemote + '\'' +
                ", mDirection=" + mDirection +
                '}';
    }
}
