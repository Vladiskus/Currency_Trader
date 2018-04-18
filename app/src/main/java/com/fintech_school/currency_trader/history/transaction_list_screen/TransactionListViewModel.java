package com.fintech_school.currency_trader.history.transaction_list_screen;

import android.app.Application;
import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.data.Transaction;
import com.fintech_school.currency_trader.parents.BaseViewModel;
import com.fintech_school.currency_trader.data.Filter;

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class TransactionListViewModel extends BaseViewModel {

    private ArrayList<Transaction> transactions;
    private ObservableEmitter<Boolean> loadingStateEmitter;
    private ObservableEmitter<Object> navigationEmitter;
    private ObservableEmitter<String> filterNameEmitter;
    private Filter filter;

    public TransactionListViewModel(@NonNull Application application) {
        super(application);
    }

    public Flowable<ArrayList<Transaction>> getTransactionsSource() {
        filter = getCurrencyRepository().loadFilter();
        filterNameEmitter.onNext(filter.toString(getApplication().getApplicationContext()));
        Flowable<ArrayList<Transaction>> source = getCurrencyRepository().getTransactions()
                .observeOn(AndroidSchedulers.mainThread()).map((transactions -> {
                    this.transactions = (ArrayList<Transaction>) transactions;
                    loadingStateEmitter.onNext(false);
                    return getFilteredTransactions();
                }));
        if (transactions == null) return source;
        else return Flowable.concat(Flowable.just(getFilteredTransactions()), source);
    }

    private ArrayList<Transaction> getFilteredTransactions() {
        ArrayList<String> selectedCurrencies = filter.getSelectedCurrencies();
        ArrayList<Transaction> filteredTransactions = new ArrayList<>(transactions);
        for (Transaction transaction : transactions) {
            boolean isCurrencySelected = true;
            if (selectedCurrencies != null) isCurrencySelected =
                    selectedCurrencies.contains(transaction.getBaseCurrencyName())
                    || selectedCurrencies.contains(transaction.getTargetCurrencyName());
            boolean isDateSelected = transaction.getDate().getTime() > filter.getStartDate().getTime()
                    && transaction.getDate().getTime() < filter.getEndDate().getTime();
            if (!(isCurrencySelected && isDateSelected)) filteredTransactions.remove(transaction);
        }
        Collections.reverse(filteredTransactions);
        return filteredTransactions;
    }

    public Observable<Boolean> getLoadingStateSource() {
        return Observable.create(emitter -> {
            loadingStateEmitter = emitter;
            if (transactions == null) loadingStateEmitter.onNext(true);
        });
    }

    public Observable<Object> getNavigationTriggerSource() {
        return Observable.create(emitter -> navigationEmitter = emitter);
    }

    public Observable<String> getFilterNameSource() {
        return Observable.create(emitter -> filterNameEmitter = emitter);
    }

    public Filter getFilter() {
        return filter;
    }

    public void onFilterClick() {
        navigationEmitter.onNext(new Object());
    }
}