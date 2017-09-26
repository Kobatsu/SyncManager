package com.cargo.demoapp;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.sync.filelisting.AbstractLocalConverter;

import java.io.File;

/**
 * Created by leb on 24/08/2017.
 */

public class CustomLocalFileConverter extends AbstractLocalConverter {
    @Override
    public LocalFile convertToRoomFiles(FolderToSync origin, File file) {
        int prefix = origin.getFile().getAbsolutePath().length();
        return new LocalFile(file.getAbsolutePath().substring(prefix, file.getAbsolutePath().length()-1),
                file.length(),
                file.lastModified(),
                origin.getFile().getAbsolutePath(),
                origin.getRemotePath(),
                origin.getDirection());
    }

    @Override
    public File getLocalFile(FolderToSync folder, RemoteFile remote) {
        return new File(folder.getFile(), remote.getName() + "a");
    }

    @Override
    public File getLocalFile(FolderToSync folder, LocalFile local) {
        return new File(folder.getFile(), local.getName() + "a");
    }
}
