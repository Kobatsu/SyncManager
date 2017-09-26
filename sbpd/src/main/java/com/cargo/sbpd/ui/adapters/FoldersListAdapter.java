package com.cargo.sbpd.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cargo.sbpd.R;
import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.SizeAndNumber;
import com.cargo.sbpd.sync.clients.AbstractSynchroClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sébastien on 12/08/2017.
 */

public class FoldersListAdapter extends RecyclerView.Adapter<FoldersListAdapter.FolderHolder> {

    private final List<FolderToSync> mListFoldersToSync;
    private Activity mActivity;
    private int mCurrentFolderIndex;

    public FoldersListAdapter(Activity activity) {
        mListFoldersToSync = new ArrayList<>();
        mActivity = activity;
        mCurrentFolderIndex = -1;
    }

    @Override
    public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder_to_sync, parent, false);
        return new FoldersListAdapter.FolderHolder(view);
    }

    @Override
    public void onBindViewHolder(FolderHolder holder, int position) {
        FolderToSync folder = mListFoldersToSync.get(holder.getLayoutPosition());
        holder.title.setText(folder.getDisplayName() + (holder.getLayoutPosition() == mCurrentFolderIndex ? " (en cours)" : ""));

        new Thread(() -> {
            SizeAndNumber delete = null;
            if (folder.getDirection() == AbstractSynchroClient.Direction.LOCAL_TO_REMOTE) {
                delete = AppDatabase.getInstance().localFileDao().getNbAndSizeDeleteTodoLocalToRemote(folder.getFile().getAbsolutePath(), folder.getRemotePath());
            } else if (folder.getDirection() == AbstractSynchroClient.Direction.REMOTE_TO_LOCAL) {
                delete = AppDatabase.getInstance().localFileDao().getNbAndSizeDeleteTodoRemoteToLocal(folder.getFile().getAbsolutePath(), folder.getRemotePath());
            }
            SizeAndNumber upload = AppDatabase.getInstance().localFileDao().getNbAndSizeUploadTodo(folder.getFile().getAbsolutePath(), folder.getRemotePath());
            SizeAndNumber download = AppDatabase.getInstance().localFileDao().getNbAndSizeDownloadTodo(folder.getFile().getAbsolutePath(), folder.getRemotePath());


            SizeAndNumber finalDelete = delete;
            mActivity.runOnUiThread(() -> {
                holder.delete.setText(finalDelete.getDisplayString(mActivity));
                holder.upload.setText(upload.getDisplayString(mActivity));
                holder.download.setText(download.getDisplayString(mActivity));
            });
        }).start();
    }

    @Override
    public int getItemCount() {
        return mListFoldersToSync.size();
    }

    public void update(List<FolderToSync> list, int indexFolder) {
        mListFoldersToSync.clear();
        mListFoldersToSync.addAll(list);
        mCurrentFolderIndex = indexFolder;
        notifyDataSetChanged();
    }


    public class FolderHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView delete;
        TextView upload;
        TextView download;

        public FolderHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.sbpd_item_folder_title);
            delete = (TextView) view.findViewById(R.id.sbpd_item_files_to_delete);
            upload = (TextView) view.findViewById(R.id.sbpd_item_files_to_upload);
            download = (TextView) view.findViewById(R.id.sbpd_item_files_to_download);
        }
    }
}
