package com.cargo.sbpd.model.objects;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

/**
 * Created by leb on 29/08/2017.
 */
@Entity(tableName = "T_ListingLog", primaryKeys = {"client", "network", "timeStart"})
public class ListingLog {

    @ColumnInfo(name = "client")
    private String mClient;
    @ColumnInfo(name = "network")
    private String mNetwork;
    @ColumnInfo(name = "timeStart")
    private long mTimeStart;
    @ColumnInfo(name = "timeEnd")
    private long mTimeEnd;
    @ColumnInfo(name = "nbFiles")
    private long mNbFiles;

    public String getClient() {
        return mClient;
    }

    public void setClient(String client) {
        mClient = client;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public void setNetwork(String network) {
        mNetwork = network;
    }

    public long getTimeStart() {
        return mTimeStart;
    }

    public void setTimeStart(long timeStart) {
        mTimeStart = timeStart;
    }

    public long getTimeEnd() {
        return mTimeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        mTimeEnd = timeEnd;
    }

    public long getNbFiles() {
        return mNbFiles;
    }

    public void setNbFiles(long nbFiles) {
        mNbFiles = nbFiles;
    }

    @Override
    public String toString() {
        return "ListingLog{" +
                "mClient='" + mClient + '\'' +
                ", mNetwork='" + mNetwork + '\'' +
                ", mTimeStart=" + mTimeStart +
                ", mTimeEnd=" + mTimeEnd +
                ", mNbFiles=" + mNbFiles +
                '}';
    }
}
