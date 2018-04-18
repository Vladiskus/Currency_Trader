package com.fintech_school.currency_trader.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.fintech_school.currency_trader.data.Currency;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addCurrency(Currency currency);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addCurrencies(List<Currency> currencies);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateCurrency(Currency currency);

    @Query("UPDATE currencies SET value = :value  WHERE name = :name")
    void updateCurrencyValue(String name, double value);

    @Query("SELECT * FROM currencies")
    Flowable<List<Currency>> getCurrencies();

    @Query("SELECT * FROM currencies WHERE name IN (:names)")
    Flowable<List<Currency>> getSpecificCurrencies(List<String> names);
}
