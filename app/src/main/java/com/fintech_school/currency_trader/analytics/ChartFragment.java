package com.fintech_school.currency_trader.analytics;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.databinding.FragmentChartBinding;
import com.fintech_school.currency_trader.parents.BaseFragment;
import com.fintech_school.currency_trader.util.DateUtil;

import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ChartFragment extends BaseFragment {

    private FragmentChartBinding binding;
    private ChartViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false);
        viewModel = ViewModelProviders.of(getActivity()).get(ChartViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setDefaultActionBar(R.string.title_analytics);
    }

    @Override
    protected void addListeners() {
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.onPeriodChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getLoadingStateSource().subscribe(isLoading ->
                getFragmentListener().showProgress(isLoading)));
        disposables.add(viewModel.getUsedCurrenciesSource()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currencies -> {
                    binding.root.setVisibility(View.VISIBLE);
                    initAdapters(currencies);
                    setSpinnerPosition();
                }, throwable -> getFragmentListener().getNavigationController().navigateToNoInternetFragment()));
        disposables.add(viewModel.getSelectedCurrenciesSource().subscribe(pair -> {
            ((SimpleCurrencyAdapter) binding.firstRecyclerView.getAdapter()).setSelectedCurrency(pair.first);
            ((SimpleCurrencyAdapter) binding.secondRecyclerView.getAdapter()).setSelectedCurrency(pair.second);
        }));
        disposables.add(viewModel.getChartLoadingStateSource().subscribe(isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.chart.setVisibility(View.GONE);
                binding.error.setVisibility(View.GONE);
            }
        }));
        disposables.add(viewModel.getChartDataSource().subscribe(data -> {
            binding.error.setVisibility(data.size() == 0 ? View.VISIBLE : View.GONE);
            binding.chart.setVisibility(data.size() == 0 ? View.GONE : View.VISIBLE);
            if (data.size() != 0) showChart(data);
        }));
    }

    private void initAdapters(ArrayList<String> currencies) {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.divider_line));
        binding.firstRecyclerView.addItemDecoration(dividerItemDecoration);
        binding.secondRecyclerView.addItemDecoration(dividerItemDecoration);
        binding.firstRecyclerView.setAdapter(new SimpleCurrencyAdapter(viewModel, currencies,
                true, viewModel.getFirstCurrency()));
        binding.secondRecyclerView.setAdapter(new SimpleCurrencyAdapter(viewModel,
                viewModel.getSecondList(), false, viewModel.getSecondCurrency()));
        binding.firstRecyclerView.scrollToPosition(viewModel.getFirstCurrencyPosition());
        binding.secondRecyclerView.scrollToPosition(viewModel.getSecondCurrencyPosition());
    }

    private void setSpinnerPosition() {
        if (DateUtil.MONTH.equals(viewModel.getPeriod())) binding.spinner.setSelection(2);
        else if (DateUtil.TWO_WEEKS.equals(viewModel.getPeriod())) binding.spinner.setSelection(1);
        else binding.spinner.setSelection(0);
    }

    private void showChart(LinkedHashMap<String, Double> data) {
        binding.chart.clearChart();
        ValueLineSeries series = new ValueLineSeries();
        series.setColor(getResources().getColor(R.color.chartColor));
        for (Map.Entry<String, Double> entry : data.entrySet())
            series.addPoint(new ValueLinePoint(entry.getKey(), entry.getValue().floatValue()));
        binding.chart.addSeries(series);
        binding.chart.startAnimation();
        binding.chart.setVisibility(View.VISIBLE);
    }
}