package com.fintech_school.currency_trader.exchange.currency_list_screen;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.databinding.FragmentCurrencyListBinding;
import com.fintech_school.currency_trader.parents.BaseFragment;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CurrencyListFragment extends BaseFragment {

    private FragmentCurrencyListBinding binding;
    private CurrencyListViewModel viewModel;
    private CurrencyAdapter currencyAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(getActivity()).get(CurrencyListViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_currency_list, container, false);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        currencyAdapter = new CurrencyAdapter(viewModel);
        binding.recyclerView.setAdapter(currencyAdapter);
        setDefaultActionBar(R.string.title_selection);
        return binding.getRoot();
    }

    @Override
    protected void addListeners() {
        binding.selectedCurrency.mark.setOnClickListener((view) -> viewModel.onRemoveClick());
        binding.swipeToRefresh.setOnRefreshListener(() -> viewModel.getRefreshStateSource()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> binding.swipeToRefresh.setRefreshing(false))
                .subscribe(() -> {}, throwable -> {
                    getFragmentListener().showMessage(getString(R.string.error_connection_failure));
                    viewModel.updateIsDone();
                }));
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getCurrenciesSource()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currencies -> {
                    if (viewModel.isUpdateRequested()) {
                        if (currencyAdapter.getItemCount() != 0) viewModel.updateIsDone();
                        currencyAdapter.setCurrencies(currencies, () -> binding.recyclerView.scrollToPosition(0));
                    } else currencyAdapter.setCurrencies(currencies, () -> {});
                    if (viewModel.getSelectedCurrency() != null)
                        attachOrDetachCurrency(viewModel.getSelectedCurrency());
                }, exception -> getFragmentListener().getNavigationController().navigateToNoInternetFragment()));
        disposables.add(viewModel.getLoadingStateSource().subscribe(isLoading ->
                getFragmentListener().showProgress(isLoading)));
        disposables.add(viewModel.getSelectedCurrencySource().subscribe(this::attachOrDetachCurrency));
        disposables.add(viewModel.getNavigationTriggerSource().subscribe(pair -> {
            viewModel.removeSelectedCurrency();
            getFragmentListener().getNavigationController().navigateToExchangeFragment(pair.first, pair.second);
        }));
    }

    private void attachOrDetachCurrency(Pair<Currency, Integer> selectedCurrency) {
        binding.selectedCurrency.getRoot().setVisibility(selectedCurrency.first == null ? View.GONE : View.VISIBLE);
        binding.divider.setVisibility(selectedCurrency.first == null ? View.GONE : View.VISIBLE);
        if (selectedCurrency.first == null) {
            currencyAdapter.addCurrency(viewModel.getSelectedCurrency().first, viewModel.getSelectedCurrency().second);
            viewModel.removeSelectedCurrency();
        } else {
            binding.selectedCurrency.text.setText(selectedCurrency.first.getName());
            binding.selectedCurrency.mark.setImageDrawable(getResources()
                    .getDrawable(R.drawable.ic_close_black_24dp));
            currencyAdapter.removeCurrency(selectedCurrency.first);
        }
    }
}