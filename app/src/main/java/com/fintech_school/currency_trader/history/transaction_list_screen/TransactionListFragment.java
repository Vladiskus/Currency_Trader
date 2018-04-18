package com.fintech_school.currency_trader.history.transaction_list_screen;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.data.Filter;
import com.fintech_school.currency_trader.databinding.FragmentTransactionListBinding;
import com.fintech_school.currency_trader.parents.BaseFragment;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TransactionListFragment extends BaseFragment {

    private FragmentTransactionListBinding binding;
    private TransactionListViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private boolean hasContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction_list, container, false);
        viewModel = ViewModelProviders.of(getActivity()).get(TransactionListViewModel.class);
        setDefaultActionBar(R.string.title_history);
        initAdapter();
        return binding.getRoot();
    }

    private void initAdapter() {
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        transactionAdapter = new TransactionAdapter();
        binding.recyclerView.setAdapter(transactionAdapter);
        // Небольшой костыль, исправляющий баг RecyclerView, приводящий к редким крашам.
        // https://issuetracker.google.com/issues/37030377
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getLoadingStateSource().subscribe(isLoading ->
                getFragmentListener().showProgress(isLoading)));
        disposables.add(viewModel.getFilterNameSource().subscribe(filterName ->
                getActionBar().setSubtitle(filterName)));
        disposables.add(viewModel.getNavigationTriggerSource().subscribe(object ->
                getFragmentListener().getNavigationController().navigateToFilterFragment()));
        disposables.add(viewModel.getTransactionsSource()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transactions -> {
                    hasContent = transactions.size() != 0;
                    switchVisibility();
                    transactionAdapter.setTransactions(transactions, () ->
                            binding.recyclerView.scrollToPosition(0));
                }));
    }

    private void switchVisibility() {
        binding.noTransactions.setVisibility(hasContent ? View.GONE : View.VISIBLE);
        binding.recyclerView.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (hasContent || !(viewModel.getFilter().getSelectedCurrencies() == null
                && viewModel.getFilter().getPeriod() == Filter.Period.ALL_TIME))
            inflater.inflate(R.menu.filter_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filter) viewModel.onFilterClick();
        return super.onOptionsItemSelected(item);
    }
}