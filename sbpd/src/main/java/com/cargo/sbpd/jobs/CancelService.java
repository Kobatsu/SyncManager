package com.cargo.sbpd.jobs;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.cargo.sbpd.bus.RxBus;
import com.cargo.sbpd.bus.RxMessage;

import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class CancelService extends IntentService {
    public static final String ACTION_CANCEL = "com.cargo.sbpd.jobs.action.CANCEL";

    public CancelService() {
        super("CancelService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CANCEL.equals(action)) {
                Log.d("CancelService", "Called");
                // Write in a bus
                RxBus.getRxBusSingleton().send(new RxMessage("CancelService", ACTION_CANCEL,
                        RxMessage.Type.REQUEST, new Date(System.currentTimeMillis())));
            }
        }
    }
}
