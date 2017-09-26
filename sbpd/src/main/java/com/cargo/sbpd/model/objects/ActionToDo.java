package com.cargo.sbpd.model.objects;

import android.arch.persistence.room.ColumnInfo;

/**
 * Created by leb on 01/08/2017.
 *
 * // TODO: 01/08/2017 Les noms des colonnes ne correspondent pas vraiment à ceux indiqués par la requête via les alias. Les noms renseignés ici sont un workaround pour que la requête aboutisse.
 */
public class ActionToDo {
    @ColumnInfo(name = "actionType")
    private String mActionType;
    @ColumnInfo(name = "name")
    private String mName;
    @ColumnInfo(name = "size")
    private long mSize;
    @ColumnInfo(name = "lastModification")
    private long mLastModification;
    @ColumnInfo(name = "direction")
    private int mDirection;
    @ColumnInfo(name = "folderLocal")
    private String mFolderLocal;
    @ColumnInfo(name = "folderRemote")
    private String mFolderRemote;

    public String getActionType() {
        return mActionType;
    }

    public void setActionType(String actionType) {
        mActionType = actionType;
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
        return "ActionToDo{" +
                "mActionType='" + mActionType + '\'' +
                ", mName='" + mName + '\'' +
                ", mSize=" + mSize +
                ", mLastModification=" + mLastModification +
                ", mDirection=" + mDirection +
                ", mFolderLocal=" + mFolderLocal +
                ", mFolderRemote=" + mFolderRemote +
                '}';
    }
}
