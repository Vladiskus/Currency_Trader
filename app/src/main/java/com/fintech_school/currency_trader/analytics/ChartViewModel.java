package com.fintech_school.currency_trader.analytics;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.data.HistoricalData;
import com.fintech_school.currency_trader.parents.BaseViewModel;
import com.fintech_school.currency_trader.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ChartViewModel extends BaseViewModel implements OnSimpleCurrencyClickListener {

    private static final String BASE_CURRENCY = "EUR";

    private Long period;
    private String firstCurrency;
    private String secondCurrency;
    private ArrayList<String> currencies;
    private ArrayList<HistoricalData> tempCurrencyData;
    private HashMap<String, ArrayList<HistoricalData>> savedData = new HashMap<>();
    private ObservableEmitter<Boolean> loadingStateEmitter;
    private ObservableEmitter<Boolean> chartLoadingStateEmitter;
    private ObservableEmitter<Pair<String, String>> selectedCurrenciesEmitter;
    private ObservableEmitter<LinkedHashMap<String, Double>> chartDataEmitter;
    private CompositeDisposable disposables = new CompositeDisposable();

    public ChartViewModel(@NonNull Application application) {
        super(application);
    }

    public Observable<ArrayList<String>> getUsedCurrenciesSource() {
        if (currencies != null) return Observable.just(currencies);
        return getCurrencyRepository().getCurrencies(null)
                .observeOn(AndroidSchedulers.mainThread()).map((currencies -> {
                    this.currencies = new ArrayList<>();
                    for (Currency currency : currencies) this.currencies.add(currency.getName());
                    if (firstCurrency == null) {
                        firstCurrency = this.currencies.get(0);
                        if (firstCurrency.equals(BASE_CURRENCY))
                            firstCurrency = this.currencies.get(1);
                    }
                    if (secondCurrency == null) secondCurrency = BASE_CURRENCY;
                    requestChartData();
                    loadingStateEmitter.onNext(false);
                    return this.currencies;
                }));
    }

    public Observable<LinkedHashMap<String, Double>> getChartDataSource() {
        return Observable.create(emitter -> chartDataEmitter = emitter);
    }

    public Observable<Boolean> getLoadingStateSource() {
        return Observable.create(emitter -> {
            loadingStateEmitter = emitter;
            if (currencies == null) loadingStateEmitter.onNext(true);
        });
    }

    public Observable<Boolean> getChartLoadingStateSource() {
        return Observable.create(emitter -> chartLoadingStateEmitter = emitter);
    }

    public Observable<Pair<String, String>> getSelectedCurrenciesSource() {
        return Observable.create(emitter -> selectedCurrenciesEmitter = emitter);
    }

    public Long getPeriod() {
        return period;
    }

    public String getFirstCurrency() {
        return firstCurrency;
    }

    public String getSecondCurrency() {
        return secondCurrency;
    }

    public int getFirstCurrencyPosition() {
        return currencies.indexOf(firstCurrency);
    }

    public int getSecondCurrencyPosition() {
        return getSecondList().indexOf(secondCurrency);
    }

    public ArrayList<String> getSecondList() {
        ArrayList<String> secondList = new ArrayList<>(currencies);
        secondList.remove(BASE_CURRENCY);
        secondList.add(0, BASE_CURRENCY);
        return secondList;
    }

    @Override
    public void onFirstCurrencyClick(String currency) {
        if (firstCurrency.equals(currency)) return;
        if (secondCurrency.equals(currency)) secondCurrency = String.valueOf(firstCurrency);
        firstCurrency = String.valueOf(currency);
        selectedCurrenciesEmitter.onNext(new Pair<>(firstCurrency, secondCurrency));
        requestChartData();
    }

    @Override
    public void onSecondCurrencyClick(String currency) {
        if (secondCurrency.equals(currency)) return;
        if (firstCurrency.equals(currency)) firstCurrency = String.valueOf(secondCurrency);
        secondCurrency = String.valueOf(currency);
        selectedCurrenciesEmitter.onNext(new Pair<>(firstCurrency, secondCurrency));
        requestChartData();
    }

    public void onPeriodChanged(int position) {
        if (position == 0) period = DateUtil.WEEK;
        else if (position == 1) period = DateUtil.TWO_WEEKS;
        else period = DateUtil.MONTH;
        requestChartData();
    }

    private void requestChartData() {
        if (firstCurrency == null || secondCurrency == null || period == null) return;
        tempCurrencyData = null;
        disposables.clear();
        requestHistoricalData(firstCurrency);
        requestHistoricalData(secondCurrency);
    }

    private void requestHistoricalData(String currency) {
        ArrayList<HistoricalData> savedHistoricalData = savedData.get(currency + period);
        if (savedHistoricalData != null) onDataReceived(savedHistoricalData, currency);
        else disposables.add(getCurrencyRepository().getHistoricalData(currency,
                new Date(new Date().getTime() - period), new Date())
                .timeout(5000, TimeUnit.MILLISECONDS)
                .retry(4)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> chartLoadingStateEmitter.onNext(true))
                .subscribe(data -> {
                    savedData.put(currency + period, data);
                    onDataReceived(data, currency);
                }, throwable -> {
                    chartDataEmitter.onNext(new LinkedHashMap<>());
                    chartLoadingStateEmitter.onNext(false);
                }));
    }

    private void onDataReceived(ArrayList<HistoricalData> data, String currency) {
        if (tempCurrencyData != null) {
            LinkedHashMap<String, Double> chartData = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(data.size(), tempCurrencyData.size()); i++) {
                chartData.put(DateUtil.getString(data.get(i).getDate(), "dd.MM.yyyy"),
                        currency.equals(firstCurrency) ?
                                tempCurrencyData.get(i).getValue() / data.get(i).getValue() :
                                data.get(i).getValue() / tempCurrencyData.get(i).getValue());
            }
            chartLoadingStateEmitter.onNext(false);
            chartDataEmitter.onNext(chartData);
        } else tempCurrencyData = data;
    }
}