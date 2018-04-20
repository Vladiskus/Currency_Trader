package com.fintech_school.currency_trader.history.transaction_filter_screen;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.fintech_school.currency_trader.main.MyApp;
import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.data.Filter;
import com.fintech_school.currency_trader.databinding.FragmentFilterBinding;
import com.fintech_school.currency_trader.parents.BaseFragment;

import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FilterFragment extends BaseFragment {

    private FragmentFilterBinding binding;
    private FilterViewModel viewModel;
    private UsedCurrencyAdapter usedCurrencyAdapter;
    private ArrayAdapter<String> spinnerAdapter4;
    private ArrayAdapter<String> spinnerAdapter5;
    private String subtitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false);
        viewModel = ViewModelProviders.of(this).get(FilterViewModel.class);
        initSpinnerAdapters();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBar().setTitle(R.string.title_filter);
        if (subtitle != null) getActionBar().setSubtitle(subtitle);
        getActionBar().setDisplayHomeAsUpEnabled(!((MyApp) getActivity().getApplication())
                .isBottomNavigationSelected());
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
    }

    private void initSpinnerAdapters() {
        spinnerAdapter4 = new ArrayAdapter<>(getContext(), R.layout.spinner_layout,
                getResources().getStringArray(R.array.filter_array_4));
        spinnerAdapter5 = new ArrayAdapter<>(getContext(), R.layout.spinner_layout,
                getResources().getStringArray(R.array.filter_array_5));
        binding.spinner.setAdapter(spinnerAdapter4);
    }

    private void changeSpinnerAdapter(ArrayAdapter<String> adapter) {
        if (binding.spinner.getAdapter() != adapter) {
            binding.spinner.setAdapter(adapter);
            setSpinnerPosition();
        }
    }

    @Override
    protected void addListeners() {
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) {
                    if (!viewModel.getFilter()
                            .toString(getResources().getStringArray(R.array.filter_array_4)).contains("-"))
                        showDatePicker();
                    else changeSpinnerAdapter(spinnerAdapter5);

                } else if (position == 4) showDatePicker();
                else {
                    viewModel.onSimpleFilterSelected(position);
                    changeSpinnerAdapter(spinnerAdapter4);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.title.checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.onMainCheckedChange(isChecked));
    }

    private void showDatePicker() {
        Calendar startCalendarDate = Calendar.getInstance();
        Calendar endCalendarDate = Calendar.getInstance();
        endCalendarDate.setTime(viewModel.getFilter().getEndDate());
        if (viewModel.getFilter().getPeriod() != Filter.Period.ALL_TIME)
            startCalendarDate.setTime(viewModel.getFilter().getStartDate());
        DatePickerDialog dataPicker = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth, yearEnd, monthOfYearEnd, dayOfMonthEnd) -> {
                    Calendar startDate = Calendar.getInstance();
                    Calendar endDate = Calendar.getInstance();
                    startDate.set(year, monthOfYear, dayOfMonth);
                    endDate.set(yearEnd, monthOfYearEnd, dayOfMonthEnd);
                    viewModel.onComplexFilterSelected(startDate.getTime(), endDate.getTime());
                    changeSpinnerAdapter(spinnerAdapter5);
                },
                startCalendarDate.get(Calendar.YEAR),
                startCalendarDate.get(Calendar.MONTH),
                startCalendarDate.get(Calendar.DAY_OF_MONTH),
                endCalendarDate.get(Calendar.YEAR),
                endCalendarDate.get(Calendar.MONTH),
                endCalendarDate.get(Calendar.DAY_OF_MONTH)
        );
        dataPicker.setOnDismissListener(dialog -> setSpinnerPosition());
        dataPicker.show(getActivity().getFragmentManager(), "Date picker dialog");
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getLoadingStateSource().subscribe(isLoading ->
                getFragmentListener().showProgress(isLoading)));
        disposables.add(viewModel.getNavigationTriggerSource().subscribe(() ->
                getActivity().getSupportFragmentManager().popBackStack()));
        disposables.add(viewModel.getSelectionDroppedSource().subscribe(isAllChecked -> {
            binding.title.checkBox.setChecked(isAllChecked);
            binding.title.checkBox.jumpDrawablesToCurrentState();
            if (usedCurrencyAdapter != null) usedCurrencyAdapter.notifyDataSetChanged();
        }));
        disposables.add(viewModel.getPeriodSelectedSource().subscribe(period -> {
            if (getActionBar() != null) getActionBar().setSubtitle(period);
            subtitle = period;
        }));
        disposables.add(viewModel.getUsedCurrenciesSource()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currencies -> {
                    setSpinnerPosition();
                    switchVisibility(currencies.size() != 0);
                    initAdapter(currencies);
                }));
    }

    private void setSpinnerPosition() {
        if (viewModel.getFilter().getPeriod() == null) binding.spinner.setSelection(3);
        else switch (viewModel.getFilter().getPeriod()) {
            case ALL_TIME:
                binding.spinner.setSelection(0);
                break;
            case LAST_WEEK:
                binding.spinner.setSelection(1);
                break;
            case LAST_MONTH:
                binding.spinner.setSelection(2);
                break;
        }
    }

    private void switchVisibility(boolean hasContent) {
        if (hasContent) binding.getRoot().setVisibility(View.VISIBLE);
        else binding.noUsedCurrencies.setVisibility(View.VISIBLE);
    }

    private void initAdapter(List<String> currencies) {
        usedCurrencyAdapter = new UsedCurrencyAdapter(viewModel, currencies, viewModel.getFilter());
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        binding.recyclerView.setAdapter(usedCurrencyAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.done_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            viewModel.onDoneClick();
            return true;
        } else return super.onOptionsItemSelected(item);
    }
}