package com.cargo.sbpd.model.objects;

import android.arch.persistence.room.ColumnInfo;
import android.content.Context;
import android.text.format.Formatter;

/**
 * Created by Sébastien on 12/08/2017.
 */

public class SizeAndNumber {
    @ColumnInfo(name = "number")
    private long mNumber;
    @ColumnInfo(name = "size")
    private long mSize;

    public long getNumber() {
        return mNumber;
    }

    public void setNumber(long number) {
        mNumber = number;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getDisplayString(Context context) {
        if (mNumber > 0) {
            return mNumber + " (" + Formatter.formatFileSize(context, mSize) + ")";
        } else {
            return "-";
        }
    }

    @Override
    public String toString() {
        return "SizeAndNumber{" +
                "mNumber=" + mNumber +
                ", mSize=" + mSize +
                '}';
    }
}
