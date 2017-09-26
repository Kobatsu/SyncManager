package com.cargo.sbpd.sync.filelisting;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;

/**
 * Created by leb on 24/08/2017.
 */

public abstract class AbstractRemoteConverter {

    public abstract RemoteFile buildRemoteFile(String absolutePathFtp, long size, long lastModification, FolderToSync folderToSync, int direction);

    public abstract String getPath(FolderToSync folder, LocalFile local);

    public abstract String getPath(FolderToSync folder, RemoteFile remote);
}
