package com.example.horizontourist.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Formatters {

    private Formatters() {
    }

    public static String formatMoney(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.##");
        return "BDT " + df.format(amount);
    }

    public static String formatDateTime(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return "";
        }
        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd"
        };
        String cleanIso = iso.replace("Z", "");
        for (String pattern : patterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.US);
                Date date = parser.parse(cleanIso);
                if (date != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);
                    return formatter.format(date);
                }
            } catch (ParseException ignored) {
            }
        }
        return iso;
    }

    public static String formatDate(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return "";
        }
        String[] patterns = new String[]{
                "yyyy-MM-dd",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm"
        };
        String cleanIso = iso.replace("Z", "");
        for (String pattern : patterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.US);
                Date date = parser.parse(cleanIso);
                if (date != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                    return formatter.format(date);
                }
            } catch (ParseException ignored) {
            }
        }
        return iso;
    }

    public static String capitalizeEnum(String enumVal) {
        if (enumVal == null || enumVal.trim().isEmpty()) {
            return "";
        }
        String s = enumVal.replace('_', ' ').trim().toLowerCase(Locale.US);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static int getCategoryIconRes(String category) {
        if (category == null) {
            return com.example.horizontourist.R.drawable.ic_nav_explore;
        }
        switch (category.toUpperCase(Locale.US)) {
            case "BUS":
            case "MICROBUS":
                return com.example.horizontourist.R.drawable.ic_bus;
            case "TRAIN":
                return com.example.horizontourist.R.drawable.ic_train;
            case "AIRPLANE":
                return com.example.horizontourist.R.drawable.ic_flight;
            case "LAUNCH":
            case "SHIP":
                return com.example.horizontourist.R.drawable.ic_boat;
            case "HOTEL":
            case "RESORT":
            case "CONVENTION_CENTER":
                return com.example.horizontourist.R.drawable.ic_hotel;
            case "BUFFET":
            case "AMUSEMENT_PARK":
                return com.example.horizontourist.R.drawable.ic_attraction;
            default:
                return com.example.horizontourist.R.drawable.ic_nav_explore;
        }
    }
}
