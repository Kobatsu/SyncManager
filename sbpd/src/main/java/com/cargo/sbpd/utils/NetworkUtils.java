package com.cargo.sbpd.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.StringDef;
import android.telephony.TelephonyManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leb on 04/08/2017.
 *
 * This class is used to store the network name in database and make statistics about it with a
 * synchro client.
 */
public class NetworkUtils {


    @StringDef({NetworkType.NETWORK_2G, NetworkType.NETWORK_3G, NetworkType.NETWORK_4G, NetworkType.NETWORK_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NetworkType {
        String NETWORK_2G = "2G";
        String NETWORK_3G = "3G";
        String NETWORK_4G = "4G";
        String NETWORK_UNKNOWN = "UNKNOWN";
        String NO_NETWORK = "NO NETWORK";
    }

    private static List<String> listMobileNetworks;

    /**
     * To get device consuming network type is 2g,3g,4g
     *
     * @param context
     * @return "2g","3g","4g" as a String based on the network type
     */
    public static @NetworkType
    String getNetworkMobileType(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NetworkType.NETWORK_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                /**
                 From this link https://goo.gl/R2HOjR ..NETWORK_TYPE_EVDO_0 & NETWORK_TYPE_EVDO_A
                 EV-DO is an evolution of the CDMA2000 (IS-2000) standard that supports high data rates.

                 Where CDMA2000 https://goo.gl/1y10WI .CDMA2000 is a family of 3G[1] mobile technology standards for sending voice,
                 data, and signaling data between mobile phones and cell sites.
                 */
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                //Log.d("Type", "3g");
                //For 3g HSDPA , HSPAP(HSPA+) are main  networktype which are under 3g Network
                //But from other constants also it will 3g like HSPA,HSDPA etc which are in 3g case.
                //Some cases are added after  testing(real) in device with 3g enable data
                //and speed also matters to decide 3g network type
                //http://goo.gl/bhtVT
                return NetworkType.NETWORK_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                //No specification for the 4g but from wiki
                //I found(LTE (Long-Term Evolution, commonly marketed as 4G LTE))
                //https://goo.gl/9t7yrR
                return NetworkType.NETWORK_4G;
            default:
                return NetworkType.NETWORK_UNKNOWN;
        }
    }

    public static String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return formatSSID(wifiInfo.getSSID());
                }
            }
        }
        return null;
    }

    private static String formatSSID(String ssid) {
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public static String getNetworkName(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (isWiFi) {
                return getWifiName(context);
            } else {
                return getNetworkMobileType(context);
            }
        } else {
            return NetworkType.NO_NETWORK;
        }
    }

    /**
     * To check device has internet
     *
     * @param context
     * @return boolean as per status
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static List<String> getListMobileNetwork() {
        if (listMobileNetworks == null) {
            listMobileNetworks = new ArrayList<>();
            listMobileNetworks.add(NetworkType.NETWORK_2G);
            listMobileNetworks.add(NetworkType.NETWORK_3G);
            listMobileNetworks.add(NetworkType.NETWORK_4G);
            listMobileNetworks.add(NetworkType.NETWORK_UNKNOWN);
            listMobileNetworks.add(NetworkType.NO_NETWORK);
        }
        return listMobileNetworks;
    }
}