package com.fintech_school.currency_trader.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.databinding.ActivityMainBinding;
import com.fintech_school.currency_trader.parents.BaseActivity;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends BaseActivity implements FragmentListener {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private boolean isFirstStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if (savedInstanceState == null) isFirstStart = true;
    }

    @Override
    protected void addListeners() {
        binding.navigation.setOnNavigationItemSelectedListener((item) ->
                viewModel.onNavigationItemSelected(item.getItemId()));
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getNavigationTriggerSource(isFirstStart)
                .subscribe(destinationFragment -> {
                    if (destinationFragment == viewModel.getCurrentFragment()) return;
                    getSupportFragmentManager().popBackStack();
                    if (destinationFragment != MainViewModel.DestinationFragment.BACK)
                        viewModel.setCurrentFragment(destinationFragment);
                    switch (destinationFragment) {
                        case CURRENCY_LIST_FRAGMENT:
                            getNavigationController().navigateToCurrencyListFragment();
                            break;
                        case HISTORY_FRAGMENT:
                            getNavigationController().navigateToHistoryFragment();
                            break;
                        case ANALYTICS_FRAGMENT:
                            getNavigationController().navigateToAnalyticsFragment();
                            break;
                    }
                }));
        isFirstStart = false;
    }

    @Override
    public FragmentNavigationController getNavigationController() {
        showProgress(false);
        return new FragmentNavigationController(getSupportFragmentManager(), R.id.fragment);
    }

    @Override
    public void showMessage(String text) {
        Snackbar.make(binding.fragment, text, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showProgress(final boolean show) {
        hideKeyboard();
        binding.progress.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    @Override
    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        viewModel.onNavigationUp();
        return super.onSupportNavigateUp();
    }
}