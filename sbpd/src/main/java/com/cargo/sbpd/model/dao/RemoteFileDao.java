package com.cargo.sbpd.model.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;

import java.util.List;

/**
 * Created by leb on 28/07/2017.
 */
@Dao
public interface RemoteFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RemoteFile remoteFile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RemoteFile> roomFiles);

    /**
     * get the files on server not tablet (doesn't include empty directories)
     *
     * @return
     */
    @Query("SELECT T_RemoteFiles.name, T_RemoteFiles.size, T_RemoteFiles.lastModification, T_RemoteFiles.folderLocal, T_RemoteFiles.folderRemote, T_RemoteFiles.direction FROM T_RemoteFiles\n" +
            "LEFT JOIN T_LocalFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            "AND T_RemoteFiles.folderLocal = T_LocalFiles.folderLocal AND T_RemoteFiles.folderRemote = T_LocalFiles.folderRemote\n" +
            "WHERE T_LocalFiles.name IS NULL AND T_RemoteFiles.folderLocal = :folderLocal AND T_RemoteFiles.folderRemote = :folderRemote" +
            " AND T_RemoteFiles.size != 0")
    List<RemoteFile> getFilesOnServerNotTablet(String folderLocal, String folderRemote);

    /**
     * get the empty directories on server not tablet
     *
     * @return
     */
    @Query("SELECT T_RemoteFiles.name, T_RemoteFiles.size, T_RemoteFiles.lastModification, T_RemoteFiles.folderLocal, T_RemoteFiles.folderRemote, T_RemoteFiles.direction FROM T_RemoteFiles\n" +
            "LEFT JOIN T_LocalFiles ON T_LocalFiles.name = T_RemoteFiles.name AND T_LocalFiles.size = T_RemoteFiles.size\n" +
            "AND T_RemoteFiles.folderLocal = T_LocalFiles.folderLocal AND T_RemoteFiles.folderRemote = T_LocalFiles.folderRemote\n" +
            "WHERE T_LocalFiles.name IS NULL AND T_RemoteFiles.folderLocal = :folderLocal AND T_RemoteFiles.folderRemote = :folderRemote" +
            " AND T_RemoteFiles.size = 0")
    List<RemoteFile> getEmptyFoldersOnServerNotTablet(String folderLocal, String folderRemote);


    @Query("DELETE FROM T_RemoteFiles WHERE name = :name AND folderLocal = :folderLocal AND folderRemote = :folderRemote")
    int delete(String name, String folderLocal, String folderRemote);

    @Query("DELETE FROM T_RemoteFiles")
    int deleteAll();
}