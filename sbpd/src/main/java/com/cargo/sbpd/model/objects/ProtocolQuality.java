package com.cargo.sbpd.model.objects;

import android.content.Context;

import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.model.dao.ActionDoneDao;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.sync.discovery.SampleTestTransfert;
import com.cargo.sbpd.utils.NetworkUtils;

import java.util.Calendar;

/**
 * Created by leb on 27/07/2017.
 */

public class ProtocolQuality {

    /*
    Use some arbitrary values, for example : UPLOAD_HIGH_SPEED, UPLOAD_LOW_SPEED...
    UPLOAD_500MBPS
    UPLOAD_200MBPS

    10_FILES
    50_FILES
    100_FILES


    Sur des gros fichiers, on privilégiera de gros débits.
    Sur des petits fichiers, on s'en préoccupera peut-être un peu moins, et on voudra davantage vérifier la montée en régime, le temps d'établissement
    de la connection etc...


    On pourrait avoir une qualité minimale pour le téléchargement des fichiers et si on passe en dessous de cette qualité pendant une certaine durée alors on arrête la synchro et on la reporte
    */

    private static final int NB_ACTIONS_MINI = 10; // At least 10 actions done
    private static final long SIZE_MINI = 1000000; // At least 1 Mo approx

    private final AbstractSynchroClient mClient;

    private boolean mBasedOnSample;

    private float mUploadSpeed;
    private float mDownloadSpeed;

    private int mNbFilesUploadedSuccess;
    private int mNbFilesUploadedError;

    private int mNbFilesDownloadedSuccess;
    private int mNbFilesDownloadedError;


    public ProtocolQuality(Context context, AbstractSynchroClient client, String network) {
        mClient = client;
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -7);
        ActionDoneDao dao = AppDatabase.getInstance().actionDoneDao();
        long date = c.getTimeInMillis();
        String clientName = client.getClientName();

        // If no info on this client and network we have to make some tests with a sample file. Can't rely on stats on other networks since they may be really different from this one.
        if (!NetworkUtils.getListMobileNetwork().contains(network) && isThereInformationsForClientAndNetwork(client, network, NB_ACTIONS_MINI, SIZE_MINI, c.getTimeInMillis())) {
            // if stats for the folder exists use them? Or not... Not really usefull since if the files are all up to date, there may not be anything new. Non sense.
            // check stats
            mBasedOnSample = false;
            int isSample = mBasedOnSample ? 1 : 0;
            mDownloadSpeed = dao.getSpeed(clientName, network, date, LocalFileDao.ActionType.DOWNLOAD, isSample);
            mUploadSpeed = dao.getSpeed(clientName, network, date, LocalFileDao.ActionType.UPLOAD, isSample);
            mNbFilesDownloadedSuccess = dao.getNbFilesSuccess(clientName, network, date, LocalFileDao.ActionType.DOWNLOAD, isSample);
            mNbFilesUploadedSuccess = dao.getNbFilesSuccess(clientName, network, date, LocalFileDao.ActionType.UPLOAD, isSample);
            mNbFilesDownloadedError = dao.getNbFilesError(clientName, network, date, LocalFileDao.ActionType.DOWNLOAD, isSample);
            mNbFilesUploadedError = dao.getNbFilesError(clientName, network, date, LocalFileDao.ActionType.UPLOAD, isSample);
        } else {
            //do sample then get protocol quality
            date = System.currentTimeMillis(); // only get last information sample
            if (SampleTestTransfert.testSample(context, client, network)) {
                mBasedOnSample = true;
                int isSample = mBasedOnSample ? 1 : 0;
                mDownloadSpeed = dao.getSpeed(clientName, network, date, LocalFileDao.ActionType.DOWNLOAD, isSample);
                mUploadSpeed = dao.getSpeed(clientName, network, date, LocalFileDao.ActionType.UPLOAD, isSample);
                mNbFilesDownloadedSuccess = dao.getNbFilesSuccess(clientName, network, date, LocalFileDao.ActionType.DOWNLOAD, isSample);
                mNbFilesUploadedSuccess = dao.getNbFilesSuccess(clientName, network, date, LocalFileDao.ActionType.UPLOAD, isSample);
                mNbFilesDownloadedError = dao.getNbFilesError(clientName, network, date, LocalFileDao.ActionType.DOWNLOAD, isSample);
                mNbFilesUploadedError = dao.getNbFilesError(clientName, network, date, LocalFileDao.ActionType.UPLOAD, isSample);
            }
        }
    }

    private boolean isThereInformationsForClientAndNetwork(AbstractSynchroClient client, String network, int nbActionsMini, long sizeMini, long timeInMillis) {
        ClientAndNetworkInfo cani = AppDatabase.getInstance().actionDoneDao().getNbActionsAndSizeTransferred(client.getClientName(), network, timeInMillis);
        return cani.getNbActions() > nbActionsMini && sizeMini > cani.getSizeTransferredSum();
    }

    public boolean isBasedOnSample() {
        return mBasedOnSample;
    }

    public void setBasedOnSample(boolean basedOnSample) {
        mBasedOnSample = basedOnSample;
    }

    public float getUploadSpeed() {
        return mUploadSpeed;
    }

    public void setUploadSpeed(float uploadSpeed) {
        mUploadSpeed = uploadSpeed;
    }

    public float getDownloadSpeed() {
        return mDownloadSpeed;
    }

    public void setDownloadSpeed(float downloadSpeed) {
        mDownloadSpeed = downloadSpeed;
    }

    public int getNbFilesUploadedSuccess() {
        return mNbFilesUploadedSuccess;
    }

    public void setNbFilesUploadedSuccess(int nbFilesUploadedSuccess) {
        mNbFilesUploadedSuccess = nbFilesUploadedSuccess;
    }

    public int getNbFilesUploadedError() {
        return mNbFilesUploadedError;
    }

    public void setNbFilesUploadedError(int nbFilesUploadedError) {
        mNbFilesUploadedError = nbFilesUploadedError;
    }

    public int getNbFilesDownloadedSuccess() {
        return mNbFilesDownloadedSuccess;
    }

    public void setNbFilesDownloadedSuccess(int nbFilesDownloadedSuccess) {
        mNbFilesDownloadedSuccess = nbFilesDownloadedSuccess;
    }

    public int getNbFilesDownloadedError() {
        return mNbFilesDownloadedError;
    }

    public void setNbFilesDownloadedError(int nbFilesDownloadedError) {
        mNbFilesDownloadedError = nbFilesDownloadedError;
    }

    public AbstractSynchroClient getClient() {
        return mClient;
    }

    @Override
    public String toString() {
        return "ProtocolQuality{" +
                "mClient=" + mClient +
                ", mBasedOnSample=" + mBasedOnSample +
                ", mUploadSpeed=" + mUploadSpeed +
                ", mDownloadSpeed=" + mDownloadSpeed +
                ", mNbFilesUploadedSuccess=" + mNbFilesUploadedSuccess +
                ", mNbFilesUploadedError=" + mNbFilesUploadedError +
                ", mNbFilesDownloadedSuccess=" + mNbFilesDownloadedSuccess +
                ", mNbFilesDownloadedError=" + mNbFilesDownloadedError +
                '}';
    }
}
