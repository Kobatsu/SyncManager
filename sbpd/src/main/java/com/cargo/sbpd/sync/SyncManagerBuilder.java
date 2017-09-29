package com.cargo.sbpd.sync;

import android.content.Context;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;
import com.cargo.sbpd.sync.discovery.AbstractClientChooser;
import com.cargo.sbpd.sync.filelisting.AbstractLocalConverter;
import com.cargo.sbpd.sync.filelisting.AbstractRemoteConverter;
import com.cargo.sbpd.ui.notifications.chooseclient.AbstractNotifChooseClientFactory;
import com.cargo.sbpd.ui.notifications.estimatingdiff.AbstractNotifEstimatingDiffFactory;
import com.cargo.sbpd.ui.notifications.updating.AbstractNotifUpdatingFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by leb on 04/08/2017.
 */

public class SyncManagerBuilder {

    private final List<FolderToSync> mListFolders;
    private Context mContext;
    private List<AbstractSynchroClient> mAbstractSynchroClients;
    private AbstractClientChooser mClientChooser;

    private static SyncManager instance;
    private AbstractNotifChooseClientFactory mNotifChooseClientFactory;
    private AbstractNotifEstimatingDiffFactory mNotifEstimatingFactory;
    private AbstractNotifUpdatingFactory mNotifUpdatingFactory;

    public SyncManagerBuilder(Context context) {
        mContext = context;
        mAbstractSynchroClients = new ArrayList<>();
        mListFolders = new ArrayList<>();
    }

    public SyncManagerBuilder addClient(AbstractSynchroClient client) {
        mAbstractSynchroClients.add(client);
        return this;
    }

    public SyncManagerBuilder addFolder(FolderToSync... folderToSyncs) {
        Collections.addAll(mListFolders, folderToSyncs);
        return this;
    }

    public SyncManagerBuilder setClientChooser(AbstractClientChooser clientChooser) {
        mClientChooser = clientChooser;
        return this;
    }

    public SyncManagerBuilder setNotifChooseClientFactory(AbstractNotifChooseClientFactory notifChooseClientFactory) {
        mNotifChooseClientFactory = notifChooseClientFactory;
        return this;
    }

    public SyncManagerBuilder setNotifEstimatingFactory(AbstractNotifEstimatingDiffFactory notifEstimatingFactory) {
        mNotifEstimatingFactory = notifEstimatingFactory;
        return this;
    }

    public SyncManagerBuilder setNotifUpdatingFactory(AbstractNotifUpdatingFactory notifUpdatingFactory) {
        mNotifUpdatingFactory = notifUpdatingFactory;
        return this;
    }

    public SyncManager build() {
        // Only one instance of SyncManager should be running at the same time
        if (instance == null || instance.isWorking()) {
            return instance = new SyncManager(mContext, mAbstractSynchroClients, mClientChooser, mListFolders,
                    mNotifChooseClientFactory, mNotifEstimatingFactory, mNotifUpdatingFactory);
        } else {
            throw new UnsupportedOperationException("SyncManagerBuilder.build() should not get called twice");
        }
    }

    public static void endSync() {
        instance = null;
    }
}
