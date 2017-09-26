package com.cargo.synchroftpapache;

import android.util.Log;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.sync.filelisting.ListFilter;
import com.cargo.sbpd.utils.FormatUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leb on 18/07/2017.
 */

public class ApacheFTPClient extends AbstractSynchroClient {

    protected final String mAdress;
    protected final String mUser;
    protected final String mPassWord;
    protected FTPClient mFtpClient;

    public ApacheFTPClient(String adress, String user, String password) {
        // make the connexion and hold it
        // attach the copyStreamListener and make it publish to the bus
        mAdress = adress;
        mUser = user;
        mPassWord = password;
        connect();
    }

    /**
     * Connects to the FTP
     *
     * @return
     */
    public boolean connect() {
        boolean result = false;
        try {
            mFtpClient = new FTPClient();
            mFtpClient.setControlEncoding("UTF-8");
            mFtpClient.connect(InetAddress.getByName(mAdress));
            mFtpClient.login(mUser, mPassWord);
            mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
            mFtpClient.enterLocalPassiveMode();
            mFtpClient.setCopyStreamListener(new CopyStreamListener() {
                @Override
                public void bytesTransferred(CopyStreamEvent event) {
                }

                @Override
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                    onDataTransferred(totalBytesTransferred);
                }
            });
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean disconnect() {
        try {
            mFtpClient.logout();
            mFtpClient.disconnect();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RemoteFile> listFilesAndEmptyDirectoriesRecursively(String path, FolderToSync folderLocal) throws IOException {
        ListFilter listFilter = folderLocal.getListFilter();
        List<RemoteFile> list = new ArrayList<>();
        FTPFile[] listFiles = mFtpClient.listFiles(path);
        if (path.equals("/")) {
            path = "";
        }
        if (listFiles != null) {
            for (FTPFile file : listFiles) {
                if (isCancelled()) {
                    break;
                }
                if (file.isFile()) {
                    if (listFilter == null || listFilter.acceptRemote(FormatUtils.addSlashes(path, file.getName()))) {
                        list.add(mRemoteFileConverter.buildRemoteFile(FormatUtils.addSlashes(path, file.getName()),
                                file.getSize(),
                                file.getTimestamp().getTimeInMillis(),
                                folderLocal,
                                folderLocal.getDirection()));
                    }


                }
                if (file.isDirectory()) {
                    List<RemoteFile> fileList = listFilesAndEmptyDirectoriesRecursively(FormatUtils.addSlash(path) + file.getName(), folderLocal);

                    if (fileList.size() > 0) {
                        list.addAll(fileList);
                    } else {
                        if (listFilter == null || listFilter.acceptRemote(FormatUtils.addSlashes(path, file.getName()))) {
                            list.add(mRemoteFileConverter.buildRemoteFile(FormatUtils.addSlash(path) + file.getName(), file.getSize(), file.getTimestamp().getTimeInMillis(), folderLocal, folderLocal.getDirection()));
                        }
                    }
                }
            }
        }
        return list;
    }

    //region upload
    @Override
    public boolean uploadWithoutLogs(File file, String pathFtp) throws IOException {
        boolean result = false;
        if (!mFtpClient.isConnected()) {
            connect();
        }
        file.getParentFile().mkdirs();
        if (pathFtp.contains("/")) {
            ftpCreateDirectoryTree(pathFtp.substring(0, pathFtp.lastIndexOf("/")));
        }
        Log.d("UploadWithoutLogs", "file : " + file.getAbsolutePath() + " - path : " + pathFtp);
        FileInputStream input = new FileInputStream(file);
        result = mFtpClient.storeFile(pathFtp, input);
        input.close();
        return result;
    }

    /**
     * utility to create an arbitrary directory hierarchy on the remote ftp server
     *
     * @param dirTree the directory tree only delimited with / chars.  No file name!
     * @throws Exception
     */
    protected void ftpCreateDirectoryTree(String dirTree) {
        try {
            boolean dirExists = true;
            String originalDir = mFtpClient.printWorkingDirectory();
            //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
            String[] directories = dirTree.split("/");
            for (String dir : directories) {
                if (!dir.isEmpty()) {
                    if (dirExists) {
                        dirExists = mFtpClient.changeWorkingDirectory(dir);
                    }
                    if (!dirExists) {
                        if (!mFtpClient.makeDirectory(dir)) {
                            throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + mFtpClient.getReplyString() + "'");
                        }
                        if (!mFtpClient.changeWorkingDirectory(dir)) {
                            throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + mFtpClient.getReplyString() + "'");
                        }
                    }
                }
            }
            mFtpClient.changeWorkingDirectory(originalDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region download
    @Override
    public boolean downloadWithoutLogs(File file, String pathFtp) throws IOException {
        boolean result = false;
        if (!mFtpClient.isConnected()) {
            connect();
        }
        file.getParentFile().mkdirs();

        FileOutputStream output = new FileOutputStream(file);
        try {
            result = mFtpClient.retrieveFile(pathFtp, output);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        output.close();
        return result;
    }
    //endregion


    //region delete
    @Override
    public void deleteListOfFiles(FolderToSync folder, List<RemoteFile> files) throws IOException {
        for (RemoteFile remote : files) {
            if (isCancelled()) {
                break;
            }
            boolean result = delete(folder, remote.getName());
            Log.d("ApacheFTPClient", remote.getName() + " delete success = " + result);
        }
    }

    @Override
    public boolean deleteWithoutLogs(String path) throws IOException {
        boolean result = mFtpClient.deleteFile(path);
        return result;
    }
    //endregion


    @Override
    public boolean isResumeSupported() {
        return false;
    }


    @Override
    public int directionSupported() {
        return Direction.REMOTE_TO_LOCAL;
    }

    @Override
    public void onDataTransferred(long totalBytesTransferred) {
        if (mActionDone != null) {
            mActionDone.setSizeTransferred(totalBytesTransferred);
            notifyDataTransferred(mActionDone);
        }
    }

    @Override
    public void cancel() {
        try {
            mFtpClient.abort();
            mFtpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getSize(String path) {
        long fileSize = 0;
        FTPFile[] files = new FTPFile[0];
        try {
            files = mFtpClient.listFiles(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files.length == 1 && files[0].isFile()) {
            fileSize = files[0].getSize();
        }
        return fileSize;
    }

    @Override
    public long getLastModification(String path) {
        long lastModification = 0;
        FTPFile[] files = new FTPFile[0];
        try {
            files = mFtpClient.listFiles(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files.length == 1 && files[0].isFile()) {
            lastModification = files[0].getTimestamp().getTimeInMillis();
        }
        return lastModification;
    }
}
