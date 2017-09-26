package com.cargo.sbpd.sync.discovery;

import android.content.Context;

import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;

import java.util.List;

/**
 * Created by leb on 07/08/2017.
 *
 * This class should determine which synchro clien use to make the listing and the synchronisation of the files
 */

public abstract class AbstractClientChooser {

    public abstract AbstractSynchroClient chooseClient(Context context, List<AbstractSynchroClient> clients,
                                                       List<FolderToSync> folders, String network);

    public abstract AbstractSynchroClient chooseClientForListing(List<AbstractSynchroClient> clients, String network);

}
