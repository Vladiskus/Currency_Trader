package com.fintech_school.currency_trader.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.fintech_school.currency_trader.data.Transaction;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface TransactionDao {

    @Query("SELECT * FROM transactions")
    Flowable<List<Transaction>> getTransactions();

    @Insert
    void addTransaction(Transaction transaction);
}
