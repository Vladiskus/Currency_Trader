package com.fintech_school.currency_trader.main.no_internet_screen;

import android.app.Application;
import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.parents.BaseViewModel;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoInternetViewModel extends BaseViewModel {

    private ObservableEmitter<Boolean> loadingStateEmitter;
    private CompletableEmitter navigationEmitter;

    public NoInternetViewModel(@NonNull Application application) {
        super(application);
    }

    public Observable<Boolean> getLoadingStateSource() {
        return Observable.create(emitter -> loadingStateEmitter = emitter);
    }

    public Completable getNavigationTriggerSource() {
        return Completable.create(emitter -> navigationEmitter = emitter);
    }

    public void onButtonClick() {
        loadingStateEmitter.onNext(true);
        getCurrencyRepository().downloadCurrencyValues()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> navigationEmitter.onComplete(),
                        exception -> loadingStateEmitter.onNext(false));
    }
}
