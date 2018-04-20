package com.fintech_school.currency_trader.exchange.exchange_screen;

import android.app.Application;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.data.Transaction;
import com.fintech_school.currency_trader.parents.BaseViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ExchangeViewModel extends BaseViewModel {

    public enum ResponseState {
        SUCCESS,
        NO_VALUE,
        CONNECTION_FAILURE,
        CURRENCY_RATE_CHANGED
    }

    private ObservableField<Currency> baseCurrency;
    private ObservableField<Currency> targetCurrency;
    private ObservableField<Double> baseAmount;
    private ObservableField<Double> targetAmount;
    private ObservableField<Double> inputField;
    private ObservableEmitter<Boolean> loadingStateEmitter;
    private ObservableEmitter<Boolean> buttonStateEmitter;
    private ObservableEmitter<ResponseState> responseEmitter;
    private Disposable currencySubscription;
    private boolean isButtonClicked = false;
    private Date lastUpdate;

    public ExchangeViewModel(@NonNull Application application) {
        super(application);
        initObservableFields();
    }

    private void initObservableFields() {
        baseCurrency = new ObservableField<Currency>() {
            @Override
            public void set(Currency value) {
                super.set(value);
                if (baseAmount == inputField) baseAmount.set(baseAmount.get());
            }
        };
        targetCurrency = new ObservableField<Currency>() {
            @Override
            public void set(Currency value) {
                super.set(value);
                if (targetAmount == inputField) targetAmount.set(targetAmount.get());
            }
        };
        baseAmount = new ObservableField<Double>() {
            @Override
            public void set(Double value) {
                value = roundValue(value);
                if (baseAmount.get() != null && Math.abs(baseAmount.get() - value) < 0.4) return;
                super.set(value);
                targetAmount.set(value / baseCurrency.get().getValue() * targetCurrency.get().getValue());
            }
        };
        targetAmount = new ObservableField<Double>() {
            @Override
            public void set(Double value) {
                value = roundValue(value);
                if (targetAmount.get() != null && Math.abs(targetAmount.get() - value) < 0.4) return;
                super.set(value);
                baseAmount.set(value / targetCurrency.get().getValue() * baseCurrency.get().getValue());
            }
        };
    }

    private Double roundValue(Double value) {
        String format = "#.##";
        DecimalFormat decimalFormat = new DecimalFormat(format);
        String stringValue = decimalFormat.format(value);
        while (stringValue.charAt(stringValue.length() - 1) == '0' && stringValue.contains(".")) {
            format += "#";
            decimalFormat = new DecimalFormat(format);
        }
        return Double.valueOf(decimalFormat.format(value).replace(',', '.'));
    }

    public Observable<Boolean> getLoadingStateSource() {
        return Observable.create(emitter -> {
            loadingStateEmitter = emitter;
            if (buttonStateEmitter != null) refreshCurrencies();
        });
    }

    public Observable<Boolean> getButtonStateSource() {
        return Observable.create(emitter -> {
            buttonStateEmitter = emitter;
            if (loadingStateEmitter != null) refreshCurrencies();
        });
    }

    public Observable<ResponseState> getNavigationTriggerSource() {
        return Observable.create(emitter -> responseEmitter = emitter);
    }

    public ObservableField<Currency> getBaseCurrency() {
        return baseCurrency;
    }

    public ObservableField<Currency> getTargetCurrency() {
        return targetCurrency;
    }

    public ObservableField<Double> getBaseAmount() {
        return baseAmount;
    }

    public ObservableField<Double> getTargetAmount() {
        return targetAmount;
    }

    public void onStart(Currency firstCurrency, Currency secondCurrency) {
        if (baseCurrency.get() == null || targetCurrency.get() == null) {
            this.baseCurrency.set(firstCurrency);
            this.targetCurrency.set(secondCurrency);
            inputField = baseAmount;
            baseAmount.set(1.00);
            subscribe();
        }
    }

    private void subscribe() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(baseCurrency.get().getName(),
                targetCurrency.get().getName()));
        currencySubscription = getCurrencyRepository().getCurrencies(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currencies -> {
                    boolean isCurrencyRateChanged = baseCurrency.get().getValue() !=
                            currencies.get(currencies.indexOf(baseCurrency.get())).getValue() ||
                            targetCurrency.get().getValue() !=
                                    currencies.get(currencies.indexOf(targetCurrency.get())).getValue();
                    if (isCurrencyRateChanged) {
                        baseCurrency.set(currencies.get(currencies.indexOf(baseCurrency.get())));
                        targetCurrency.set(currencies.get(currencies.indexOf(targetCurrency.get())));
                    }
                    if (isButtonClicked) {
                        if (isCurrencyRateChanged)
                            responseEmitter.onNext(ResponseState.CURRENCY_RATE_CHANGED);
                        else onSuccessTransaction();
                        isButtonClicked = false;
                    }
                });
    }

    public void onSuccessTransaction() {
        if (baseAmount.get() != 0 || targetAmount.get() != 0) {
            getCurrencyRepository().markAsRecent(baseCurrency.get());
            getCurrencyRepository().markAsRecent(targetCurrency.get());
            Transaction transaction = new Transaction(baseCurrency.get().getName(),
                    targetCurrency.get().getName(), baseAmount.get(), targetAmount.get(), new Date());
            getCurrencyRepository().addTransaction(transaction);
            responseEmitter.onNext(ResponseState.SUCCESS);
        } else responseEmitter.onNext(ResponseState.NO_VALUE);
    }

    public void onCurrencySwapClick() {
        Currency tempCurrency = baseCurrency.get();
        baseCurrency.set(targetCurrency.get());
        targetCurrency.set(tempCurrency);
        if (targetAmount.get() != null) baseAmount.set(targetAmount.get());
        else baseAmount.set(1.00);
    }

    public void onBaseAmountEntered(String amount) {
        inputField = baseAmount;
        onInputEntered(amount);
    }

    public void onTargetAmountEntered(String amount) {
        inputField = targetAmount;
        onInputEntered(amount);
    }

    private void onInputEntered(String amount) {
        if (amount.equals("")) inputField.set(0.00);
        inputField.set(Double.valueOf(amount));
        refreshCurrencies();
    }

    public void onExchangeButtonClick() {
        isButtonClicked = true;
        refreshCurrencies();
    }

    private void refreshCurrencies() {
        if (lastUpdate != null && lastUpdate.getTime() - new Date().getTime() < 5 * 60 * 1000) {
            if (isButtonClicked) {
                isButtonClicked = false;
                onSuccessTransaction();
            }
            return;
        }
        if (isButtonClicked) loadingStateEmitter.onNext(true);
        buttonStateEmitter.onNext(false);
        getCurrencyRepository().downloadCurrencyValues()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    buttonStateEmitter.onNext(true);
                    loadingStateEmitter.onNext(false);
                }).subscribe(() -> lastUpdate = new Date(),
                error -> responseEmitter.onNext(ResponseState.CONNECTION_FAILURE));
    }

    public void onStop() {
        currencySubscription.dispose();
    }
}