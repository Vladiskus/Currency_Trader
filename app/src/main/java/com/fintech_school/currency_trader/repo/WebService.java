package com.fintech_school.currency_trader.repo;

import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.data.HistoricalData;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebService {

    String SYMBOLS = "symbols";
    String DATE = "date";

    @GET("latest")
    Call<List<Currency>> downloadCurrencies();

    @GET("{date}")
    Call<HistoricalData> downloadHistoricalData(@Path(DATE) String date, @Query(SYMBOLS) String currency);

}
