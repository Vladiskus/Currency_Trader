package com.fintech_school.currency_trader.util;

import com.fintech_school.currency_trader.data.Currency;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CurrencyListDeserializer implements JsonDeserializer<List<Currency>> {

    public List<Currency> deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException {
        ArrayList<Currency> currencies = new ArrayList<>();
        JsonElement rates = json.getAsJsonObject().get("rates");
        Set<Map.Entry<String, JsonElement>> rateEntries = rates.getAsJsonObject().entrySet();
        for (Map.Entry<String, JsonElement> rateEntry : rateEntries)
            currencies.add(new Currency(rateEntry.getKey(), rateEntry.getValue().getAsDouble()));
        return currencies;
    }
}

