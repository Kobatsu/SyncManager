package com.cargo.sbpd.ui.activities;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cargo.sbpd.R;
import com.cargo.sbpd.bus.RxBus;
import com.cargo.sbpd.bus.RxMessage;
import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.model.dao.LocalFileDao;
import com.cargo.sbpd.sync.SyncManager;
import com.cargo.sbpd.sync.networklistener.INetworkSpeedListener;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.ActionToDo;
import com.cargo.sbpd.model.objects.FolderToSync;
import com.cargo.sbpd.model.objects.ListFoldersToSyncState;
import com.cargo.sbpd.model.objects.NetworkSpeed;
import com.cargo.sbpd.sync.networklistener.NetworkListener;
import com.cargo.sbpd.ui.adapters.ActionsDoneListAdapter;
import com.cargo.sbpd.ui.adapters.ActionsListAdapter;
import com.cargo.sbpd.ui.adapters.FoldersListAdapter;
import com.cargo.sbpd.ui.dialogs.DialogBuilder;
import com.cargo.sbpd.utils.FormatUtils;

import java.util.Date;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.cargo.sbpd.jobs.CancelService.ACTION_CANCEL;

/**
 *
 * This class has some work to be done until it's clean. The UI is ugly and the data displayed is not always good.
 */
public class DialogProgressSyncActivity extends AppCompatActivity implements LifecycleRegistryOwner, INetworkSpeedListener {

