package com.fintech_school.currency_trader.util;

import android.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final Long WEEK = 7L * 24 * 60 * 60 * 1000;
    public static final Long TWO_WEEKS = 14L * 24 * 60 * 60 * 1000;
    public static final Long MONTH = 31L * 24 * 60 * 60 * 1000;

    public static Date getDate(String string, String format) {
        try {
            return new SimpleDateFormat(format, Locale.ENGLISH).parse(string);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getString(Date date, String format) {
        return new SimpleDateFormat(format, Locale.ENGLISH).format(date);
    }

    public static Pair<Date,Date> getMaxRange(Date startDate, Date endDate) {
        if (endDate.before(startDate)) {
            Date tempDate = startDate;
            startDate = endDate;
            endDate = tempDate;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Pair<>(roundDate(startDate), calendar.getTime());
    }

    public static ArrayList<Date> getDateList(Date startDate, Date endDate) {
        startDate = roundDate(startDate);
        endDate = roundDate(endDate);
        ArrayList<Date> dates = new ArrayList<>();
        while (startDate.before(endDate)) {
            startDate = new Date(startDate.getTime() + 24 * 60 * 60 * 1000);
            dates.add(startDate);
        }
        return dates;
    }

    private static Date roundDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
