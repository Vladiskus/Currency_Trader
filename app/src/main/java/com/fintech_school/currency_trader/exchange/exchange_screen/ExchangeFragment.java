package com.fintech_school.currency_trader.exchange.exchange_screen;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.databinding.FragmentExchangeBinding;
import com.fintech_school.currency_trader.parents.BaseFragment;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;

public class ExchangeFragment extends BaseFragment {

    private static final String BASE_CURRENCY = "base_currency";
    private static final String TARGET_CURRENCY = "target_currency";
    private static final String DIALOG_FRAGMENT = "exchange_dialog_fragment";

    private FragmentExchangeBinding binding;
    private ExchangeViewModel viewModel;

    public static ExchangeFragment newInstance(Currency baseCurrency, Currency targetCurrency) {
        ExchangeFragment fragment = new ExchangeFragment();
        Bundle args = new Bundle();
        args.putParcelable(BASE_CURRENCY, baseCurrency);
        args.putParcelable(TARGET_CURRENCY, targetCurrency);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_exchange, container, false);
        viewModel = ViewModelProviders.of(this).get(ExchangeViewModel.class);
        if (getArguments() != null) viewModel.onStart(getArguments().getParcelable(BASE_CURRENCY),
                getArguments().getParcelable(TARGET_CURRENCY));
        binding.setViewModel(viewModel);
        getActionBar().setTitle(R.string.title_exchange);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return binding.getRoot();
    }

    @Override
    protected void addListeners() {
        binding.twoArrows.setOnClickListener(view -> viewModel.onCurrencySwapClick());
        binding.button.setOnClickListener(view -> {
            if (getActivity().getCurrentFocus() == binding.editTextFrom)
                viewModel.onBaseAmountEntered(binding.editTextFrom.getText().toString());
            if (getActivity().getCurrentFocus() == binding.editTextTo)
                viewModel.onTargetAmountEntered(binding.editTextTo.getText().toString());
            viewModel.onExchangeButtonClick();
        });
        binding.editTextFrom.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onBaseAmountEntered(v.getText().toString());
                getFragmentListener().hideKeyboard();
                return true;
            } else return false;
        });
        binding.editTextTo.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onTargetAmountEntered(view.getText().toString());
                getFragmentListener().hideKeyboard();
                return true;
            } else return false;
        });
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getLoadingStateSource().subscribe((isLoading -> {
            getFragmentListener().showProgress(isLoading);
            binding.container.setBackground(getResources().getDrawable(isLoading ?
                    R.color.darkGray : android.R.color.white));
        })));
        disposables.add(viewModel.getButtonStateSource().subscribe((buttonState ->
                binding.button.setEnabled(buttonState))));
        disposables.add(viewModel.getNavigationTriggerSource()
                .throttleWithTimeout(400, TimeUnit.MILLISECONDS)
                .subscribe((ExchangeViewModel.ResponseState state) -> {
                    getFragmentListener().hideKeyboard();
                    switch (state) {
                        case SUCCESS:
                            getActivity().getSupportFragmentManager().popBackStack();
                            break;
                        case CONNECTION_FAILURE:
                            getFragmentListener().showMessage(getString(R.string.error_connection_failure));
                            break;
                        case NO_VALUE:
                            getFragmentListener().showMessage(getString(R.string.error_no_value));
                            break;
                        case CURRENCY_RATE_CHANGED:
                            ExchangeDialogFragment.newInstance(
                                    String.format(getString(R.string.dialog_text),
                                            String.valueOf(viewModel.getBaseAmount().get()),
                                            viewModel.getBaseCurrency().get().getName(),
                                            String.valueOf(viewModel.getTargetAmount().get()),
                                            viewModel.getTargetCurrency().get().getName()))
                                    .show(getChildFragmentManager(), DIALOG_FRAGMENT);
                            break;
                    }
                }));
    }

    public ExchangeViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }
}