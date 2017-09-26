package com.cargo.sbpd.sync.filelisting;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;

import java.io.File;

/**
 * Created by leb on 22/08/2017.
 * This class is intended to convert something into a File or LocalFile (local files on the tablet)
 *
 * Can't use static methods to let business apps extend it.
 * This is the default implementation of AbstractLocalConverter.
 */

public class LocalConverter extends AbstractLocalConverter {

    @Override
    public LocalFile convertToRoomFiles(FolderToSync origin, File file) {
        int prefix = origin.getFile().getAbsolutePath().length();
        return new LocalFile(file.getAbsolutePath().substring(prefix),
                file.length(),
                file.lastModified(),
                origin.getFile().getAbsolutePath(),
                origin.getRemotePath(),
                origin.getDirection());
    }

    @Override
    public File getLocalFile(FolderToSync folder, RemoteFile remote) {
        return new File(folder.getFile(), remote.getName());
    }

    @Override
    public File getLocalFile(FolderToSync folder, LocalFile local) {
        return new File(folder.getFile(), local.getName());
    }
}
