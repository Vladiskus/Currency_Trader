package com.fintech_school.currency_trader.data;

import android.arch.persistence.room.Entity;

import com.fintech_school.currency_trader.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(primaryKeys = {"date"}, tableName = "transactions")
public class Transaction {

    private String baseCurrencyName;
    private String targetCurrencyName;
    private double baseCurrencyAmount;
    private double targetCurrencyAmount;
    private Date date;

    public Transaction(String baseCurrencyName, String targetCurrencyName, double baseCurrencyAmount,
                       double targetCurrencyAmount, Date date) {
        this.baseCurrencyName = baseCurrencyName;
        this.targetCurrencyName = targetCurrencyName;
        this.baseCurrencyAmount = baseCurrencyAmount;
        this.targetCurrencyAmount = targetCurrencyAmount;
        this.date = date;
    }

    public String getBaseCurrencyName() {
        return baseCurrencyName;
    }

    public String getTargetCurrencyName() {
        return targetCurrencyName;
    }

    public double getBaseCurrencyAmount() {
        return baseCurrencyAmount;
    }

    public double getTargetCurrencyAmount() {
        return targetCurrencyAmount;
    }

    public Date getDate() {
        return date;
    }

    public String getFormattedDate() {
        return DateUtil.getString(date, "dd.MM.yyyy");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        if (Double.compare(that.baseCurrencyAmount, baseCurrencyAmount) != 0) return false;
        if (Double.compare(that.targetCurrencyAmount, targetCurrencyAmount) != 0) return false;
        if (baseCurrencyName != null ? !baseCurrencyName.equals(that.baseCurrencyName) : that.baseCurrencyName != null)
            return false;
        if (targetCurrencyName != null ? !targetCurrencyName.equals(that.targetCurrencyName) : that.targetCurrencyName != null)
            return false;
        return date != null ? date.equals(that.date) : that.date == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = baseCurrencyName != null ? baseCurrencyName.hashCode() : 0;
        result = 31 * result + (targetCurrencyName != null ? targetCurrencyName.hashCode() : 0);
        temp = Double.doubleToLongBits(baseCurrencyAmount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(targetCurrencyAmount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
