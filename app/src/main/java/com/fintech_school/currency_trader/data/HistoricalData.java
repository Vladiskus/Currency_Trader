package com.fintech_school.currency_trader.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity
public class HistoricalData implements Comparable<HistoricalData> {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String currency;
    private Date date;
    private double value;

    public HistoricalData(String currency, Date date, double value) {
        this.currency = currency;
        this.date = date;
        this.value = value;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public Date getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricalData that = (HistoricalData) o;
        if (!currency.equals(that.currency)) return false;
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        int result = currency.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }

    @Override
    public int compareTo(@NonNull HistoricalData o) {
        return (int) (date.getTime() - o.date.getTime());
    }
}