package com.jamburger.kitter.utilities

import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatter {
    private var format: DateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH)

    fun getTimeDifference(dateId: String?, returnShorter: Boolean): String? {
        try {
            val date = dateId?.let { format.parse(it) }
            val now = Date()
            val differenceInMillis = now.time - date!!.time
            val differenceInSeconds = differenceInMillis / 1000
            val differenceInMinutes = differenceInSeconds / 60
            val differenceInHours = differenceInMinutes / 60
            val differenceInDays = differenceInHours / 24
            val differenceInWeeks = differenceInDays / 7

            var timeText: String
            if (differenceInMinutes == 0L) {
                timeText = "$differenceInSeconds second"
                if (differenceInSeconds > 1) timeText += "s"
                if (returnShorter) timeText = differenceInSeconds.toString() + "s"
            } else if (differenceInHours == 0L) {
                timeText = "$differenceInMinutes minute"
                if (differenceInMinutes > 1) timeText += "s"
                if (returnShorter) timeText = differenceInMinutes.toString() + "m"
            } else if (differenceInDays == 0L) {
                timeText = "$differenceInHours hour"
                if (differenceInHours > 1) timeText += "s"
                if (returnShorter) timeText = differenceInHours.toString() + "h"
            } else if (differenceInWeeks == 0L) {
                timeText = "$differenceInDays day"
                if (differenceInDays > 1) timeText += "s"
                if (returnShorter) timeText = differenceInDays.toString() + "d"
            } else {
                timeText = "$differenceInWeeks week"
                if (differenceInWeeks > 1) timeText += "s"
                if (returnShorter) timeText = differenceInWeeks.toString() + "w"
            }
            if (!returnShorter) timeText += " ago"
            return timeText
        } catch (e: Exception) {
            return null
        }
    }

    fun getDateMonth(dateId: String): String {
        val strings = dateId.split("-".toRegex(), limit = 6).toTypedArray()
        val month = strings[1].toInt()
        val date = strings[2].toInt()
        return date.toString() + " " + DateFormatSymbols().months[month - 1]
    }

    val currentTime: String
        get() = format.format(Date())

    fun getHoursMinutes(dateId: String): String {
        val strings = dateId.split("-".toRegex(), limit = 6).toTypedArray()
        var hours = strings[3].toInt()
        val minutes = strings[4].toInt()
        val meridiem = if (hours >= 12) "PM" else "AM"
        hours %= 12
        if (hours == 0) hours = 12
        return "$hours:$minutes $meridiem"
    }
}
