package com.cargo.sbpd.sync.filelisting;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leb on 24/08/2017.
 */

public abstract class AbstractLocalConverter {

    public abstract LocalFile convertToRoomFiles(FolderToSync origin, File file);

    public abstract File getLocalFile(FolderToSync folder, RemoteFile remote);

    public abstract File getLocalFile(FolderToSync folder, LocalFile local);

    public List<LocalFile> convertToRoomFiles(FolderToSync folder, List<File> files) {
        List<LocalFile> roomFiles = new ArrayList<>();
        for (File f : files) {
            roomFiles.add(convertToRoomFiles(folder, f));
        }
        return roomFiles;
    }
}