    private TextView mCurrentFolder;
    private TextView mCurrentFile;
    private TextView mCurrentSize;
    private TextView mCurrentPercent;
    private ImageView mCurrentActionIcon;
    private RecyclerView mCurrentListTodo;
    private ActionsListAdapter mTodoAdapter;
    private LiveData<List<ActionToDo>> mTodoLiveData;
    private TextView mLeftTitle;
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
    private String mLastSpeed;
    private List<FolderToSync> mListFoldersToSync;
    private int mIndexFolder;
    private Button mShowAllFolders;
    private Scheduler mScheduler;
    private Date mDate;
    private LiveData<List<ActionDone>> mCurrentFileLiveData;
    private ProgressBar mCurrentProgress;
    private Observer<List<ActionDone>> mObserver;
    private TextView mSpeedUp;
    private TextView mSpeedDown;
    private NetworkListener mNetworkListener;
    private FoldersListAdapter mListFoldersAdapter;
    private Disposable mSubscription;
    private View mCurrentTitle;
    private View mCurrentCard;
    private ActionsDoneListAdapter mListActionsDoneAdapter;
    private LiveData<List<ActionDone>> mHistoriqueLiveData;
    private Observer<List<ActionDone>> mObserverActionsDone;
    private View mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_progress_sync);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.update);

        //todo replace with butterknife
        //region findviewbyid
        mCurrentFolder = (TextView) findViewById(R.id.sbpd_folder_to_sync);
        mCurrentFile = (TextView) findViewById(R.id.sbpd_item_action_file);
        mCurrentSize = (TextView) findViewById(R.id.sbpd_item_action_size);
        mCurrentPercent = (TextView) findViewById(R.id.sbpd_item_action_percent);
        mCurrentActionIcon = (ImageView) findViewById(R.id.sbpd_item_action_icon);
        mCurrentListTodo = (RecyclerView) findViewById(R.id.sbpd_item_list_todo);
        mShowAllFolders = (Button) findViewById(R.id.spbd_show_all_folders);
        mCurrentProgress = (ProgressBar) findViewById(R.id.spbd_item_progress);
        mLeftTitle = (TextView) findViewById(R.id.sbpd_item_left_to_update);
        mSpeedUp = (TextView) findViewById(R.id.spbd_speed_up);
        mSpeedDown = (TextView) findViewById(R.id.spbd_speed_down);
        mCurrentTitle = findViewById(R.id.sbpd_title_current);
        mCurrentCard = findViewById(R.id.sbpd_card_current);
        //endregion

        mCurrentListTodo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mCurrentListTodo.setAdapter(mTodoAdapter = new ActionsListAdapter());

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(view -> {
            RxBus.getRxBusSingleton().send(new RxMessage(this.getClass().getSimpleName(), ACTION_CANCEL,
                    RxMessage.Type.REQUEST, new Date(System.currentTimeMillis())));
        });

        mShowAllFolders.setOnClickListener(view -> {
            DialogBuilder.showListDialog(DialogProgressSyncActivity.this,
                    getString(R.string.sbpd_list_folders_to_sync), mListFoldersAdapter);
        });
        findViewById(R.id.sbpd_historique).setOnClickListener(view ->
                DialogBuilder.showListDialog(DialogProgressSyncActivity.this,
                        getString(R.string.sbpd_historique), mListActionsDoneAdapter));

        mNetworkListener = new NetworkListener(this);

        mListFoldersAdapter = new FoldersListAdapter(this);
        mListActionsDoneAdapter = new ActionsDoneListAdapter(this);


        mTodoLiveData = AppDatabase.getInstance().localFileDao().getActionsToDo();
        mHistoriqueLiveData = AppDatabase.getInstance().actionDoneDao().getAll();
        observeTodo();
    }

    private void observeTodo() {
        if (mTodoLiveData != null && !mTodoLiveData.hasObservers()) {
            mTodoLiveData.observe(DialogProgressSyncActivity.this, actionToDo2s -> {
                        mTodoAdapter.dataChanged(actionToDo2s);
                        mLeftTitle.setText("Restant (" + mTodoAdapter.getItemCount() + ") :");
                    }
            );
        } else if (mTodoLiveData == null) {
            Log.d("LiveData", "null");
        }
    }

    private void observeLastActionDone() {
        if (mCurrentFileLiveData != null) {

            mCurrentFileLiveData.removeObserver(mObserver);
            mCurrentFileLiveData.observe(DialogProgressSyncActivity.this, mObserver = o -> {
                if (o.size() == 1) {
                    mCurrentCard.setVisibility(View.VISIBLE);
                    mCurrentTitle.setVisibility(View.VISIBLE);
                    updateCurrentAction(o.get(0));
                } else if (o.size() == 0) {
                    mCurrentCard.setVisibility(View.GONE);
                    mCurrentTitle.setVisibility(View.GONE);
                }
            });
        } else {
            Log.d("LiveData", "null");
        }
    }

    private void observeHistorique() {
        if (mHistoriqueLiveData != null) {

            mHistoriqueLiveData.removeObserver(mObserverActionsDone);
            mHistoriqueLiveData.observe(DialogProgressSyncActivity.this, mObserverActionsDone = o -> {
                mListActionsDoneAdapter.dataChanged(o);
            });
        } else {
            Log.d("LiveData", "null");
        }
    }

    private void updateFolders() {
        mCurrentFolder.setText(mListFoldersToSync.get(mIndexFolder).getDisplayName());
        mShowAllFolders.setText((mIndexFolder + 1) + " / " + mListFoldersToSync.size());
        mListFoldersAdapter.update(mListFoldersToSync, mIndexFolder);

        if (mListFoldersToSync.size() == 0) {
            mCurrentListTodo.setVisibility(View.GONE);
            mLeftTitle.setVisibility(View.GONE);
            mCurrentFolder.setText(getString(R.string.sbpd_no_sync));
            mShowAllFolders.setText("-");
            mFab.setVisibility(View.GONE);
        } else {
            mCurrentListTodo.setVisibility(View.VISIBLE);
            mLeftTitle.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkListener.startListening();
        if (mScheduler != null) {
            RxBus.getRxBusSingleton().asFlowable().unsubscribeOn(mScheduler);
        }
        Log.d("RxBus", "subscribe");

        mSubscription = RxBus.getRxBusSingleton().asFlowable()
                .subscribeOn(mScheduler = Schedulers.newThread())
                .subscribe(event -> {
                    if (event instanceof RxMessage && ((RxMessage) event).getType().equals(RxMessage.Type.RESPONSE)) {
                        RxMessage rxResponse = (RxMessage) event;
                        if (rxResponse.getSender() == null || rxResponse.getSender().equals(DialogProgressSyncActivity.class.getSimpleName())) {
                            if (rxResponse.getRequest().equals(SyncManager.REQUEST_LIST_FOLDERS)) {
                                if (rxResponse.getObject() instanceof ListFoldersToSyncState) {
                                    ListFoldersToSyncState temp = ((ListFoldersToSyncState) rxResponse.getObject());
                                    mListFoldersToSync = temp.getFoldersToSync();
                                    mIndexFolder = temp.getPosition();
                                    runOnUiThread(this::updateFolders);
                                }
                            } else if (rxResponse.getRequest().equals(SyncManager.REQUEST_START_DATE)) {
                                if (rxResponse.getObject() instanceof Date) {
                                    mDate = (Date) (rxResponse.getObject());
                                    runOnUiThread(() -> {
                                        mCurrentFileLiveData = AppDatabase.getInstance().actionDoneDao().getLastNotFinished(mDate);
                                        observeLastActionDone();
                                    });
                                }
                            }
                        } else {
                            Log.d("RxCalls", "Caller : " + rxResponse.getSender());
                        }
                    }
                });
//        RxBus.getRxBusSingleton().send(new RxRequest(DialogProgressSyncActivity.class.getSimpleName(), SyncManager.REQUEST_LIST_FOLDERS, null));
//        RxBus.getRxBusSingleton().send(new RxRequest(DialogProgressSyncActivity.class.getSimpleName(), SyncManager.REQUEST_START_DATE, null));
        new Handler().postDelayed(() -> {
            RxBus.getRxBusSingleton().send(new RxMessage(DialogProgressSyncActivity.class.getSimpleName(), SyncManager.REQUEST_LIST_FOLDERS, RxMessage.Type.REQUEST, null));
            RxBus.getRxBusSingleton().send(new RxMessage(DialogProgressSyncActivity.class.getSimpleName(), SyncManager.REQUEST_START_DATE, RxMessage.Type.REQUEST, null));
        }, 1000);
        observeHistorique();
    }

    private void updateCurrentAction(ActionDone actionDone) {
        switch (actionDone.getActionType()) {
            case LocalFileDao.ActionType.DOWNLOAD:
                mCurrentActionIcon.setImageResource(R.drawable.ic_file_download_black_24dp);
                break;
            case LocalFileDao.ActionType.UPLOAD:
                mCurrentActionIcon.setImageResource(R.drawable.ic_file_upload_black_24dp);
                break;
            case LocalFileDao.ActionType.DELETE:
                mCurrentActionIcon.setImageResource(R.drawable.ic_close_black_24dp_vector);
                break;
        }
        mCurrentFile.setText(actionDone.getRemoteFilePath());
        mCurrentSize.setText(Formatter.formatFileSize(this, actionDone.getSizeTransferred()) + " / " + Formatter.formatFileSize(this, actionDone.getSize()));
        double percent = (actionDone.getSizeTransferred() / (actionDone.getSize() * 1.0)) * 100;
        mCurrentPercent.setText(FormatUtils.formatPourcentage(percent, 2));
        mCurrentProgress.setMax(10000);
        mCurrentProgress.setProgress((int) (percent * 10));
    }


    @Override
    protected void onPause() {
        RxBus.getRxBusSingleton().asFlowable().unsubscribeOn(mScheduler);
        mScheduler = null;
        mNetworkListener.stopListening();
        super.onPause();
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mLifecycleRegistry;
    }

    @Override
    protected void onStop() {
        if (mSubscription != null && !mSubscription.isDisposed()) {
            mSubscription.dispose();
        }
        super.onStop();
    }

    @Override
    public void updateUI(NetworkSpeed speed) {
        mLastSpeed = "Download : " + Formatter.formatFileSize(this, (long) speed.getSpeedDlApp())
                + "/s - Upload : " + Formatter.formatFileSize(this, (long) speed.getSpeedUpApp()) + "/s";

        runOnUiThread(() -> {
            mSpeedUp.setText(Formatter.formatFileSize(this, (long) speed.getSpeedUpApp()) + "/s");
            mSpeedDown.setText(Formatter.formatFileSize(this, (long) speed.getSpeedDlApp()) + "/s");
        });
    }
}
