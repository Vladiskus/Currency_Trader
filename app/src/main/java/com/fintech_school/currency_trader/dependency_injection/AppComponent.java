package com.fintech_school.currency_trader.dependency_injection;

import com.fintech_school.currency_trader.parents.BaseViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(BaseViewModel viewModel);
}
