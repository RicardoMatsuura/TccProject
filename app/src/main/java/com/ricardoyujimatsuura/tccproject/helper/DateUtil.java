package com.ricardoyujimatsuura.tccproject.helper;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {


    public static String currentDate() {
        long date = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return simpleDateFormat.format(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String currentDateTest() {
        Instant instant = Instant.now(); // Current moment in UTC.
        ZoneId z = ZoneId.of("America/Sao_Paulo"); // Replace with the zone you know to have been intended for the input strings.
        ZonedDateTime zdtNow = instant.atZone(z); // Adjust from UTC to a time zone.
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //Format the date do year-month-day
        return dateTimeFormatter.format(zdtNow);
    }
    public static Date timeStampDate() {
        Date timeStampDate = new Date();
        return timeStampDate;
    }

    /*@RequiresApi(api = Build.VERSION_CODES.O)
    public static String oneWeekLaterTimeStamp() {
        Instant instant = Instant.now(); // Current moment in UTC.
        ZoneId z = ZoneId.of("America/Sao_Paulo"); // Replace with the zone you know to have been intended for the input strings.
        ZonedDateTime zdtNow = instant.atZone(z); // Adjust from UTC to a time zone.
        ZonedDateTime zdtWeekAfter = zdtNow.plusDays(7) ; // Count a week later the current date
        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Instant instantZoned = zdtWeekAfter.toInstant();
        Date timestamp = java.sql.Timestamp.from(instantZoned);
        return timestamp.toString();

    }*/
}
