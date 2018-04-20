package com.fintech_school.currency_trader.main;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.databinding.FragmentDeveloperOptionsBinding;
import com.fintech_school.currency_trader.parents.BaseFragment;

public class DeveloperOptionsFragment extends BaseFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDeveloperOptionsBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_developer_options, container, false);
        MainViewModel viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        binding.bottomNavigationButton.setChecked(viewModel.isBottomNavigationSelected());
        binding.drawerNavigationButton.setChecked(!viewModel.isBottomNavigationSelected());
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) ->
                viewModel.onNavigationTypeChanged(
                        checkedId == binding.bottomNavigationButton.getId()));
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBar().setTitle(R.string.title_developer_options);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.developer_options).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}
