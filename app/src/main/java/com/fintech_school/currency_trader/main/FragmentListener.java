package com.fintech_school.currency_trader.main;

public interface FragmentListener {

    void showProgress(boolean show);
    void showMessage(String text);
    FragmentNavigationController getNavigationController();
    void hideKeyboard();
}
