package com.fintech_school.currency_trader.main;

import android.support.v4.app.FragmentManager;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.analytics.ChartFragment;
import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.exchange.currency_list_screen.CurrencyListFragment;
import com.fintech_school.currency_trader.exchange.exchange_screen.ExchangeFragment;
import com.fintech_school.currency_trader.main.no_internet_screen.NoInternetFragment;
import com.fintech_school.currency_trader.history.transaction_filter_screen.FilterFragment;
import com.fintech_school.currency_trader.history.transaction_list_screen.TransactionListFragment;

public class FragmentNavigationController {

    private final int containerId;
    private final FragmentManager fragmentManager;

    public FragmentNavigationController(FragmentManager fragmentManager, int containerId) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }

    public void navigateToCurrencyListFragment() {
        fragmentManager.beginTransaction().replace(containerId, new CurrencyListFragment()).commit();
    }

    public void navigateToExchangeFragment(Currency currency1, Currency currency2) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(containerId, ExchangeFragment.newInstance(currency1, currency2))
                .addToBackStack(null).commit();
    }

    public void navigateToNoInternetFragment() {
        fragmentManager.beginTransaction().replace(containerId, new NoInternetFragment())
                .addToBackStack(null).commit();
    }

    public void navigateToHistoryFragment() {
        fragmentManager.beginTransaction().replace(containerId, new TransactionListFragment()).commit();
    }

    public void navigateToFilterFragment() {
        fragmentManager.beginTransaction().replace(containerId, new FilterFragment())
                .addToBackStack(null).commit();
    }

    public void navigateToAnalyticsFragment() {
        fragmentManager.beginTransaction().replace(containerId, new ChartFragment()).commit();
    }
}

