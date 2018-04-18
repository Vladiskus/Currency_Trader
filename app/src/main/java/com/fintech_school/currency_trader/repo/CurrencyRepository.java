package com.fintech_school.currency_trader.repo;

import android.content.SharedPreferences;

import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.data.Filter;
import com.fintech_school.currency_trader.data.HistoricalData;
import com.fintech_school.currency_trader.data.Transaction;
import com.fintech_school.currency_trader.util.DateUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CurrencyRepository {

    private CurrencyDatabase database;
    private WebService webService;
    private SharedPreferences sharedPreferences;

    @Inject
    public CurrencyRepository(CurrencyDatabase database, WebService webService,
                              SharedPreferences sharedPreferences) {
        this.database = database;
        this.webService = webService;
        this.sharedPreferences = sharedPreferences;
    }

    public Observable<ArrayList<Currency>> getCurrencies(ArrayList<String> names) {
        return Observable.create(emitter -> {
            Flowable<List<Currency>> databaseSource;
            if (names == null) databaseSource = database.getCurrencyDao().getCurrencies();
            else databaseSource = database.getCurrencyDao().getSpecificCurrencies(names);
            emitter.setDisposable(databaseSource.throttleWithTimeout(400, TimeUnit.MILLISECONDS)
                    .subscribe((currencies) -> {
                        if (currencies.size() > 1) {
                            Collections.sort(currencies, Collections.reverseOrder());
                            emitter.onNext((ArrayList<Currency>) currencies);
                        } else downloadCurrencyValues().subscribe(() -> {
                        }, throwable -> {
                            if (!emitter.isDisposed()) emitter.onError(throwable);
                        });
                    }));
        });
    }

    public Completable downloadCurrencyValues() {
        return Completable.fromAction(() -> {
            List<Currency> currencies = webService.downloadCurrencies().execute().body();
            if (currencies == null) throw new NullPointerException();
            database.getCurrencyDao().addCurrencies(currencies);
            for (Currency currency : currencies)
                database.getCurrencyDao().updateCurrencyValue(currency.getName(), currency.getValue());
        });
    }

    public void updateFavoriteState(Currency currency) {
        currency.setFavorite(!currency.isFavorite());
        updateCurrency(currency);
    }

    public void markAsRecent(Currency currency) {
        currency.setMaxIndex();
        updateCurrency(currency);
    }

    private void updateCurrency(Currency currency) {
        Completable.fromAction(() -> database.getCurrencyDao().updateCurrency(currency))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    public Flowable<List<Transaction>> getTransactions() {
        return database.getTransactionDao().getTransactions();
    }

    public void addTransaction(Transaction transaction) {
        Completable.fromAction(() -> database.getTransactionDao().addTransaction(transaction))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    public void saveFilter(Filter filter) {
        sharedPreferences.edit().putString(Filter.KEY, new Gson().toJson(filter)).apply();
    }

    public Filter loadFilter() {
        String json = sharedPreferences.getString(Filter.KEY, null);
        if (json == null) return new Filter(Filter.Period.ALL_TIME);
        else return new Gson().fromJson(json, Filter.class);
    }

    // Загружать курсы валют по всем дням слишком долго. Но одновременная загрузка курсов всех дней
    // черевата ошибками(объект HistoricalData иногда приходит null). Поэтому я попытался совместить оба
    // подхода, чтобы получить наилучший результат.
    public Single<ArrayList<HistoricalData>> getHistoricalData(String currency, Date startDate, Date endDate) {
        return Single.create(emitter -> {
            ArrayList<Date> dates = DateUtil.getDateList(startDate, endDate);
            ArrayList<Date> requestedDates = new ArrayList<>();
            ArrayList<HistoricalData> data = new ArrayList<>();
            CompositeDisposable disposables = new CompositeDisposable();
            for (Date date : dates) {
                disposables.add(database.getHistoricalDataDao().getHistoricalData(currency, date)
                        .subscribe(historicalData -> {
                            if (historicalData.size() != 0) {
                                if (!data.contains(historicalData.get(0)))
                                    data.add(historicalData.get(0));
                                if (data.size() == dates.size()) {
                                    Collections.sort(data);
                                    if (!emitter.isDisposed()) emitter.onSuccess(data);
                                }
                            } else if (!requestedDates.contains(date)) {
                                requestedDates.add(date);
                                downloadHistoricalData(currency, date)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(Schedulers.single())
                                        .subscribe(() -> {
                                        }, throwable -> {
                                            if (throwable instanceof NullPointerException) {
                                                downloadHistoricalData(currency, date).subscribe(() -> {
                                                }, exception -> {
                                                    if (!emitter.isDisposed()) {
                                                        if (data.size() > dates.size() * 4 / 5)
                                                            emitter.onSuccess(data);
                                                        else emitter.onError(exception);
                                                    }
                                                });
                                            } else if (!emitter.isDisposed()) emitter.onError(throwable);
                                        });
                            }
                        }));
            }
            emitter.setDisposable(disposables);
        });
    }

    private Completable downloadHistoricalData(String currency, Date date) {
        return Completable.fromAction(() -> {
            HistoricalData historicalData = webService.downloadHistoricalData(
                    DateUtil.getString(date, "yyyy-MM-dd"), currency).execute().body();
            if (historicalData != null) {
                historicalData.setDate(date);
                saveHistoricalData(historicalData);
            } else throw new NullPointerException();
        }).retry(6);
    }

    private void saveHistoricalData(HistoricalData historicalData) {
        Completable.fromAction(() -> database.getHistoricalDataDao().addHistoricalData(historicalData))
                .subscribeOn(Schedulers.io()).subscribe();
    }
}