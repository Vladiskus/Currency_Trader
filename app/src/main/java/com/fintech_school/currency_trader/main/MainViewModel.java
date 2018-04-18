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
        BACK
    }

    private DestinationFragment currentFragment;
    private ObservableEmitter<DestinationFragment> navigationEmitter;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public Observable<DestinationFragment> getNavigationTriggerSource(boolean isFirstStart) {
        return Observable.create(emitter -> {
            navigationEmitter = emitter;
            if (isFirstStart) navigationEmitter.onNext(DestinationFragment.CURRENCY_LIST_FRAGMENT);
        });
    }

    public DestinationFragment getCurrentFragment() {
        return currentFragment;
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

    public void onNavigationUp() {
        navigationEmitter.onNext(DestinationFragment.BACK);
    }
}
