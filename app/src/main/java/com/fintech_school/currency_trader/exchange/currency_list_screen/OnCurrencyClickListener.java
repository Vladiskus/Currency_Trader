package com.fintech_school.currency_trader.exchange.currency_list_screen;

import com.fintech_school.currency_trader.data.Currency;

public interface OnCurrencyClickListener {

    void onCurrencyClick(Currency currency);
    boolean onLongCurrencyClick(Currency currency, int position);
    void onFavoriteMarkClick(Currency currency);
}
