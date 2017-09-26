package com.cargo.sbpd.model.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.ClientAndNetworkInfo;

import java.util.Date;
import java.util.List;

/**
 * Created by leb on 04/08/2017.
 */
@Dao
public interface ActionDoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ActionDone actionDone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ActionDone> actionDones);


    @Query("DELETE FROM T_ActionsDone WHERE id = :id")
    int delete(long id);

    @Query("DELETE FROM T_ActionsDone")
    int deleteAll();

    @Update
    void update(ActionDone actionDone);

    @Query("SELECT * FROM T_ActionsDone  WHERE T_ActionsDone.startTransfert >= :date ORDER BY startTransfert DESC")
    LiveData<List<ActionDone>> getAllFromDate(Date date);

    @Query("SELECT * FROM T_ActionsDone ORDER BY startTransfert DESC")
    LiveData<List<ActionDone>> getAll();


    @Query("SELECT * FROM T_ActionsDone WHERE T_ActionsDone.startTransfert >= :date AND T_ActionsDone.endTransfert = 0 ORDER BY T_ActionsDone.startTransfert DESC LIMIT 1")
    LiveData<List<ActionDone>> getLastNotFinished(Date date);

    @Query("SELECT * FROM T_ActionsDone WHERE T_ActionsDone.endTransfert != 0 ORDER BY T_ActionsDone.startTransfert DESC LIMIT 1")
    LiveData<List<ActionDone>> getLastFinished();


    @Query("SELECT sizeTransferred FROM T_ActionsDone  WHERE T_ActionsDone.startTransfert >= :date ORDER BY startTransfert DESC LIMIT 1")
    long getSizeTransferredLastAction(Date date);

    //region statistics
    @Query("SELECT COUNT() AS nbActions,\n" +
            "       SUM(T_ActionsDone.sizeTransferred) AS sizeTransferredSum  FROM T_ActionsDone\n" +
            " WHERE T_ActionsDone.synchroClient = :client AND \n" +
            "       T_ActionsDone.network = :network AND\n" +
            "       T_ActionsDone.endTransfert > :date\n")
    ClientAndNetworkInfo getNbActionsAndSizeTransferred(String client, String network, long date);

    @Query("SELECT  CAST(SUM(T_ActionsDone.sizeTransferred) AS REAL) / SUM(T_ActionsDone.endTransfert-T_ActionsDone.startTransfert)\n" +
            "       FROM T_ActionsDone\n" +
            "WHERE T_ActionsDone.synchroClient = :client AND\n" +
            "       T_ActionsDone.network = :network AND\n" +
            "       T_ActionsDone.endTransfert > :date AND\n" +
            "       T_ActionsDone.actionType = :actionType AND \n" +
            "       T_ActionsDone.isSample = :basedOnSample")
    float getSpeed(String client, String network, long date, String actionType, int basedOnSample);

    @Query("SELECT COUNT() \n" +
            "       FROM T_ActionsDone\n" +
            "WHERE T_ActionsDone.synchroClient = :client AND \n" +
            "       T_ActionsDone.network = :network AND \n" +
            "       T_ActionsDone.endTransfert > :timeInMillis AND \n" +
            "       T_ActionsDone.size == T_ActionsDone.sizeTransferred AND \n" +
            "       T_ActionsDone.actionType = :download AND " +
            "       T_ActionsDone.isSample = :basedOnSample")
    int getNbFilesSuccess(String client, String network, long timeInMillis, String download, int basedOnSample);

    @Query("SELECT COUNT() \n" +
            "       FROM T_ActionsDone\n" +
            "WHERE T_ActionsDone.synchroClient = :client AND \n" +
            "       T_ActionsDone.network = :network AND \n" +
            "       T_ActionsDone.endTransfert > :timeInMillis AND \n" +
            "       T_ActionsDone.size != T_ActionsDone.sizeTransferred AND \n" +
            "       T_ActionsDone.actionType = :download AND \n" +
            "       T_ActionsDone.isSample = :basedOnSample")
    int getNbFilesError(String client, String network, long timeInMillis, String download, int basedOnSample);
    //endregion
}
