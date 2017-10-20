package com.cargo.demoapp;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.sync.filelisting.AbstractLocalConverter;

import java.io.File;

/**
 * Created by leb on 09/10/2017.
 */


public class CustomLocalFileConverter2 extends AbstractLocalConverter {

    private final String mPathFolderPhotos;
    private final String mNameFolderPhotos;

    public CustomLocalFileConverter2(String pathFolderPhotos, String nameFolderPhotos) {
        mPathFolderPhotos = pathFolderPhotos;
        mNameFolderPhotos = nameFolderPhotos;
    }

    @Override
    public LocalFile convertToRoomFiles(FolderToSync origin, File file) {
        String path;
        int prefix = origin.getFile().getAbsolutePath().length();
        if (origin.getRemotePath().contains(mNameFolderPhotos)) {
            String societe = origin.getFile().getParentFile().getName();
            path = String.format(mPathFolderPhotos, societe, file.getName()).substring(prefix);
            // todo real path
            path = file.getAbsolutePath().substring(new File(origin.getFile(), buildRelativePathPhotos(file.getName())).getParentFile().getAbsolutePath().length());
            if (countOccurrences(path, '/') != 1) {
                path = file.getAbsolutePath().substring(origin.getFile().getAbsolutePath().length());
            }
        } else {
            path = file.getAbsolutePath().substring(prefix);
        }
        return new LocalFile(path,
                file.length(),
                file.lastModified(),
                origin.getFile().getAbsolutePath(),
                origin.getRemotePath(),
                origin.getDirection());
    }

    @Override
    public File getLocalFile(FolderToSync folder, RemoteFile remote) {
        if (folder.getRemotePath().contains(mNameFolderPhotos)) {
            return new File(folder.getFile(), buildRelativePathPhotos(remote.getName()));
        } else {
            return new File(folder.getFile(), remote.getName());
        }
    }

    @Override
    public File getLocalFile(FolderToSync folder, LocalFile local) {
        if (folder.getRemotePath().contains(mNameFolderPhotos)) {
            if(countOccurrences(local.getName(), '/') != 1){
                return new File(folder.getFile(), local.getName());
            }else{
                return new File(folder.getFile(), buildRelativePathPhotos(local.getName()));
            }
        } else {
            return new File(folder.getFile(), local.getName());
        }
    }

    public static String buildRelativePathPhotos(String name) {
        StringBuilder result = new StringBuilder();
        String numArt = name.split("_")[0];
        boolean added = false;
        for (int j = 0; j < numArt.length() - 2; j++) {
            char character = numArt.charAt(j);
            if (character == ' ') {
                continue;
            }
            if (added) {
                result.append("/");
            }
            result.append(character);
            added = true;
        }

        result.append("/");
        return result.append(name).toString();
    }

    private static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }
}