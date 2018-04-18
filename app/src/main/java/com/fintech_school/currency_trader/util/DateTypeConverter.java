package com.fintech_school.currency_trader.util;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateTypeConverter {

    @TypeConverter
    public static Long getLong(Date date) {
        return date.getTime();
    }

    @TypeConverter
    public static Date getDate(Long time) {
        return new Date(time);
    }
}
