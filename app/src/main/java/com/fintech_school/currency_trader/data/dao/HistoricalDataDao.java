package com.fintech_school.currency_trader.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.fintech_school.currency_trader.data.HistoricalData;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface HistoricalDataDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addHistoricalData(HistoricalData data);

    @Query("DELETE FROM historicalData WHERE date < :validPeriod")
    void removeOldHistoricalData(long validPeriod);

    @Query("SELECT * FROM historicalData WHERE currency = :currency AND date = :date")
    Flowable<List<HistoricalData>> getHistoricalData(String currency, Date date);
}
