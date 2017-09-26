package com.cargo.sbpd.model.objects;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import com.cargo.sbpd.sync.clients.AbstractSynchroClient;

/**
 * Created by leb on 29/08/2017.
 */

public class ListingStats {
    @ColumnInfo(name = "nbListingReussis")
    private long mNbListringReussis;
    @ColumnInfo(name = "nbFilesTotal")
    private long mNbFilesTotal;
    @ColumnInfo(name = "timeTotal")
    private long mTimeTotal;
    @ColumnInfo(name = "nbListingFails")
    private long mNbListingFails;
    @ColumnInfo(name = "lastTime")
    private long mLastTime;

    @Ignore
    private AbstractSynchroClient mClient;

    public long getNbListringReussis() {
        return mNbListringReussis;
    }

    public void setNbListringReussis(long nbListringReussis) {
        mNbListringReussis = nbListringReussis;
    }

    public long getNbFilesTotal() {
        return mNbFilesTotal;
    }

    public void setNbFilesTotal(long nbFilesTotal) {
        mNbFilesTotal = nbFilesTotal;
    }

    public long getTimeTotal() {
        return mTimeTotal;
    }

    public void setTimeTotal(long timeTotal) {
        mTimeTotal = timeTotal;
    }

    public long getNbListingFails() {
        return mNbListingFails;
    }

    public void setNbListingFails(long nbListingFails) {
        mNbListingFails = nbListingFails;
    }

    public long getLastTime() {
        return mLastTime;
    }

    public void setLastTime(long lastTime) {
        mLastTime = lastTime;
    }

    public AbstractSynchroClient getClient() {
        return mClient;
    }

    public void setClient(AbstractSynchroClient client) {
        mClient = client;
    }

    @Override
    public String toString() {
        return "ListingStats{" +
                "mNbListringReussis=" + mNbListringReussis +
                ", mNbFilesTotal=" + mNbFilesTotal +
                ", mTimeTotal=" + mTimeTotal +
                ", mNbListingFails=" + mNbListingFails +
                ", mLastTime=" + mLastTime +
                '}';
    }
}
