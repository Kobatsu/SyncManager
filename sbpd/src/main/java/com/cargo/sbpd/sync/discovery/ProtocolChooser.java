package com.cargo.sbpd.sync.discovery;

import android.content.Context;
import android.util.Log;

import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.model.dao.ListingLogDao;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.ListingStats;
import com.cargo.sbpd.model.objects.ProtocolQuality;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by leb on 07/08/2017.
 */

public class ProtocolChooser extends AbstractClientChooser {

    // TODO: 08/08/2017 HIGH PRIORITY : add marge on differences
    private static Comparator<ProtocolQuality> COMPARATOR_PRIORITY_DOWNLOAD_SPEED = (pq1, pq2) -> {
        if (pq1.getDownloadSpeed() > pq2.getDownloadSpeed()) {
            return -1;
        } else if (pq1.getDownloadSpeed() == pq2.getDownloadSpeed()) {
            return 0;
        } else {
            return 1;
        }
    };

    private static Comparator<ProtocolQuality> COMPARATOR_PRIORITY_UPLOAD_SPEED = (pq1, pq2) -> {
        if (pq1.getUploadSpeed() > pq2.getUploadSpeed()) {
            return 1;
        } else if (pq1.getUploadSpeed() == pq2.getUploadSpeed()) {
            return 0;
        } else {
            return -1;
        }
    };


    private static Comparator<ProtocolQuality> COMPARATOR_PRIORITY_FIABILITY_DOWNLOAD = (pq1, pq2) -> {
        if ((1.0 * pq1.getNbFilesDownloadedError()) / (pq1.getNbFilesDownloadedSuccess() + pq1.getNbFilesDownloadedError())
                > (1.0 * pq2.getNbFilesDownloadedError()) / (pq2.getNbFilesDownloadedSuccess() + pq2.getNbFilesDownloadedError())) {
            return 1;
        } else if ((1.0 * pq1.getNbFilesDownloadedError()) / (pq1.getNbFilesDownloadedSuccess() + pq1.getNbFilesDownloadedError())
                == (1.0 * pq2.getNbFilesDownloadedError()) / (pq2.getNbFilesDownloadedSuccess() + pq2.getNbFilesDownloadedError())) {
            return 0;
        } else {
            return -1;
        }
    };


    @Override
    public AbstractSynchroClient chooseClient(Context context, List<AbstractSynchroClient> clients,
                                              List<FolderToSync> folders, String network) {
        List<ProtocolQuality> list = new ArrayList<>();
        if (clients.size() > 1) {
            for (AbstractSynchroClient client : clients) {
                list.add(new ProtocolQuality(context, client, network));
            }

            int nbLocalToRemote = 0;
            int nbRemoteToLocal = 0;
            for (FolderToSync folder : folders) {
                if (folder.getDirection() == AbstractSynchroClient.Direction.LOCAL_TO_REMOTE) {
                    nbLocalToRemote++;
                } else if (folder.getDirection() == AbstractSynchroClient.Direction.REMOTE_TO_LOCAL) {
                    nbRemoteToLocal++;
                }
            }
            // TODO implement comparators
            if (nbLocalToRemote > 0 && nbRemoteToLocal == 0) {
                // Choose comparator with best upload speed
            } else if (nbRemoteToLocal > 0 && nbLocalToRemote == 0) {
                // Choose comparator with best download speed
            }

            Collections.sort(list, COMPARATOR_PRIORITY_DOWNLOAD_SPEED);
            for (int i = 0; i < list.size(); i++) {
                Log.d("ProtocolChooser", i + " : " + list.get(i).toString());
            }
            return list.get(0).getClient();
        } else {
            return clients.get(0);
        }
    }

    @Override
    public AbstractSynchroClient chooseClientForListing(List<AbstractSynchroClient> clients, String network) {
        List<AbstractSynchroClient> clientsOk = new ArrayList<>();
        for (AbstractSynchroClient client : clients) {
            try {
                if (client.connect()) {
                    clientsOk.add(client);
                }
            } catch (Exception e) {
                // Can't connect / disconnect : problem
            }
        }

        ListingLogDao dao = AppDatabase.getInstance().listingLogDao();
        List<ListingStats> list = new ArrayList<>();
        for (AbstractSynchroClient client : clientsOk) {
            // Récupérer les stats si on a des stats
            ListingStats stats = dao.getAll(client.getClientName(), network);
            if (stats != null) {
                stats.setClient(client);
                list.add(stats);
            } else {
                ListingStats stat = new ListingStats();
                stat.setClient(client);
                list.add(stat);
            }
        }
        // Trier les stats et renvoyer le client n°1 (ou un des clients n'ayant jamais fait de listing)
        // Pour l'instant, juste faire par rapport au nombre de fichiers listés par ms
        Collections.sort(list, (a, b) -> {
            float valA = ((float) a.getNbFilesTotal() / (float) a.getTimeTotal());
            float valB = ((float) b.getNbFilesTotal() / (float) b.getTimeTotal());
            if (valA > valB) {
                return 1;
            } else if (valA < valB) {
                return -1;
            } else {
                return 0;
            }
        });

        if (list.size() > 0) {
            return list.get(0).getClient();
        } else {
            // no clients which succeded to connect and disconnect
            return null;
        }
    }
}
