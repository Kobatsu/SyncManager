package com.cargo.sbpd.sync.filelisting;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.utils.FormatUtils;

/**
 * Created by leb on 22/08/2017.
 *
 * This class is intended to convert something into a Path or RemoteFile (remote files on server)
 *
 * Can't use static methods to let business apps extend it.
 * This is the default AbstractRemoteConverter.
 */

public class RemoteConverter extends AbstractRemoteConverter {

    @Override
    public String getPath(FolderToSync folder, LocalFile local) {
        return FormatUtils.addSlashes(folder.getRemotePath(), local.getName());
    }

    @Override
    public String getPath(FolderToSync folder, RemoteFile remote) {
        return FormatUtils.addSlashes(folder.getRemotePath(), remote.getName());
    }

    @Override
    public RemoteFile buildRemoteFile(String absolutePathFtp, long size, long lastModification, FolderToSync folderToSync, int direction) {
        String relativePath = absolutePathFtp.substring(folderToSync.getRemotePath().length()-1); // Keep the "/"
        return new RemoteFile(relativePath, size, lastModification, folderToSync.getFile().getAbsolutePath(), folderToSync.getRemotePath(), direction);
    }
}
