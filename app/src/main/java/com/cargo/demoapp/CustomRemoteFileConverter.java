package com.cargo.demoapp;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.sync.filelisting.AbstractRemoteConverter;
import com.cargo.sbpd.utils.FormatUtils;

/**
 * Created by leb on 24/08/2017.
 */

public class CustomRemoteFileConverter extends AbstractRemoteConverter {


    @Override
    public RemoteFile buildRemoteFile(String absolutePathFtp, long size, long lastModification, FolderToSync folderToSync, int direction) {
        String relativePath = absolutePathFtp.substring(folderToSync.getRemotePath().length() - 1); // Keep the "/"
        return new RemoteFile(relativePath, size, lastModification, folderToSync.getFile().getAbsolutePath(), folderToSync.getRemotePath(), direction);
    }

    @Override
    public String getPath(FolderToSync folder, LocalFile local) {
        if (local.getDirection() == AbstractSynchroClient.Direction.LOCAL_TO_REMOTE) {
            return FormatUtils.addSlashes(folder.getRemotePath(), local.getName()) + "b";
        }
        return FormatUtils.addSlashes(folder.getRemotePath(), local.getName());
    }

    @Override
    public String getPath(FolderToSync folder, RemoteFile remote) {
        return FormatUtils.addSlashes(folder.getRemotePath(), remote.getName());
    }
}
