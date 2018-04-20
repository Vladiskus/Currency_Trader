package com.fintech_school.currency_trader.main;

import android.app.Application;
import android.preference.PreferenceManager;

import com.fintech_school.currency_trader.dependency_injection.AppComponent;
import com.fintech_school.currency_trader.dependency_injection.AppModule;
import com.fintech_school.currency_trader.dependency_injection.DaggerAppComponent;

public class MyApp extends Application {

    private static final String NAVIGATION_FEATURE_KEY = "bottom_navigation";

    private AppComponent appComponent;
    private boolean isBottomNavigationSelected;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        isBottomNavigationSelected = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(NAVIGATION_FEATURE_KEY, true);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public boolean isBottomNavigationSelected() {
        return isBottomNavigationSelected;
    }

    public void setBottomNavigationSelected(boolean isBottomNavigationSelected) {
        this.isBottomNavigationSelected = isBottomNavigationSelected;
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(NAVIGATION_FEATURE_KEY, isBottomNavigationSelected).apply();
    }
}