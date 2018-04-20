package com.fintech_school.currency_trader.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.databinding.ActivityMainBinding;
import com.fintech_school.currency_trader.exchange.exchange_screen.ExchangeFragment;
import com.fintech_school.currency_trader.parents.BaseActivity;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends BaseActivity implements FragmentListener {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        setSupportActionBar(binding.toolbar);
    }

    @Override
    protected void addListeners() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            viewModel.onNavigationItemSelected(item.getItemId());
            binding.drawerNavigation.setCheckedItem(item.getItemId());
            return true;
        });
        binding.drawerNavigation.setNavigationItemSelectedListener(item -> {
            viewModel.onNavigationItemSelected(item.getItemId());
            binding.bottomNavigation.setSelectedItemId(item.getItemId());
            return true;
        });
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getNavigationTriggerSource()
                .subscribe(destinationFragment -> {
                    if (destinationFragment == viewModel.getCurrentFragment()) return;
                    if (destinationFragment != MainViewModel.DestinationFragment.DEVELOPER_OPTIONS_FRAGMENT)
                        getSupportFragmentManager().popBackStack();
                    if (destinationFragment != MainViewModel.DestinationFragment.DEVELOPER_OPTIONS_FRAGMENT
                            && destinationFragment != MainViewModel.DestinationFragment.BACK)
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
                        case DEVELOPER_OPTIONS_FRAGMENT:
                            getNavigationController().navigateToDeveloperOptionsFragment();
                            break;
                    }
                    new Handler().postDelayed(() ->
                            binding.drawerLayout.closeDrawer(GravityCompat.START), 250);
                }));
        disposables.add(viewModel.getNavigationDrawerState().subscribe(show -> {
            if (show) binding.drawerLayout.openDrawer(GravityCompat.START);
            else binding.drawerLayout.closeDrawer(GravityCompat.START);
        }));
        disposables.add(viewModel.getNavigationTypeSource().subscribe(isBottomNavigationSelected -> {
            binding.bottomNavigation.setVisibility(isBottomNavigationSelected ? View.VISIBLE : View.GONE);
            binding.drawerNavigation.setVisibility(isBottomNavigationSelected ? View.GONE : View.VISIBLE);
            ((MyApp) getApplication()).setBottomNavigationSelected(isBottomNavigationSelected);
        }));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
                if (currentFragment instanceof DeveloperOptionsFragment
                        || currentFragment instanceof ExchangeFragment)
                    viewModel.onNavigationBackClick();
                else viewModel.onHomeClick(binding.drawerLayout.isDrawerOpen(GravityCompat.START));
                return true;
            case R.id.developer_options:
                viewModel.onDeveloperOptionsClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}