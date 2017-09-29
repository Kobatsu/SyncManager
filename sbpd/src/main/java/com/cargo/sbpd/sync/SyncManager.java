package com.cargo.sbpd.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cargo.sbpd.R;
import com.cargo.sbpd.bus.RxBus;
import com.cargo.sbpd.bus.RxMessage;
import com.cargo.sbpd.jobs.CancelService;
import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.model.dao.ListingLogDao;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.dao.RemoteFileDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.FileNotification;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.ListFoldersToSyncState;
import com.cargo.sbpd.model.objects.ListingLog;
import com.cargo.sbpd.model.objects.LocalFile;
import com.cargo.sbpd.model.objects.RemoteFile;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.sync.discovery.AbstractClientChooser;
import com.cargo.sbpd.sync.discovery.ProtocolChooser;
import com.cargo.sbpd.sync.filelisting.FileLister;
import com.cargo.sbpd.sync.networklistener.ConnectionClassManager;
import com.cargo.sbpd.sync.networklistener.ConnectionQuality;
import com.cargo.sbpd.sync.networklistener.DeviceBandwidthSampler;
import com.cargo.sbpd.ui.notifications.chooseclient.AbstractNotifChooseClientFactory;
import com.cargo.sbpd.ui.notifications.chooseclient.NotifChooseClientFactory;
import com.cargo.sbpd.ui.notifications.estimatingdiff.AbstractNotifEstimatingDiffFactory;
import com.cargo.sbpd.ui.notifications.estimatingdiff.NotifEstimatingDiffFactory;
import com.cargo.sbpd.ui.notifications.updating.AbstractNotifUpdatingFactory;
import com.cargo.sbpd.ui.notifications.updating.NotifUpdatingFactory;
import com.cargo.sbpd.utils.FormatUtils;
import com.cargo.sbpd.utils.NetworkUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by leb on 31/07/2017.
 */

public class SyncManager implements ConnectionClassManager.ConnectionClassStateChangeListener {

    public static final String REQUEST_LIST_FOLDERS = "REQUEST_LIST_FOLDERS";
    public static final String REQUEST_START_DATE = "REQUEST_START_DATE";

    public static final String EVENT_FOLDER_ESTIMATING_STARTED = "EVENT_FOLDER_ESTIMATING_STARTED";
    public static final String EVENT_FOLDER_ESTIMATING_ENDED = "EVENT_FOLDER_ESTIMATING_ENDED";
    public static final String EVENT_FOLDER_SYNCING_STARTED = "EVENT_FOLDER_SYNCING_STARTED";
    public static final String EVENT_FOLDER_SYNCING_ENDED = "EVENT_FOLDER_SYNCING_ENDED";
    public static final String EVENT_FILE_SYNCING_STARTED = "EVENT_FILE_SYNCING_STARTED";
    public static final String EVENT_FILE_SYNCING_UPDATED = "EVENT_FILE_SYNCING_UPDATED";
    public static final String EVENT_FILE_SYNCING_ENDED = "EVENT_FILE_SYNCING_ENDED";

    public static final int REQUEST_NOTIFICATION_SYNCHRO = 2000;


    private static String network;

    private final Context mContext;
    private final List<FolderToSync> mListFolders;
    private Disposable mSubscription;
    private final AbstractNotifChooseClientFactory mNotifChooseClientFactory;
    private final AbstractNotifEstimatingDiffFactory mNotifEstimatingDiffFactory;
    private final AbstractNotifUpdatingFactory mNotifUpdatingFactory;
    private Date mStartDate;

