package com.fintech_school.currency_trader.data;

import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;

public class Filter {

    public static final String KEY = "filter";

    public enum Period {
        ALL_TIME,
        LAST_WEEK,
        LAST_MONTH
    }

    private Date startDate;
    private Date endDate;
    private Period period;
    private ArrayList<String> selectedCurrencies;

    public Filter(Period period) {
        this.period = period;
        refreshDates();
    }

    public void setComplexDate(Date startDate, Date endDate) {
        period = null;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setSimpleDate(@NonNull Period period) {
        this.period = period;
        refreshDates();
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }

    public void setSelectedCurrencies(ArrayList<String> selectedCurrencies) {
        this.selectedCurrencies = selectedCurrencies;
    }

    public Date getStartDate() {
        refreshDates();
        return startDate;
    }

    public Date getEndDate() {
        refreshDates();
        return endDate;
    }

    public ArrayList<String> getSelectedCurrencies() {
        return selectedCurrencies;
    }

    private void refreshDates() {
        if (period == null) return;
        switch (period) {
            case ALL_TIME:
                startDate = new Date(0);
                break;
            case LAST_WEEK:
                startDate = new Date(new Date().getTime() - DateUtil.WEEK);
                break;
            case LAST_MONTH:
                startDate = new Date(new Date().getTime() - DateUtil.MONTH);
                break;
        }
        endDate = new Date();
    }

    public String toString(String[] names) {
        if (period == null) return DateUtil.getString(startDate, "dd.MM.yyyy")
                + " - " + DateUtil.getString(endDate, "dd.MM.yyyy");
        else switch (period) {
            case LAST_MONTH:
                return names[2];
            case LAST_WEEK:
                return names[1];
            default:
                return names[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filter filter = (Filter) o;
        if (period != filter.period) return false;
        return selectedCurrencies != null ? selectedCurrencies.equals(filter.selectedCurrencies) :
                filter.selectedCurrencies == null;
    }

    @Override
    public int hashCode() {
        int result = period != null ? period.hashCode() : 0;
        result = 31 * result + (selectedCurrencies != null ? selectedCurrencies.hashCode() : 0);
        return result;
    }
}
