package com.cargo.sbpd.model.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.cargo.sbpd.model.objects.ListingLog;
import com.cargo.sbpd.model.objects.ListingStats;

/**
 * Created by leb on 29/08/2017.
 */
@Dao
public interface ListingLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ListingLog listing);

    // TODO verify query
    @Query("SELECT SUM(nbListingReussis) AS nbListingReussis, SUM(nbFilesTotal) AS nbFilesTotal," +
            "SUM(timeTotal) AS timeTotal, SUM(nbListingFails) AS nbListingFails, SUM(lastTime) AS lastTime FROM (" +
            "SELECT COUNT(*) AS nbListingReussis, SUM(nbFiles) AS nbFilesTotal," +
            " SUM(timeEnd-timeStart) AS timeTotal, 0 AS nbListingFails, MAX(timeStart) AS lastTime" +
            " FROM T_ListingLog\n" +
            "WHERE timeEnd IS NOT NULL AND client= :clientName AND network = :network\n" +
            "UNION\n" +
            "SELECT 0 AS nbListingReussis, 0 AS nbFilesTotal," +
            " 0 AS timeTotal, COUNT(*) AS nbListingFails, 0 AS lastTime" +
            " FROM T_ListingLog " +
            "WHERE timeEnd IS NULL AND client= :clientName AND network = :network)")
    ListingStats getAll(String clientName, String network);
}
