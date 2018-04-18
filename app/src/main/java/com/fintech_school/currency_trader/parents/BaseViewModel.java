package com.fintech_school.currency_trader.parents;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.dependency_injection.MyApp;
import com.fintech_school.currency_trader.repo.CurrencyRepository;

import javax.inject.Inject;

public abstract class BaseViewModel extends AndroidViewModel {

    @Inject CurrencyRepository currencyRepository;

    public BaseViewModel(@NonNull Application application) {
        super(application);
        ((MyApp) application).getAppComponent().inject(this);
    }

    protected CurrencyRepository getCurrencyRepository() {
        return currencyRepository;
    }
}
