package com.cargo.sbpd.model.objects;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by leb on 31/07/2017.
 */
@Entity(tableName = "T_ActionsDone")
public class ActionDone {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;

    @ColumnInfo(name = "localFilePath")
    private String mLocalFilePath;

    @ColumnInfo(name = "remoteFilePath")
    private String mRemoteFilePath;

    @ColumnInfo(name = "actionType")
    private String mActionType;

    @ColumnInfo(name = "network")
    private String mNetwork;

    @ColumnInfo(name = "synchroClient")
    private String mSynchroClient;

    @ColumnInfo(name = "destination") // Server or local
    private String mDestination;

    @ColumnInfo(name = "localFolder")
    private String mLocalFolder;

    @ColumnInfo(name = "remoteFolder")
    private String mRemoteFolder;

    @ColumnInfo(name = "size")
    private long mSize;

    @ColumnInfo(name = "lastModification")
    private long mLastModification;

    @ColumnInfo(name = "sizeTransferred")
    private long mSizeTransferred;

    @ColumnInfo(name = "startTransfert")
    private long mStartTransfert;

    @ColumnInfo(name = "endTransfert")
    private long mEndTransfert;

    @ColumnInfo(name = "exception")
    private String mException;

    @ColumnInfo(name = "isSample")
    private boolean mIsSample;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getLocalFilePath() {
        return mLocalFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        mLocalFilePath = localFilePath;
    }

    public String getRemoteFilePath() {
        return mRemoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        mRemoteFilePath = remoteFilePath;
    }

    public String getActionType() {
        return mActionType;
    }

    public void setActionType(String actionType) {
        mActionType = actionType;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public void setNetwork(String network) {
        mNetwork = network;
    }

    public String getSynchroClient() {
        return mSynchroClient;
    }

    public void setSynchroClient(String synchroClient) {
        mSynchroClient = synchroClient;
    }

    public String getDestination() {
        return mDestination;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public String getLocalFolder() {
        return mLocalFolder;
    }

    public void setLocalFolder(String localFolder) {
        mLocalFolder = localFolder;
    }

    public String getRemoteFolder() {
        return mRemoteFolder;
    }

    public void setRemoteFolder(String remoteFolder) {
        mRemoteFolder = remoteFolder;
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

    public long getSizeTransferred() {
        return mSizeTransferred;
    }

    public void setSizeTransferred(long sizeTransferred) {
        mSizeTransferred = sizeTransferred;
    }

    public long getStartTransfert() {
        return mStartTransfert;
    }

    public void setStartTransfert(long startTransfert) {
        mStartTransfert = startTransfert;
    }

    public long getEndTransfert() {
        return mEndTransfert;
    }

    public void setEndTransfert(long endTransfert) {
        mEndTransfert = endTransfert;
    }

    public String getException() {
        return mException;
    }

    public void setException(String exception) {
        mException = exception;
    }

    public boolean isSample() {
        return mIsSample;
    }

    public void setIsSample(boolean sample) {
        mIsSample = sample;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionDone that = (ActionDone) o;

        if (mId != that.mId) return false;
        if (mSize != that.mSize) return false;
        if (mLastModification != that.mLastModification) return false;
        if (mSizeTransferred != that.mSizeTransferred) return false;
        if (mStartTransfert != that.mStartTransfert) return false;
        if (mEndTransfert != that.mEndTransfert) return false;
        if (mIsSample != that.mIsSample) return false;
        if (mLocalFilePath != null ? !mLocalFilePath.equals(that.mLocalFilePath) : that.mLocalFilePath != null)
            return false;
        if (mRemoteFilePath != null ? !mRemoteFilePath.equals(that.mRemoteFilePath) : that.mRemoteFilePath != null)
            return false;
        if (mActionType != null ? !mActionType.equals(that.mActionType) : that.mActionType != null)
            return false;
        if (mNetwork != null ? !mNetwork.equals(that.mNetwork) : that.mNetwork != null)
            return false;
        if (mSynchroClient != null ? !mSynchroClient.equals(that.mSynchroClient) : that.mSynchroClient != null)
            return false;
        if (mDestination != null ? !mDestination.equals(that.mDestination) : that.mDestination != null)
            return false;
        if (mLocalFolder != null ? !mLocalFolder.equals(that.mLocalFolder) : that.mLocalFolder != null)
            return false;
        if (mRemoteFolder != null ? !mRemoteFolder.equals(that.mRemoteFolder) : that.mRemoteFolder != null)
            return false;
        return mException != null ? mException.equals(that.mException) : that.mException == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + (mLocalFilePath != null ? mLocalFilePath.hashCode() : 0);
        result = 31 * result + (mRemoteFilePath != null ? mRemoteFilePath.hashCode() : 0);
        result = 31 * result + (mActionType != null ? mActionType.hashCode() : 0);
        result = 31 * result + (mNetwork != null ? mNetwork.hashCode() : 0);
        result = 31 * result + (mSynchroClient != null ? mSynchroClient.hashCode() : 0);
        result = 31 * result + (mDestination != null ? mDestination.hashCode() : 0);
        result = 31 * result + (mLocalFolder != null ? mLocalFolder.hashCode() : 0);
        result = 31 * result + (mRemoteFolder != null ? mRemoteFolder.hashCode() : 0);
        result = 31 * result + (int) (mSize ^ (mSize >>> 32));
        result = 31 * result + (int) (mLastModification ^ (mLastModification >>> 32));
        result = 31 * result + (int) (mSizeTransferred ^ (mSizeTransferred >>> 32));
        result = 31 * result + (int) (mStartTransfert ^ (mStartTransfert >>> 32));
        result = 31 * result + (int) (mEndTransfert ^ (mEndTransfert >>> 32));
        result = 31 * result + (mException != null ? mException.hashCode() : 0);
        result = 31 * result + (mIsSample ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ActionDone{" +
                "mId=" + mId +
                ", mLocalFilePath='" + mLocalFilePath + '\'' +
                ", mRemoteFilePath='" + mRemoteFilePath + '\'' +
                ", mActionType='" + mActionType + '\'' +
                ", mNetwork='" + mNetwork + '\'' +
                ", mSynchroClient='" + mSynchroClient + '\'' +
                ", mDestination='" + mDestination + '\'' +
                ", mLocalFolder='" + mLocalFolder + '\'' +
                ", mRemoteFolder='" + mRemoteFolder + '\'' +
                ", mSize=" + mSize +
                ", mLastModification=" + mLastModification +
                ", mSizeTransferred=" + mSizeTransferred +
                ", mStartTransfert=" + mStartTransfert +
                ", mEndTransfert=" + mEndTransfert +
                ", mException='" + mException + '\'' +
                ", mIsSample=" + mIsSample +
                '}';
    }
}
