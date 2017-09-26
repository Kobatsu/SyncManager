package com.cargo.sbpd.model.objects;

import android.arch.persistence.room.ColumnInfo;

/**
 * Created by leb on 08/08/2017.
 */

public class ClientAndNetworkInfo {
    @ColumnInfo(name = "nbActions")
    private long mNbActions;
    @ColumnInfo(name = "sizeTransferredSum")
    private long mSizeTransferredSum;

    public long getNbActions() {
        return mNbActions;
    }

    public void setNbActions(long nbActions) {
        mNbActions = nbActions;
    }

    public long getSizeTransferredSum() {
        return mSizeTransferredSum;
    }

    public void setSizeTransferredSum(long sizeTransferredSum) {
        mSizeTransferredSum = sizeTransferredSum;
    }
}
