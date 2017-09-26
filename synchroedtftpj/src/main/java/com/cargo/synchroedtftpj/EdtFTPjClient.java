package com.cargo.synchroedtftpj;

import android.util.Log;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.sync.filelisting.ListFilter;
import com.cargo.sbpd.utils.FormatUtils;
import com.enterprisedt.net.ftp.EventListener;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FileTransferClient;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leb on 24/08/2017.
 */

public class EdtFTPjClient extends AbstractSynchroClient {

    protected final String mAdress;
    protected final String mUser;
    protected final String mPassWord;
    protected FileTransferClient mFtpClient;

    public EdtFTPjClient(String adress, String user, String password) {
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
            mFtpClient = new FileTransferClient();
//            Logger.setLevel(Level.ALL);
            mFtpClient.getAdvancedSettings().setControlEncoding("UTF-8");
            mFtpClient.setRemoteHost(mAdress);
            mFtpClient.setUserName(mUser);
            mFtpClient.setPassword(mPassWord);
            mFtpClient.connect();
            mFtpClient.setContentType(FTPTransferType.BINARY);
            mFtpClient.getAdvancedFTPSettings().setConnectMode(FTPConnectMode.PASV);
            mFtpClient.setEventListener(new EventListener() {
                @Override
                public void commandSent(String s, String s1) {

                }

                @Override
                public void replyReceived(String s, String s1) {

                }

                @Override
                public void bytesTransferred(String s, String s1, long l) {
                    onDataTransferred(l);
                }

                @Override
                public void downloadStarted(String s, String s1) {

                }

                @Override
                public void downloadCompleted(String s, String s1) {

                }

                @Override
                public void uploadStarted(String s, String s1) {

                }

                @Override
                public void uploadCompleted(String s, String s1) {

                }
            });
            result = true;
        } catch (IOException | FTPException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    @Override
    public boolean disconnect() {
        try {
            mFtpClient.disconnect();
            return true;
        } catch (FTPException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RemoteFile> listFilesAndEmptyDirectoriesRecursively(String path, FolderToSync folderLocal) throws IOException {
        ListFilter listFilter = folderLocal.getListFilter();
        List<RemoteFile> list = new ArrayList<>();
        FTPFile[] listFiles = new FTPFile[0];
        try {
            listFiles = mFtpClient.directoryList(path);
        } catch (FTPException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
                                file.size(),
                                file.lastModified().getTime(),
                                folderLocal,
                                folderLocal.getDirection()));
                    }
                }
                if (file.isDir()) {
                    List<RemoteFile> fileList = listFilesAndEmptyDirectoriesRecursively(FormatUtils.addSlash(path) + file.getName(), folderLocal);

                    if (fileList.size() > 0) {
                        list.addAll(fileList);
                    } else {
                        if (listFilter == null || listFilter.acceptRemote(FormatUtils.addSlashes(path, file.getName()))) {
                            list.add(mRemoteFileConverter.buildRemoteFile(FormatUtils.addSlash(path) + file.getName(), file.size(), file.lastModified().getTime(), folderLocal, folderLocal.getDirection()));
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
        try {
            mFtpClient.uploadFile(file.getAbsolutePath(), pathFtp);
            result = true;
        } catch (FTPException e) {
            Log.e("EdtFtpJ.Upload", pathFtp);
            e.printStackTrace();
        }
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
            mFtpClient.createDirectory(dirTree);
        } catch (FTPException e) {
            Log.e("EdtFtpJ.CreateDir", dirTree);
            e.printStackTrace();
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
        try {
            mFtpClient.downloadFile(file.getAbsolutePath(), pathFtp);
            result = true;
        } catch (FTPException npe) {
            Log.e("EdtFtpJ.Download", pathFtp);
            npe.printStackTrace();
        }
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
            Log.d("EdtFtpJ", remote.getName() + " delete success = " + result);
        }
    }

    @Override
    public boolean deleteWithoutLogs(String path) throws IOException {
        boolean result = false;
        try {
            mFtpClient.deleteFile(path);
            result = true;
        } catch (FTPException e) {
            boolean res = false;
            try {
                mFtpClient.deleteDirectory(path);
                res = true;
            } catch (FTPException e1) {
                Log.d("DeleteDirError", path);
                e1.printStackTrace();
            }
            if (!res) {
                Log.d("DeleteFileError", path);
                e.printStackTrace();
            }
        }
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
            mFtpClient.cancelAllTransfers();
            mFtpClient.disconnect();
        } catch (IOException | FTPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getSize(String path) {
        long fileSize = 0;
        FTPFile[] files = new FTPFile[0];
        try {
            files = mFtpClient.directoryList(path);
        } catch (IOException | FTPException | ParseException e) {
            Log.e("EdtFtpJ.getSize", "Path : " + path);
            e.printStackTrace();
        }
        if (files.length == 1 && files[0].isFile()) {
            fileSize = files[0].size();
        }
        return fileSize;
    }

    @Override
    public long getLastModification(String path) {
        long lastModification = 0;
        FTPFile[] files = new FTPFile[0];
        try {
            files = mFtpClient.directoryList(path);
        } catch (IOException | FTPException | ParseException e) {
            Log.e("EdtFtpJ.LastModif", path);
            e.printStackTrace();
        }
        if (files.length == 1 && files[0].isFile()) {
            lastModification = files[0].lastModified().getTime();
        }
        return lastModification;
    }
}