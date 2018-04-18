package com.fintech_school.currency_trader.repo;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.data.HistoricalData;
import com.fintech_school.currency_trader.data.dao.CurrencyDao;
import com.fintech_school.currency_trader.data.Transaction;
import com.fintech_school.currency_trader.data.dao.HistoricalDataDao;
import com.fintech_school.currency_trader.data.dao.TransactionDao;
import com.fintech_school.currency_trader.util.DateTypeConverter;

@Database(entities = {Currency.class, Transaction.class, HistoricalData.class}, version = 1, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class CurrencyDatabase extends RoomDatabase {

    public abstract CurrencyDao getCurrencyDao();
    public abstract TransactionDao getTransactionDao();
    public abstract HistoricalDataDao getHistoricalDataDao();
}