    private List<AbstractSynchroClient> mListSynchroClient;
    private AbstractClientChooser mClientChooser;
    private boolean mIsWorking;
    private int mPosition;
    private boolean mCancel;
    private long mSizeTotal;
    private long mLastNotificationDate;
    private AbstractSynchroClient mCurrentClient;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            network = NetworkUtils.getNetworkName(context);
        }
    };

    SyncManager(Context context, List<AbstractSynchroClient> synchroClient,
                AbstractClientChooser clientChooser, List<FolderToSync> files,
                AbstractNotifChooseClientFactory notifChooseClientFactory,
                AbstractNotifEstimatingDiffFactory notifEstimatingFactory,
                AbstractNotifUpdatingFactory notifUpdatingFactory) {
        mContext = context;
        mListSynchroClient = synchroClient;
        network = NetworkUtils.getNetworkName(context);
        mClientChooser = clientChooser != null ? clientChooser : new ProtocolChooser();
        mListFolders = files;
        mCancel = false;

        mNotifChooseClientFactory = notifChooseClientFactory != null ? notifChooseClientFactory : new NotifChooseClientFactory(mContext);
        mNotifEstimatingDiffFactory = notifEstimatingFactory != null ? notifEstimatingFactory : new NotifEstimatingDiffFactory(mContext);
        mNotifUpdatingFactory = notifUpdatingFactory != null ? notifUpdatingFactory : new NotifUpdatingFactory(mContext);

        IntentFilter filter = new IntentFilter();
        filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mBroadcastReceiver, filter);
        mSubscription = RxBus.getRxBusSingleton().asFlowable().observeOn(Schedulers.newThread()).subscribe(event -> {
            // On ne peut utiliser ici que des variables finales de la classe ou des variables passées en paramètre du RxMessage
            // Sinon, on peut accéder à une variable qui a changé entre le moment où on a émit l'évènement et le moment
            // ou on le reçoit et consulte la variable
            if (event instanceof RxMessage) {
                RxMessage rxReceived = (RxMessage) event;
                switch (rxReceived.getRequest()) {
                    case REQUEST_LIST_FOLDERS:
                        RxBus.getRxBusSingleton().send(new RxMessage(rxReceived.getSender(), REQUEST_LIST_FOLDERS,
                                RxMessage.Type.RESPONSE, new ListFoldersToSyncState(mPosition, mListFolders)));
                        break;
                    case REQUEST_START_DATE:
                        RxBus.getRxBusSingleton().send(new RxMessage(rxReceived.getSender(), REQUEST_START_DATE,
                                RxMessage.Type.RESPONSE, mStartDate));
                        break;
                    case CancelService.ACTION_CANCEL:
                        mCancel = true;
                        //todo Tester si c'est bien géré
                        cancelNotifications();
                        break;
                    case EVENT_FOLDER_ESTIMATING_STARTED:
                        if (!mCancel) {
                            mNotifEstimatingDiffFactory.create(mContext, mListFolders,
                                    ((ListFoldersToSyncState) rxReceived.getObject()).getPosition()).show();
                        }
                        break;
                    case EVENT_FOLDER_ESTIMATING_ENDED:
                        if (((ListFoldersToSyncState) rxReceived.getObject()).getPosition() >= (mListFolders.size() - 1)) {
                            mNotifEstimatingDiffFactory.cancelAll();
                        }
                        break;
                    case EVENT_FOLDER_SYNCING_STARTED:
                        break;
                    case EVENT_FOLDER_SYNCING_ENDED:
                        if (((ListFoldersToSyncState) rxReceived.getObject()).getPosition() >= (mListFolders.size() - 1) || mCancel) {
                            endProperly();
                        }
                        break;
                    case EVENT_FILE_SYNCING_STARTED:
                        logActionStarted(((FileNotification) rxReceived.getObject()).getActionDone());
                        break;
                    case EVENT_FILE_SYNCING_UPDATED:
                    case EVENT_FILE_SYNCING_ENDED:
                        FileNotification fileNotification = ((FileNotification) rxReceived.getObject());
                        ActionDone actionDone = fileNotification.getActionDone();

                        if (actionDone.getActionType().equals(LocalFileDao.ActionType.DOWNLOAD)) {
                            logFileDownloaded(mCurrentClient.getLocalFileConverter().convertToRoomFiles(fileNotification.getFolderToSync(),
                                    new File(fileNotification.getFolderToSync().getFile(), actionDone.getLocalFilePath())), actionDone);
                        } else if (actionDone.getActionType().equals(LocalFileDao.ActionType.UPLOAD)) {
                            logFileUploaded(mCurrentClient.getRemoteFileConverter()
                                            .buildRemoteFile(FormatUtils.addSlashes(fileNotification.getFolderToSync().getRemotePath(),
                                                    actionDone.getRemoteFilePath()), actionDone.getSize(),
                                                    actionDone.getLastModification(), fileNotification.getFolderToSync(),
                                                    fileNotification.getFolderToSync().getDirection()),
                                    actionDone);
                        } else if (actionDone.getActionType().equals(LocalFileDao.ActionType.DELETE)) {
                            if (actionDone.getDestination().equals(LocalFileDao.Destination.LOCAL)) {
                                logLocalFileDeleted(mCurrentClient.getLocalFileConverter()
                                                .convertToRoomFiles(fileNotification.getFolderToSync(),
                                                        new File(fileNotification.getFolderToSync().getFile().getAbsolutePath(), actionDone.getLocalFilePath())),
                                        actionDone);
                            } else if (actionDone.getDestination().equals(LocalFileDao.Destination.REMOTE)) {
                                logRemoteFileDeleted(fileNotification.getFolderToSync(), actionDone.getRemoteFilePath(), actionDone);
                            }
                        }

                        if (!mCancel) {
                            showNotificationUpdating(actionDone, ((FileNotification) rxReceived.getObject()));
                        }
                        break;
                }
            }
        });
    }

    public synchronized void sync() {

        mIsWorking = true;
        mStartDate = new Date(System.currentTimeMillis());
        mNotifChooseClientFactory.create(mContext).show();

        mCurrentClient = chooseClientForListing(mListSynchroClient);

        ConnectionClassManager.getInstance().register(this);
        DeviceBandwidthSampler.getInstance().startSampling();
        LocalFileDao localFileDao = AppDatabase.getInstance().localFileDao();
        RemoteFileDao remoteFileDao = AppDatabase.getInstance().remoteFileDao();
        ListingLogDao listingLogDao = AppDatabase.getInstance().listingLogDao();

        //TODO we could add a check if the last listing was done long ago or not
        localFileDao.deleteAll();
        remoteFileDao.deleteAll();

        mNotifChooseClientFactory.cancelAll();

        mPosition = 0;
        for (FolderToSync folder : mListFolders) {
            if (mCancel) {
                break;
            }
            RxBus.getRxBusSingleton().send(new RxMessage(SyncManager.class.getSimpleName(),
                    EVENT_FOLDER_ESTIMATING_STARTED, RxMessage.Type.BROADCAST, new ListFoldersToSyncState(mPosition, mListFolders)));

//            mNotifEstimatingDiffFactory.create(mContext, mListFolders, mPosition).show();
            ListingLog listingLog = new ListingLog();
            listingLog.setNetwork(network);
            listingLog.setClient(mCurrentClient.getClientName());
            listingLog.setTimeStart(System.currentTimeMillis());
            listingLogDao.insert(listingLog);

            try {
                // list all local files from the folder and save them in database
                List<LocalFile> localFiles = FileLister.loadLocalFiles(folder, mCurrentClient.getLocalFileConverter());
                localFileDao.insertAll(localFiles);

                // list all remote files from the path and save them in database
                List<RemoteFile> list = mCurrentClient.listFilesAndEmptyDirectoriesRecursively(folder.getRemotePath(), folder);
                remoteFileDao.insertAll(list);

                listingLog.setNbFiles(list.size());
                listingLog.setTimeEnd(System.currentTimeMillis());
                listingLogDao.insert(listingLog);

            } catch (IOException e) {
                e.printStackTrace();
            }
            RxBus.getRxBusSingleton().send(new RxMessage(SyncManager.class.getSimpleName(),
                    EVENT_FOLDER_ESTIMATING_ENDED, RxMessage.Type.BROADCAST, new ListFoldersToSyncState(mPosition, mListFolders)));
            mPosition++;
        }
        //todo Remove exports when in production
        AppDatabase.exportDatabase(mContext, "before.db");


        mPosition = 0;
        mCurrentClient = chooseClientForActions(mListSynchroClient);

        for (FolderToSync folder : mListFolders) {
            if (mCancel) {
//                RxBus.getRxBusSingleton().send(new RxMessage(SyncManager.class.getSimpleName(),
//                        EVENT_FOLDER_SYNCING_ENDED, RxMessage.Type.BROADCAST, new ListFoldersToSyncState(mPosition, mListFolders)));
                break;
            }
            RxBus.getRxBusSingleton().send(new RxMessage(SyncManager.class.getSimpleName(),
                    EVENT_FOLDER_SYNCING_STARTED, RxMessage.Type.BROADCAST, new ListFoldersToSyncState(mPosition, mListFolders)));
            RxBus.getRxBusSingleton().send(new RxMessage(SyncManager.class.getSimpleName(), REQUEST_LIST_FOLDERS, RxMessage.Type.RESPONSE, new ListFoldersToSyncState(mPosition, mListFolders)));

            try {
                // Ready to check the difference
                List<LocalFile> filesToDelete = AppDatabase.getInstance().localFileDao()
                        .getFilesOnTabletNotServer(folder.getFile().getAbsolutePath(), folder.getRemotePath());
                Log.d("filesToDelete", "Size : " + filesToDelete.size());
                if (folder.getDirection() == AbstractSynchroClient.Direction.REMOTE_TO_LOCAL) {
                    FileLister.deleteListOfFiles(folder, filesToDelete, mCurrentClient.getLocalFileConverter());
                } else if (folder.getDirection() == AbstractSynchroClient.Direction.LOCAL_TO_REMOTE) {
                    mCurrentClient.uploadListOfFiles(folder, filesToDelete);
                }

                //todo add a filter on remoteFileSize != 0 then get the files with remoteFileSize = 0 and create the directories
                List<RemoteFile> filesOnServerNotTablet = AppDatabase.getInstance().remoteFileDao()
                        .getFilesOnServerNotTablet(folder.getFile().getAbsolutePath(), folder.getRemotePath());
                Log.d("filesToDownload", "Size : " + filesOnServerNotTablet.size());
                if (folder.getDirection() == AbstractSynchroClient.Direction.REMOTE_TO_LOCAL) {
                    mCurrentClient.downloadListOfFiles(folder, filesOnServerNotTablet);
                } else if (folder.getDirection() == AbstractSynchroClient.Direction.LOCAL_TO_REMOTE) {
                    mCurrentClient.deleteListOfFiles(folder, filesOnServerNotTablet);
                }

//                List<RemoteFile> EmptyFoldersToDownload = AppDatabase.getInstance().remoteFileDao().getEmptyFoldersOnServerNotTablet(folder.getFile().getAbsolutePath(), folder.getRemotePath());
//                if (folder.getDirection() == AbstractSynchroClient.Direction.REMOTE_TO_LOCAL) {
//                    synchroClient.downloadListOfFiles(folder, filesToDownload);
//                } else if (folder.getDirection() == AbstractSynchroClient.Direction.LOCAL_TO_REMOTE) {
//                    synchroClient.deleteEmptyFolders(folder, filesToDownload);
//                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            RxBus.getRxBusSingleton().send(new RxMessage(SyncManager.class.getSimpleName(),
                    EVENT_FOLDER_SYNCING_ENDED, RxMessage.Type.BROADCAST, new ListFoldersToSyncState(mPosition, mListFolders)));
            mPosition++;
        }
    }

    /**
     * Called when event received, not before
     */
    private void endProperly() {
        mPosition = 0;
        DeviceBandwidthSampler.getInstance().stopSampling();
        //todo Remove exports when in production
        AppDatabase.exportDatabase(mContext, "after.db");
        mIsWorking = false;
        SyncManagerBuilder.endSync();
        cancelNotifications();
        if (mSubscription != null && !mSubscription.isDisposed()) {
            mSubscription.dispose();
        }
    }

    private void showNotificationUpdating(ActionDone actionDone, FileNotification fileNotification) {
        long time = System.currentTimeMillis();
        // don't spam Mr. Notifications or he will block you temporarly
        if (time - mLastNotificationDate > 500 || (actionDone.getSize() == actionDone.getSizeTransferred()
                && (actionDone.getActionType().equals(LocalFileDao.ActionType.DELETE) || actionDone.getSize() > 0))) {
            for (int i = 0; i < mListFolders.size(); i++) {
                FolderToSync folder = mListFolders.get(i);
                if (folder.getRemotePath().equals(actionDone.getRemoteFolder()) && folder.getFile().getAbsolutePath().equals(actionDone.getLocalFolder())) {
                    mNotifUpdatingFactory.create(mListFolders, i, actionDone,
                            fileNotification.getSizeFolderTransferred(),
                            fileNotification.getSizeFolderTotal(),
                            R.mipmap.ic_launcher, R.drawable.ic_folder_black_24dp).show();
                    break;
                }
            }
            mLastNotificationDate = System.currentTimeMillis();
        }
    }


    private void cancelNotifications() {
        mNotifUpdatingFactory.cancelAll();
        mNotifEstimatingDiffFactory.cancelAll();
        mNotifChooseClientFactory.cancelAll();
    }


    private AbstractSynchroClient chooseClientForActions(List<AbstractSynchroClient> listSynchroClient) {
        if (mClientChooser == null) {
            return mListSynchroClient.get(0);
        } else {
            return mClientChooser.chooseClient(mContext, mListSynchroClient, mListFolders, network);
        }
    }

    private AbstractSynchroClient chooseClientForListing(List<AbstractSynchroClient> listSynchroClient) {
        if (mClientChooser == null) {
            return mListSynchroClient.get(0);
        } else {
            return mClientChooser.chooseClientForListing(mListSynchroClient, network);
        }
    }

    //region logging actions
    private void logFileDownloaded(LocalFile localFile, ActionDone actionDone) {
        if (localFile.getSize() == actionDone.getSizeTransferred() && actionDone.getException() == null) {
            // Il n'apparaît plus dans les actiontodo ensuite du coup
            AppDatabase.getInstance().localFileDao().insert(localFile);
        } else {
            // TODO: 04/08/2017 Ajouter la gestion des exceptions dans le LiveData
        }
        // Update les actiondone
        AppDatabase.getInstance().actionDoneDao().update(actionDone);
    }

    private void logFileUploaded(RemoteFile remotefile, ActionDone actionDone) {
        if (remotefile.getSize() == actionDone.getSizeTransferred() && actionDone.getException() == null) {
            AppDatabase.getInstance().remoteFileDao().insert(remotefile);
        } else {
            // TODO: 04/08/2017 Ajouter la gestion des exceptions dans le LiveData
        }
        AppDatabase.getInstance().actionDoneDao().update(actionDone);
    }

    private void logLocalFileDeleted(LocalFile localFile, ActionDone actionDone) {
        if (0 == localFile.getSize() && actionDone.getException() == null) {
            AppDatabase.getInstance().localFileDao().delete(localFile.getName());
        } else {
            // TODO: 04/08/2017 Ajouter la gestion des exceptions dans le LiveData
        }
        AppDatabase.getInstance().actionDoneDao().update(actionDone);
    }

    private void logRemoteFileDeleted(FolderToSync folder, String pathFtp, ActionDone actionDone) {
        if (0 == actionDone.getSizeTransferred() && actionDone.getException() == null) {
            AppDatabase.getInstance().remoteFileDao().delete(pathFtp, folder.getFile().getAbsolutePath(), folder.getRemotePath());
        } else {
            // TODO: 04/08/2017 Ajouter la gestion des exceptions dans le LiveData
        }
        AppDatabase.getInstance().actionDoneDao().update(actionDone);
    }

    private void logActionStarted(ActionDone actionDone) {
        actionDone.setId(AppDatabase.getInstance().actionDoneDao().insert(actionDone));
    }
    //endregion

    //todo remove this method, no sense here
    public static void initDatabaseInstance(Context context) {
        AppDatabase.initInstance(context);
    }

    public static String getNetwork() {
        return network;
    }

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
        Log.d("BandwidthStateChanged", "quality : " + bandwidthState.name());
    }

    public void unregisterToNetworkChange(Context context) {
        context.unregisterReceiver(mBroadcastReceiver);
    }

    public boolean isWorking() {
        return mIsWorking;
    }

}
