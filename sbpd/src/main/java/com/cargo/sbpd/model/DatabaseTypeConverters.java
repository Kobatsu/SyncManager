package com.cargo.sbpd.model;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by leb on 27/07/2017.
 */

public class DatabaseTypeConverters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

//    @TypeConverter
//    public static boolean fromLong(Long value) {
//        return value != null && value == 1;
//    }
//
//    @TypeConverter
//    public static Long longFromBoolean(boolean value) {
//        return value ? 1L : 0L;
//    }


}