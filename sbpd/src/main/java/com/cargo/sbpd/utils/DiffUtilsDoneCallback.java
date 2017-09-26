package com.cargo.sbpd.utils;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.ActionToDo;

import java.util.List;

/**
 * Created by leb on 01/08/2017.
 */

public class DiffUtilsDoneCallback extends DiffUtil.Callback {

    List<ActionDone> oldActions;
    List<ActionDone> newActions;

    public DiffUtilsDoneCallback(List<ActionDone> oldActions, List<ActionDone> newActions) {
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
        return oldActions.get(oldItemPosition).getId() == newActions.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldActions.get(oldItemPosition).equals(newActions.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}