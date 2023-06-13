package com.jamburger.kitter.utilities;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeFormatter {
    public static DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);

    public static String getTimeDifference(String dateId, boolean returnShorter) {
        try {
            Date date = format.parse(dateId);
            Date now = new Date();
            assert date != null;
            long differenceInMillis = now.getTime() - date.getTime();
            long differenceInSeconds = differenceInMillis / 1000;
            long differenceInMinutes = differenceInSeconds / 60;
            long differenceInHours = differenceInMinutes / 60;
            long differenceInDays = differenceInHours / 24;
            long differenceInWeeks = differenceInDays / 7;

            String timeText = "";
            if (differenceInMinutes == 0) {
                timeText = differenceInSeconds + " second";
                if (differenceInSeconds > 1) timeText += "s";
                if (returnShorter) timeText = differenceInSeconds + "s";
            } else if (differenceInHours == 0) {
                timeText = differenceInMinutes + " minute";
                if (differenceInMinutes > 1) timeText += "s";
                if (returnShorter) timeText = differenceInMinutes + "m";
            } else if (differenceInDays == 0) {
                timeText = differenceInHours + " hour";
                if (differenceInHours > 1) timeText += "s";
                if (returnShorter) timeText = differenceInHours + "h";
            } else if (differenceInWeeks == 0) {
                timeText = differenceInDays + " day";
                if (differenceInDays > 1) timeText += "s";
                if (returnShorter) timeText = differenceInDays + "d";
            } else {
                timeText = differenceInWeeks + " week";
                if (differenceInWeeks > 1) timeText += "s";
                if (returnShorter) timeText = differenceInWeeks + "w";
            }
            if (!returnShorter) timeText += " ago";
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
