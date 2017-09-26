package com.cargo.sbpd.ui.dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.cargo.sbpd.R;

/**
 * Created by Sébastien on 15/08/2017.
 */

public class DialogBuilder {

    public static void showListDialog(Context context, String title, RecyclerView.Adapter adapter) {
        RecyclerView v = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.list_folders_to_sync, null);
        v.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        v.setAdapter(adapter);
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(v)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();
    }
}
