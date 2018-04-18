package com.fintech_school.currency_trader.dependency_injection;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.data.HistoricalData;
import com.fintech_school.currency_trader.repo.CurrencyDatabase;
import com.fintech_school.currency_trader.repo.WebService;
import com.fintech_school.currency_trader.util.CurrencyListDeserializer;
import com.fintech_school.currency_trader.util.DateUtil;
import com.fintech_school.currency_trader.util.HistoricalDataDeserializer;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class AppModule {

    private Application application;
    private CurrencyDatabase database;

    public AppModule(Application application) {
        this.application = application;
        database = provideDatabase();
    }

    @Provides
    @Singleton
    WebService provideWebService() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new TypeToken<List<Currency>>() {
                }.getType(),
                new CurrencyListDeserializer());
        gsonBuilder.registerTypeAdapter(new TypeToken<HistoricalData>() {
                }.getType(),
                new HistoricalDataDeserializer());
        return new Retrofit.Builder().baseUrl("https://api.fixer.io/")
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create())).build()
                .create(WebService.class);
    }

    @Provides
    @Singleton
    CurrencyDatabase provideDatabase() {
        return Room.databaseBuilder(application, CurrencyDatabase.class, "currency rates.db")
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Completable.fromAction(() -> database.getCurrencyDao()
                                .addCurrency(new Currency("EUR", 1)))
                                .subscribeOn(Schedulers.io()).subscribe();
                    }
                })
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        Completable.fromAction(() -> database.getHistoricalDataDao()
                                .removeOldHistoricalData(new Date().getTime() - DateUtil.MONTH))
                                .subscribeOn(Schedulers.io()).subscribe();
                    }
                }).build();
    }

    @Provides
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }
}