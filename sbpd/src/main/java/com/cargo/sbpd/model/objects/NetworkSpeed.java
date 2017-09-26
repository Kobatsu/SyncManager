package com.cargo.sbpd.model.objects;

/**
 * Created by leb on 02/08/2017.
 *
 * The values are in bits/sec
 */

public class NetworkSpeed {
    private float mSpeedUpApp;
    private float mSpeedDlApp;
    private float mSpeedUpGlobal;
    private float mSpeedDlGlobal;

    public NetworkSpeed(float speedUpApp, float speedDlApp, float speedUpGlobal, float speedDlGlobal) {
        mSpeedUpApp = speedUpApp;
        mSpeedDlApp = speedDlApp;
        mSpeedUpGlobal = speedUpGlobal;
        mSpeedDlGlobal = speedDlGlobal;
    }

    public NetworkSpeed() {
    }

    public float getSpeedUpApp() {
        return mSpeedUpApp;
    }

    public void setSpeedUpApp(float speedUpApp) {
        mSpeedUpApp = speedUpApp;
    }

    public float getSpeedDlApp() {
        return mSpeedDlApp;
    }

    public void setSpeedDlApp(float speedDlApp) {
        mSpeedDlApp = speedDlApp;
    }

    public float getSpeedUpGlobal() {
        return mSpeedUpGlobal;
    }

    public void setSpeedUpGlobal(float speedUpGlobal) {
        mSpeedUpGlobal = speedUpGlobal;
    }

    public float getSpeedDlGlobal() {
        return mSpeedDlGlobal;
    }

    public void setSpeedDlGlobal(float speedDlGlobal) {
        mSpeedDlGlobal = speedDlGlobal;
    }

    @Override
    public String toString() {
        return "NetworkSpeed{" +
                "mSpeedUpApp=" + mSpeedUpApp +
                ", mSpeedDlApp=" + mSpeedDlApp +
                ", mSpeedUpGlobal=" + mSpeedUpGlobal +
                ", mSpeedDlGlobal=" + mSpeedDlGlobal +
                '}';
    }
}
