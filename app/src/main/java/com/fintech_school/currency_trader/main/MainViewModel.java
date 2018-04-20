package com.fintech_school.currency_trader.main;

import android.app.Application;
import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.parents.BaseViewModel;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class MainViewModel extends BaseViewModel {

    public enum DestinationFragment {
        CURRENCY_LIST_FRAGMENT,
        HISTORY_FRAGMENT,
        ANALYTICS_FRAGMENT,
        DEVELOPER_OPTIONS_FRAGMENT,
        BACK
    }

    private DestinationFragment currentFragment;
    private ObservableEmitter<DestinationFragment> navigationEmitter;
    private ObservableEmitter<Boolean> navigationDrawerStateEmitter;
    private ObservableEmitter<Boolean> navigationTypeEmitter;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public Observable<DestinationFragment> getNavigationTriggerSource() {
        return Observable.create(emitter -> {
            navigationEmitter = emitter;
            if (currentFragment == null) navigationEmitter.onNext(DestinationFragment.CURRENCY_LIST_FRAGMENT);
        });
    }

    public Observable<Boolean> getNavigationDrawerState() {
        return Observable.create(emitter -> navigationDrawerStateEmitter = emitter);
    }

    public Observable<Boolean> getNavigationTypeSource() {
        return Observable.create(emitter -> {
            navigationTypeEmitter = emitter;
            navigationTypeEmitter.onNext(isBottomNavigationSelected());
        });
    }

    public DestinationFragment getCurrentFragment() {
        return currentFragment;
    }

    public boolean isBottomNavigationSelected() {
        return ((MyApp) getApplication()).isBottomNavigationSelected();
    }

    public void setCurrentFragment(DestinationFragment currentFragment) {
        this.currentFragment = currentFragment;
    }

    public boolean onNavigationItemSelected(int id) {
        switch (id) {
            case R.id.navigation_exchange:
                navigationEmitter.onNext(DestinationFragment.CURRENCY_LIST_FRAGMENT);
                return true;
            case R.id.navigation_history:
                navigationEmitter.onNext(DestinationFragment.HISTORY_FRAGMENT);
                return true;
            case R.id.navigation_analytics:
                navigationEmitter.onNext(DestinationFragment.ANALYTICS_FRAGMENT);
                return true;
        }
        return false;
    }

    public void onHomeClick(boolean isDrawerOpen) {
        navigationDrawerStateEmitter.onNext(!isDrawerOpen);
    }

    public void onDeveloperOptionsClick() {
        navigationEmitter.onNext(DestinationFragment.DEVELOPER_OPTIONS_FRAGMENT);
    }

    public void onNavigationBackClick() {
        navigationEmitter.onNext(DestinationFragment.BACK);
    }

    public void onNavigationTypeChanged(boolean isBottomNavigationSelected) {
        ((MyApp) getApplication()).setBottomNavigationSelected(isBottomNavigationSelected);
        navigationTypeEmitter.onNext(isBottomNavigationSelected);
    }
}
