package com.fintech_school.currency_trader.history.transaction_filter_screen;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.fintech_school.currency_trader.data.Transaction;
import com.fintech_school.currency_trader.parents.BaseViewModel;
import com.fintech_school.currency_trader.data.Filter;
import com.fintech_school.currency_trader.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FilterViewModel extends BaseViewModel implements OnUsedCurrencyClickListener {

    private ArrayList<String> usedCurrencies;
    private ObservableEmitter<Boolean> loadingStateEmitter;
    private ObservableEmitter<Boolean> selectionDroppedEmitter;
    private ObservableEmitter<String> periodSelectedEmitter;
    private CompletableEmitter navigationEmitter;
    private Filter filter;

    public FilterViewModel(@NonNull Application application) {
        super(application);
    }

    public Flowable<ArrayList<String>> getUsedCurrenciesSource() {
        if (usedCurrencies != null) return Flowable.just(usedCurrencies);
        return getCurrencyRepository().getTransactions()
                .observeOn(AndroidSchedulers.mainThread()).map((transactions -> {
                    Collections.reverse(transactions);
                    usedCurrencies = new ArrayList<>();
                    for (Transaction transaction : transactions) {
                        if (!usedCurrencies.contains(transaction.getBaseCurrencyName()))
                            usedCurrencies.add(transaction.getBaseCurrencyName());
                        if (!usedCurrencies.contains(transaction.getTargetCurrencyName()))
                            usedCurrencies.add(transaction.getTargetCurrencyName());
                    }
                    filter = getCurrencyRepository().loadFilter();
                    if (filter.getSelectedCurrencies() == null)
                        filter.setSelectedCurrencies(new ArrayList<>(usedCurrencies));
                    else if (filter.getSelectedCurrencies().size() == 0) selectionDroppedEmitter.onNext(false);
                    periodSelectedEmitter.onNext(filter.toString(getApplication().getApplicationContext()));
                    loadingStateEmitter.onNext(false);
                    return usedCurrencies;
                }));
    }

    public Observable<Boolean> getLoadingStateSource() {
        return Observable.create(emitter -> {
            loadingStateEmitter = emitter;
            if (usedCurrencies == null) loadingStateEmitter.onNext(true);
        });
    }

    public Observable<Boolean> getSelectionDroppedSource() {
        return Observable.create(emitter -> selectionDroppedEmitter = emitter);
    }

    public Observable<String> getPeriodSelectedSource() {
        return Observable.create(emitter -> periodSelectedEmitter = emitter);
    }

    public Completable getNavigationTriggerSource() {
        return Completable.create(emitter -> navigationEmitter = emitter);
    }

    public Filter getFilter() {
        return filter;
    }

    public void onSimpleFilterSelected(int position) {
        if (filter == null) return;
        if (position == 0) filter.setSimpleDate(Filter.Period.ALL_TIME);
        else if (position == 1) filter.setSimpleDate(Filter.Period.LAST_WEEK);
        else filter.setSimpleDate(Filter.Period.LAST_MONTH);
        periodSelectedEmitter.onNext(filter.toString(getApplication().getApplicationContext()));
    }

    public void onComplexFilterSelected(Date startDate, Date endDate) {
        Pair<Date, Date> pair = DateUtil.getMaxRange(startDate, endDate);
        filter.setComplexDate(pair.first, pair.second);
        filter.setPeriod(null);
        periodSelectedEmitter.onNext(filter.toString(getApplication().getApplicationContext()));
    }

    public void onMainCheckedChange(boolean isChecked) {
        if (isChecked) filter.setSelectedCurrencies(new ArrayList<>(usedCurrencies));
        else filter.getSelectedCurrencies().clear();
        selectionDroppedEmitter.onNext(isChecked);
    }

    @Override
    public void onCheckedChange(String currency, boolean isChecked) {
        if (isChecked) filter.getSelectedCurrencies().add(currency);
        else filter.getSelectedCurrencies().remove(currency);
    }

    public void onDoneClick() {
        if (filter.getSelectedCurrencies().containsAll(usedCurrencies)) filter.setSelectedCurrencies(null);
        getCurrencyRepository().saveFilter(filter);
        navigationEmitter.onComplete();
    }
}