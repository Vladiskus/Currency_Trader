package com.fintech_school.currency_trader.util;

import com.fintech_school.currency_trader.data.HistoricalData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

public class HistoricalDataDeserializer implements JsonDeserializer<HistoricalData> {


    public HistoricalData deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException {
        Date date = DateUtil.getDate(json.getAsJsonObject().get("date").getAsString(), "yyyy-MM-dd");
        if (date == null) return null;
        try {
            JsonElement rates = json.getAsJsonObject().get("rates");
            Map.Entry<String, JsonElement> rateEntry = rates.getAsJsonObject().entrySet().iterator().next();
            return new HistoricalData(rateEntry.getKey(), date, rateEntry.getValue().getAsDouble());
        } catch (NoSuchElementException e) {
            return new HistoricalData("EUR", date, 1);
        }
    }
}