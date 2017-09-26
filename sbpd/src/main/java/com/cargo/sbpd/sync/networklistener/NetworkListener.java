package com.cargo.sbpd.sync.networklistener;

import android.net.TrafficStats;

import com.cargo.sbpd.model.objects.NetworkSpeed;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by leb on 02/08/2017.
 */
public class NetworkListener {

    private final INetworkSpeedListener mListener;
    private long mInitTxGlobal;
    private long mInitRxGlobal;
    private long mInitTxApp;
    private long mInitRxApp;
    private float mOldTx;
    private float mOldRx;
    private float mOldTxGlobal;
    private float mOldRxGlobal;
    private Timer mTimer;

    public NetworkListener(INetworkSpeedListener listener) {
        mListener = listener;
    }

    public void startListening() {
        final int uid = android.os.Process.myUid();
        mInitTxApp = TrafficStats.getUidTxBytes(uid);
        mInitRxApp = TrafficStats.getUidRxBytes(uid);

        mInitTxGlobal = TrafficStats.getTotalTxBytes();
        mInitRxGlobal = TrafficStats.getTotalRxBytes();

        mTimer = new Timer();
        final long period = 200;
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                float vitesseUp;
                float vitesseDl;
                float vitesseUpGlobal;
                float vitesseDlGlobal;
                float valTemp;

                NetworkSpeed networkSpeed = new NetworkSpeed();

                valTemp = TrafficStats.getUidTxBytes(uid);
                vitesseUp = ((valTemp - mOldTx) /  period) * 1000;
                networkSpeed.setSpeedUpApp(vitesseUp);
                mOldTx = valTemp;

                valTemp = TrafficStats.getUidRxBytes(uid);
                vitesseDl = ((valTemp - mOldRx)  /  period) * 1000;
                networkSpeed.setSpeedDlApp(vitesseDl);
                mOldRx = valTemp;

                valTemp = TrafficStats.getTotalTxBytes();
                vitesseUpGlobal = ((valTemp - mOldTxGlobal) /  period) * 1000;
                networkSpeed.setSpeedUpGlobal(vitesseUpGlobal);
                mOldTxGlobal = valTemp;

                valTemp = TrafficStats.getTotalRxBytes();
                vitesseDlGlobal = ((valTemp - mOldRxGlobal)  /  period) * 1000;
                networkSpeed.setSpeedDlGlobal(vitesseDlGlobal);
                mOldRxGlobal = valTemp;

                mListener.updateUI(networkSpeed);
            }
        }, 0, period);//put here time 1000 milliseconds=1 second
    }

    public void stopListening() {
        mTimer.cancel();
    }
}
