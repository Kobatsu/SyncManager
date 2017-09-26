package com.cargo.demoapp;

import android.Manifest;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cargo.sbpd.model.AppDatabase;
import com.cargo.sbpd.sync.networklistener.INetworkSpeedListener;
import com.cargo.sbpd.model.objects.ActionDone;
import com.cargo.sbpd.model.objects.ActionToDo;
import com.cargo.sbpd.model.objects.NetworkSpeed;
import com.cargo.sbpd.sync.networklistener.NetworkListener;
import com.cargo.sbpd.ui.activities.DialogProgressSyncActivity;
import com.cargo.sbpd.ui.adapters.ActionsDoneListAdapter;
import com.cargo.sbpd.ui.adapters.ActionsListAdapter;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Only used at start. Kept for the code using LiveData but it will be deleted soon or later.
 */
@Deprecated
public class MainActivity extends AppCompatActivity implements LifecycleRegistryOwner, INetworkSpeedListener {

    public static final int REQ_EXTERNAL_STORAGE = 123;
    private RecyclerView mActionsToDo;
    private ActionsListAdapter mTodoAdapter;
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    private LiveData<List<ActionToDo>> mTodoLiveData;
    private NetworkListener mNetworkListener;
    private ActionsDoneListAdapter mDoneAdapter;
    private LiveData<List<ActionDone>> mDoneLiveData;
    private TextView mActionsToDoTitle;
    private TextView mActionsDoneTitle;
    private String mLastSpeed;
    private RecyclerView mActionsDone;
    private Date mStartDate = new Date(System.currentTimeMillis());
    private Observer<List<ActionDone>> mObserverCustom;
    private LiveData<List<ActionDone>> mCustomLiveData;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AppDatabase.exportDatabase(MainActivity.this, "boom.db");
        });

        mActionsToDo = findViewById(R.id.list_actions_to_do);
        mActionsToDo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mActionsToDo.setAdapter(mTodoAdapter = new ActionsListAdapter());

        mActionsDone = findViewById(R.id.list_actions_done);
        mActionsDone.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mActionsDone.setAdapter(mDoneAdapter = new ActionsDoneListAdapter(this));
        mDoneLiveData = AppDatabase.getInstance().actionDoneDao().getAll();

        mActionsToDoTitle = (TextView) findViewById(R.id.actions_to_do);
        mActionsDoneTitle = (TextView) findViewById(R.id.actions_done);

        findViewById(R.id.clear_database).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DialogProgressSyncActivity.class);
            startActivity(intent);
        });

        mNetworkListener = new NetworkListener(this);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQ_EXTERNAL_STORAGE);
        }
        mCustomLiveData = AppDatabase.getInstance().actionDoneDao().getAllFromDate(mStartDate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(MainActivity.this, "Permission not granted !", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            forceCrash(mActionsDoneTitle);
        }

        return super.onOptionsItemSelected(item);
    }

    public void forceCrash(View view) {
        throw new RuntimeException("This is a crash");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStartDate = new Date(System.currentTimeMillis());
        mCustomLiveData.removeObservers(this);
        mCustomLiveData = AppDatabase.getInstance().actionDoneDao().getAllFromDate(mStartDate);
        observeTodo();

        if (mDoneLiveData != null && !mDoneLiveData.hasObservers()) {
            mDoneLiveData.observe(this, actionsDone -> {
                mDoneAdapter.dataChanged(actionsDone);
                mActionsDoneTitle.setText("DONE - " + mDoneAdapter.getItemCount() + " actions ");
            });
        } else if (mDoneLiveData == null) {
            Log.d("LiveData", "null");
        }

        if (mCustomLiveData != null) {
            if (mCustomLiveData.hasObservers()) {
                mCustomLiveData.removeObserver(mObserverCustom);
            }
            mCustomLiveData.observe(this, mObserverCustom = actions -> {
//                for (ActionDone action : actions) {
//                    if (action.getEndTransfert() > 0 && TextUtils.isEmpty(action.getException())) {
//                        if (action.getLocalFilePath().endsWith("food 06.jpg")) {
//                            Log.d("FileDownloaded", "Yup date : " + new SimpleDateFormat("HH:mm:ss").format(action.getEndTransfert()));
//                            Intent intent = new Intent();
//                            intent.setAction(android.content.Intent.ACTION_VIEW);
//                            File f = new File(action.getLocalFilePath());
//                            Uri uri = getUri(f.getAbsolutePath());
//                            intent.setDataAndType(uri, getMimeType(uri, MainActivity.this));
//                            startActivity(intent);
//                        }
//                    }
//                }
            });
        }

    }

    private Uri getUri(String path) {
        Uri photoURI;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            photoURI = Uri.fromFile(new File(path));
        } else {
            photoURI = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider", new File(path));
        }
        return photoURI;
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(Uri uri, Context context) {
        ContentResolver cR = context.getContentResolver();
        return cR.getType(uri);
    }


    private void observeTodo() {
        if (mTodoLiveData != null && !mTodoLiveData.hasObservers()) {
            mTodoLiveData.observe(this, actionToDo2s -> {
                        mTodoAdapter.dataChanged(actionToDo2s);
                        mActionsToDoTitle.setText("TODO - " + mTodoAdapter.getItemCount() + " actions " + mLastSpeed);
                    }
            );
        } else if (mTodoLiveData == null) {
            Log.d("LiveData", "null");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void updateUI(NetworkSpeed speed) {
        mLastSpeed = "Download : " + Formatter.formatFileSize(this, (long) speed.getSpeedDlApp())
                + "/s - Upload : " + Formatter.formatFileSize(this, (long) speed.getSpeedUpApp()) + "/s";

        runOnUiThread(() -> mActionsToDoTitle.setText("TODO - " + mTodoAdapter.getItemCount() + " actions " + mLastSpeed));

        Log.d("NetworkSpeed", "Dl App : " + Formatter.formatFileSize(this, (long) speed.getSpeedDlApp())
                + "/s Dl Global : " + Formatter.formatFileSize(this, (long) speed.getSpeedDlGlobal())
                + "/s Up App : " + Formatter.formatFileSize(this, (long) speed.getSpeedUpApp())
                + "/s Up Global : " + Formatter.formatFileSize(this, (long) speed.getSpeedUpGlobal()) + "/s");
    }
}
