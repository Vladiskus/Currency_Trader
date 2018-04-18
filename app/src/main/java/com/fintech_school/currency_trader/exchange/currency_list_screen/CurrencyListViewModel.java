package com.fintech_school.currency_trader.exchange.currency_list_screen;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.parents.BaseViewModel;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CurrencyListViewModel extends BaseViewModel implements OnCurrencyClickListener {

    private ArrayList<Currency> currencies;
    private Pair<Currency, Integer> selectedCurrency;
    private ObservableEmitter<Boolean> loadingStateEmitter;
    private ObservableEmitter<Pair<Currency, Integer>> selectedCurrencyEmitter;
    private ObservableEmitter<Pair<Currency, Currency>> navigationEmitter;
    private boolean updateRequested;

    public CurrencyListViewModel(@NonNull Application application) {
        super(application);
    }

    @SuppressWarnings("UnusedAssignment")
    public Observable<ArrayList<Currency>> getCurrenciesSource() {
        Observable<ArrayList<Currency>> source = getCurrencyRepository().getCurrencies(null)
                .observeOn(AndroidSchedulers.mainThread()).map((currencies -> {
                    if (updateRequested || this.currencies == null) this.currencies = currencies;
                    else for (Currency currency : this.currencies)
                        currency = currencies.get(currencies.indexOf(currency));
                    onCurrencyListReady();
                    return this.currencies;
                }));
        if (currencies != null) {
            updateRequested = true;
            onCurrencyListReady();
            return Observable.concat(Observable.just(currencies), source);
        } else return source;
    }

    private void onCurrencyListReady() {
        if (selectedCurrency != null) currencies.remove(selectedCurrency.first);
        loadingStateEmitter.onNext(false);
    }

    public Observable<Boolean> getLoadingStateSource() {
        return Observable.create(emitter -> {
            loadingStateEmitter = emitter;
            if (currencies == null) loadingStateEmitter.onNext(true);
        });
    }

    public Observable<Pair<Currency, Integer>> getSelectedCurrencySource() {
        return Observable.create(emitter -> selectedCurrencyEmitter = emitter);
    }

    public Observable<Pair<Currency, Currency>> getNavigationTriggerSource() {
        return Observable.create(emitter -> navigationEmitter = emitter);
    }

    public Completable getRefreshStateSource() {
        updateRequested = true;
        return getCurrencyRepository().downloadCurrencyValues();
    }

    public Pair<Currency, Integer> getSelectedCurrency() {
        return selectedCurrency;
    }

    public boolean isUpdateRequested() {
        return updateRequested;
    }

    public void updateIsDone() {
        updateRequested = false;
    }

    public void removeSelectedCurrency() {
        selectedCurrency = null;
    }

    @Override
    public void onCurrencyClick(Currency clickedCurrency) {
        if (selectedCurrency == null) {
            Currency secondCurrency = null;
            for (Currency currency : currencies) {
                if (secondCurrency != null) break;
                if (currency.equals(clickedCurrency)) continue;
                if (currency.isFavorite()) secondCurrency = currency;
                else if (!clickedCurrency.getName().equals("RUB")) {
                    if (currency.getName().equals("RUB")) secondCurrency = currency;
                } else if (currency.getName().equals("USD")) secondCurrency = currency;
            }
            navigationEmitter.onNext(new Pair<>(clickedCurrency, secondCurrency));
        } else navigationEmitter.onNext(new Pair<>(selectedCurrency.first, clickedCurrency));
    }

    @Override
    public boolean onLongCurrencyClick(Currency currency, int position) {
        if (selectedCurrency != null) return false;
        selectedCurrency = new Pair<>(currency, position);
        selectedCurrencyEmitter.onNext(selectedCurrency);
        return true;
    }

    @Override
    public void onFavoriteMarkClick(Currency currency) {
        getCurrencyRepository().updateFavoriteState(currency);
    }

    public void onRemoveClick() {
        selectedCurrencyEmitter.onNext(new Pair<>(null, null));
    }
}