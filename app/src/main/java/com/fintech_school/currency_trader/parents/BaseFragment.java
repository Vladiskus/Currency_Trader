package com.fintech_school.currency_trader.parents;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fintech_school.currency_trader.main.MyApp;
import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.main.FragmentListener;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment extends Fragment {

    private CompositeDisposable disposables = new CompositeDisposable();
    private FragmentListener fragmentListener;
    private ActionBar actionBar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) fragmentListener = (FragmentListener) context;
        else throw new RuntimeException(context.toString() + " must implement FragmentListener");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar == null) throw new RuntimeException("Screen must have ActionBar");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addListeners();
        subscribe(disposables);
    }

    protected void addListeners() {}
    protected void subscribe(CompositeDisposable disposables) {}

    protected FragmentListener getFragmentListener() {
        return fragmentListener;
    }

    protected ActionBar getActionBar() {
        return actionBar;
    }

    protected void setDefaultActionBar(int titleId) {
        actionBar.setTitle(titleId);
        if (titleId != R.string.title_history) actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(!((MyApp) getActivity().getApplication())
                .isBottomNavigationSelected());
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }
}