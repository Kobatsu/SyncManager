package com.cargo.sbpd.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by leb on 03/08/2017.
 */

public class FormatUtils {

    private static SimpleDateFormat dateFormatterSimple = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public static String addSlash(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }


    /**
     * Permet de formatter une taille (long) correctement.
     *
     * @param size
     * @return
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Permet de formatter une taille (long) correctement.
     *
     * @param ms
     * @return
     */
    public static String readableDuration(long ms) {
        if (ms <= 0) return "0";
        if (ms <= 1000) { // less than a second
            return ms + "ms";
        } else if (ms < 60 * 1000) { // less than a minute
            return (ms / 1000) + "s";
        } else if (ms < 60 * 60 * 1000) { // less than an hour
            return new SimpleDateFormat("mm:ss").format(new Date(ms));
        } else if (ms < 60 * 60 * 1000 * 24) {
            return new SimpleDateFormat("HH:mm:ss").format(new Date(ms));
        } else {
            return "too long";
        }
    }

    public static SimpleDateFormat getDateFormatter() {
        return dateFormatterSimple;
    }


    public static String formatPourcentage(double pourcentage, int numberOfDigits) {
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(numberOfDigits);
        df.setMaximumFractionDigits(numberOfDigits);
        return df.format(pourcentage) + "%";
    }

    public static String addSlashes(String s1, String s2) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!s1.startsWith("/")) {
            stringBuilder.append("/");
        }
        stringBuilder.append(s1);
        if (!s1.endsWith("/") && !s2.startsWith("/")) {
            stringBuilder.append("/");
            stringBuilder.append(s2);
        } else if (s1.endsWith("/") && s2.startsWith("/")) {
            stringBuilder.append(s2.substring(1));
        } else {
            stringBuilder.append(s2);
        }
        return stringBuilder.toString();
    }
}
