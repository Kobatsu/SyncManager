package com.cargo.sbpd.model.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.support.annotation.StringDef;

import com.cargo.sbpd.model.objects.ActionToDo;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.SizeAndNumber;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by leb on 28/07/2017.
 */
@Dao
public interface LocalFileDao {


    @StringDef({LocalFileDao.ActionType.DOWNLOAD, LocalFileDao.ActionType.UPLOAD, LocalFileDao.ActionType.DELETE})
    @Retention(RetentionPolicy.SOURCE)
    @interface ActionType {
        String DOWNLOAD = "download";
        String UPLOAD = "upload";
        String DELETE = "delete";
    }

    @StringDef({LocalFileDao.Destination.LOCAL, LocalFileDao.Destination.REMOTE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Destination {
        String LOCAL = "local";
        String REMOTE = "remote";
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocalFile localFile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LocalFile> localFiles);


    @Query("DELETE FROM T_LocalFiles WHERE name = :name")
    int delete(String name);

    @Query("DELETE FROM T_LocalFiles")
    int deleteAll();


    @Query("SELECT T_LocalFiles.name, T_LocalFiles.size, T_LocalFiles.lastModification, T_LocalFiles.folderLocal, T_LocalFiles.folderRemote, T_LocalFiles.direction FROM T_LocalFiles\n" +
            "LEFT JOIN T_RemoteFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            "AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_RemoteFiles.name IS NULL AND T_LocalFiles.folderLocal = :folderLocal AND T_LocalFiles.folderRemote = :folderRemote" +
            " AND T_LocalFiles.size != 0")
    List<LocalFile> getFilesOnTabletNotServer(String folderLocal, String folderRemote);

    @Query("SELECT CASE T_LocalFiles.direction WHEN 0 THEN  'upload' WHEN 1 THEN 'delete' ELSE 'fusion' END AS actionType, " +
            "T_LocalFiles.name AS name, T_LocalFiles.folderLocal AS folderLocal, T_LocalFiles.folderRemote AS folderRemote," +
            "  T_LocalFiles.direction AS direction,  T_LocalFiles.size AS size, T_LocalFiles.lastModification AS lastModification FROM T_LocalFiles\n" +
            "LEFT JOIN T_RemoteFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_RemoteFiles.name IS NULL\n" +
            "\n" +
            "UNION\n" +
            "\n" +
            "SELECT  CASE T_RemoteFiles.direction WHEN 0 THEN  'delete' WHEN 1 THEN 'download' ELSE 'fusion' END AS actionType," +
            " T_RemoteFiles.name AS name, T_LocalFiles.folderLocal AS folderLocal, T_LocalFiles.folderRemote AS folderRemote," +
            " T_RemoteFiles.direction AS direction, T_RemoteFiles.size AS size, T_RemoteFiles.lastModification AS lastModification FROM T_RemoteFiles\n" +
            "LEFT JOIN T_LocalFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_LocalFiles.name IS NULL\n" +
            "\n" +
            "ORDER BY direction ASC, actionType ASC, folderLocal ASC, name ASC")
    LiveData<List<ActionToDo>> getActionsToDo();

    //region update progression
    @Query("SELECT COUNT(*) AS number, SUM(size) AS size FROM (SELECT T_LocalFiles.size AS size FROM T_LocalFiles\n" +
            "LEFT JOIN T_RemoteFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_RemoteFiles.name IS NULL AND T_LocalFiles.direction = 1 AND T_LocalFiles.folderLocal = :folderLocal AND T_LocalFiles.folderRemote = :folderRemote)")
    SizeAndNumber getNbAndSizeDeleteTodoRemoteToLocal(String folderLocal, String folderRemote);

    @Query("SELECT COUNT(*) AS number, SUM(size) AS size FROM (SELECT T_RemoteFiles.size AS size FROM T_RemoteFiles\n" +
            "LEFT JOIN T_LocalFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_LocalFiles.name IS NULL AND T_RemoteFiles.direction = 0 AND  T_RemoteFiles.folderLocal = :folderLocal AND T_RemoteFiles.folderRemote = :folderRemote)")
    SizeAndNumber getNbAndSizeDeleteTodoLocalToRemote(String folderLocal, String folderRemote);

    @Query("SELECT COUNT(*) AS number, SUM(size) AS size FROM (SELECT T_LocalFiles.size AS size FROM T_LocalFiles\n" +
            "LEFT JOIN T_RemoteFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_RemoteFiles.name IS NULL AND T_LocalFiles.direction = 1 AND  T_LocalFiles.folderLocal = :folderLocal AND T_LocalFiles.folderRemote = :folderRemote)")
    SizeAndNumber getNbAndSizeDownloadTodo(String folderLocal, String folderRemote);

    @Query("SELECT COUNT(*) AS number, SUM(size) AS size FROM (SELECT T_RemoteFiles.size AS size FROM T_RemoteFiles\n" +
            "LEFT JOIN T_LocalFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_LocalFiles.name IS NULL AND T_RemoteFiles.direction = 1 AND T_RemoteFiles.folderLocal = :folderLocal AND T_RemoteFiles.folderRemote = :folderRemote)")
    SizeAndNumber getNbAndSizeUploadTodo(String folderLocal, String folderRemote);
    //endregion

    @Query("SELECT SUM(size) FROM (\n" +
            "SELECT  T_LocalFiles.size AS size FROM T_LocalFiles\n" +
            "LEFT JOIN T_RemoteFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_RemoteFiles.name IS NULL AND T_LocalFiles.folderLocal = :folderLocal AND T_LocalFiles.folderRemote = :folderRemote\n" +
            "\n" +
            "UNION\n" +
            "\n" +
            "SELECT  T_RemoteFiles.size AS size FROM T_RemoteFiles\n" +
            "LEFT JOIN T_LocalFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            " AND T_LocalFiles.folderLocal = T_RemoteFiles.folderLocal AND T_LocalFiles.folderRemote = T_RemoteFiles.folderRemote\n" +
            "WHERE T_LocalFiles.name IS NULL AND T_RemoteFiles.folderLocal = :folderLocal AND T_RemoteFiles.folderRemote = :folderRemote\n" +
            ")")
    long getSizeFolder(String folderLocal, String folderRemote);




}
