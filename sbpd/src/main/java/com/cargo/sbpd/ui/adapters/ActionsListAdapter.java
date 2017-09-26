package com.cargo.sbpd.ui.adapters;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cargo.sbpd.R;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.model.objects.ActionToDo;
import com.cargo.sbpd.utils.DiffUtilsTodoCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leb on 31/07/2017.
 */

public class ActionsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ActionToDo> mList;

    public ActionsListAdapter() {
        mList = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_action_to_do, parent, false);
        return new ActionsListAdapter.TodoHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ActionToDo action = mList.get(holder.getLayoutPosition());
//
        TodoHolder todoHolder = (TodoHolder) holder;
        todoHolder.name.setText(action.getName());
        switch (mList.get(holder.getLayoutPosition()).getActionType()) {
            case LocalFileDao.ActionType.DOWNLOAD:
                todoHolder.icon.setImageResource(R.drawable.ic_file_download_black_24dp);
                todoHolder.action.setText(action.getActionType());
                break;
            case LocalFileDao.ActionType.UPLOAD:
                todoHolder.icon.setImageResource(R.drawable.ic_file_upload_black_24dp);
                todoHolder.action.setText(action.getActionType());
                break;
            case LocalFileDao.ActionType.DELETE:
                todoHolder.icon.setImageResource(R.drawable.ic_close_black_24dp_vector);
                todoHolder.action.setText(action.getActionType());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void dataChanged(List<ActionToDo> actionToDos) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtilsTodoCallback(mList, actionToDos));
        mList = actionToDos;
        diffResult.dispatchUpdatesTo(this);
    }

    public class TodoHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView action;

        public TodoHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.action_icon);
            name = (TextView) view.findViewById(R.id.action_file_name);
            action = (TextView) view.findViewById(R.id.action_type);
        }
    }
}
