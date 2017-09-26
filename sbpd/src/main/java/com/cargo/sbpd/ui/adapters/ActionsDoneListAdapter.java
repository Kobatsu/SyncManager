package com.cargo.sbpd.ui.adapters;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cargo.sbpd.R;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.utils.DiffUtilsDoneCallback;
import com.cargo.sbpd.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leb on 31/07/2017.
 */

public class ActionsDoneListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ActionDone> mList;
    private Context mContext;

    public ActionsDoneListAdapter(Context context) {
        mList = new ArrayList<>();
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_action_done, parent, false);
        return new DoneHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ActionDone action = mList.get(holder.getLayoutPosition());
//
        DoneHolder doneHolder = (DoneHolder) holder;
        doneHolder.name.setText(action.getRemoteFilePath());
        switch (mList.get(holder.getLayoutPosition()).getActionType()) {
            case LocalFileDao.ActionType.DOWNLOAD:
                doneHolder.icon.setImageResource(R.drawable.ic_file_download_black_24dp);
                break;
            case LocalFileDao.ActionType.UPLOAD:
                doneHolder.icon.setImageResource(R.drawable.ic_file_upload_black_24dp);
                break;
            case LocalFileDao.ActionType.DELETE:
                doneHolder.icon.setImageResource(R.drawable.ic_close_black_24dp_vector);
                break;
        }

        SimpleDateFormat sdf = FormatUtils.getDateFormatter();

        String size;
        if (action.getActionType().equals(LocalFileDao.ActionType.DELETE)) {
            size = FormatUtils.readableFileSize(action.getSize());
        } else {
            size = FormatUtils.readableFileSize(action.getSizeTransferred()) + "/" + FormatUtils.readableFileSize(action.getSize());
        }
        doneHolder.fileSize.setText(size);
        doneHolder.lastModif.setText(sdf.format(action.getLastModification()));
        doneHolder.dateTransfert.setText(
                String.format("%s %s, %s %s", mContext.getResources().getString(R.string.started_at),
                        sdf.format(action.getStartTransfert()),
                        mContext.getString(R.string.duration),
                        FormatUtils.readableDuration(action.getEndTransfert() - action.getStartTransfert())));
        doneHolder.network.setText(action.getSynchroClient() + " - " + action.getNetwork());

        String rootFolder;
        if (action.isSample()) {
            rootFolder = action.getRemoteFolder() + " ( sample )";
        } else {
            rootFolder = action.getRemoteFolder();
        }
        doneHolder.rootFolder.setText(rootFolder);


        if (action.getException() == null) {
            doneHolder.exception.setVisibility(View.GONE);
        } else {
            doneHolder.exception.setVisibility(View.VISIBLE);
            doneHolder.exception.setText(action.getException());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void dataChanged(List<ActionDone> actionDones) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtilsDoneCallback(mList, actionDones));
        mList = actionDones;
        diffResult.dispatchUpdatesTo(this);
    }

    public class DoneHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView fileSize;
        TextView network;
        TextView lastModif;
        TextView rootFolder;
        TextView dateTransfert;
        TextView exception;

        public DoneHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.action_icon);
            name = (TextView) view.findViewById(R.id.action_file_name);
            fileSize = (TextView) view.findViewById(R.id.action_file_size);
            network = (TextView) view.findViewById(R.id.action_network);
            lastModif = (TextView) view.findViewById(R.id.action_file_last_modif);
            rootFolder = (TextView) view.findViewById(R.id.action_root_folder);
            dateTransfert = (TextView) view.findViewById(R.id.action_date_transfert);
            exception = (TextView) view.findViewById(R.id.action_exception);
        }
    }
}
