package com.jamburger.kitter.utilities;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    public static DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);

    public static String getTimeDifference(String dateId) {
        try {
            Date date = format.parse(dateId);
            Date now = new Date();
            assert date != null;
            long difference_In_Millis = now.getTime() - date.getTime();
            long difference_In_Seconds = difference_In_Millis / 1000;
            long difference_In_Minutes = difference_In_Seconds / 60;
            long difference_In_Hours = difference_In_Minutes / 60;
            long difference_In_Days = difference_In_Hours / 24;

            String timeText = "";
            if (difference_In_Minutes == 0) {
                timeText = difference_In_Seconds + " second";
                if (difference_In_Seconds > 1) timeText += "s";
            } else if (difference_In_Hours == 0) {
                timeText = difference_In_Minutes + " minute";
                if (difference_In_Minutes > 1) timeText += "s";
            } else if (difference_In_Days == 0) {
                timeText = difference_In_Hours + " hour";
                if (difference_In_Hours > 1) timeText += "s";
            } else {
                timeText = difference_In_Days + " day";
                if (difference_In_Days > 1) timeText += "s";
            }
            timeText += " ago";
            return timeText;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getDateMonth(String dateId) {
        String[] strings = dateId.split("-", 6);
        int month = Integer.parseInt(strings[1]);
        int date = Integer.parseInt(strings[2]);
        return date + " " + new DateFormatSymbols().getMonths()[month - 1];
    }

    public static String getCurrentTime() {
        return format.format(new Date());
    }

    public static String getHoursMinutes(String dateId) {
        String[] strings = dateId.split("-", 6);
        int hours = Integer.parseInt(strings[3]);
        int minutes = Integer.parseInt(strings[4]);
        String meridiem = hours >= 12 ? "PM" : "AM";
        hours %= 12;
        if (hours == 0)
            hours = 12;
        return hours + ":" + minutes + " " + meridiem;
    }
}
