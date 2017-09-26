package com.cargo.synchroftpapache;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by leb on 24/08/2017.
 */

public class ApacheFTPClientLaggy extends ApacheFTPClient {
    private final String s = "";


    public ApacheFTPClientLaggy(String adress, String user, String password) {
        super(adress, user, password);
    }

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
        customWait(2000);
        return result;
    }

    private void customWait(int time) {
        long timeStart = System.currentTimeMillis();

        while ((System.currentTimeMillis() - timeStart) < time) {

        }
    }

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
        customWait(2000);

        return result;
    }
}
