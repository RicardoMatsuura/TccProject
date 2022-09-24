package com.ricardoyujimatsuura.tccproject.helper;

import java.text.SimpleDateFormat;

public class DateUtil {
        public static String currentDate() {
            long date = System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d/M/yyyy");
            return simpleDateFormat.format(date);

    }
}
