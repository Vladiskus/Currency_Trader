package com.fintech_school.currency_trader.parents;

import android.support.v7.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends AppCompatActivity {

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onStart() {
        super.onStart();
        addListeners();
        subscribe(disposables);
    }

    protected void addListeners() {}
    protected void subscribe(CompositeDisposable disposables) {}

    @Override
    protected void onStop() {
        super.onStop();
        disposables.clear();
    }
}
