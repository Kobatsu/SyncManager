package com.cargo.sbpd.utils;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.cargo.sbpd.model.objects.ActionToDo;

import java.util.List;

/**
 * Created by leb on 01/08/2017.
 */

public class DiffUtilsTodoCallback extends DiffUtil.Callback {

    List<ActionToDo> oldActions;
    List<ActionToDo> newActions;

    public DiffUtilsTodoCallback(List<ActionToDo> oldActions, List<ActionToDo> newActions) {
        this.oldActions = oldActions;
        this.newActions = newActions;
    }

    @Override
    public int getOldListSize() {
        return oldActions.size();
    }

    @Override
    public int getNewListSize() {
        return newActions.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldActions.get(oldItemPosition).getName().equals(newActions.get(newItemPosition).getName())
                && oldActions.get(oldItemPosition).getActionType().equals(newActions.get(newItemPosition).getActionType());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldActions.get(oldItemPosition).getSize() == newActions.get(newItemPosition).getSize()
                && oldActions.get(oldItemPosition).getLastModification() == newActions.get(newItemPosition).getLastModification();
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}