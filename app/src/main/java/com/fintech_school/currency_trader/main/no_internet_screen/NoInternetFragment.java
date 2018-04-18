package com.fintech_school.currency_trader.main.no_internet_screen;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.parents.BaseFragment;

import io.reactivex.disposables.CompositeDisposable;

public class NoInternetFragment extends BaseFragment {

    private NoInternetViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(NoInternetViewModel.class);
        View view = inflater.inflate(R.layout.fragment_no_internet, container, false);
        view.findViewById(R.id.button).setOnClickListener(v -> viewModel.onButtonClick());
        return view;
    }

    @Override
    protected void subscribe(CompositeDisposable disposables) {
        disposables.add(viewModel.getLoadingStateSource().subscribe((isLoading) -> {
            getFragmentListener().showProgress(isLoading);
            if (getView() != null) getView().setBackground(getResources().getDrawable(isLoading ?
                    R.color.darkGray : android.R.color.white));
        }));
        disposables.add(viewModel.getNavigationTriggerSource().subscribe(() ->
                getActivity().getSupportFragmentManager().popBackStack()));
    }
}