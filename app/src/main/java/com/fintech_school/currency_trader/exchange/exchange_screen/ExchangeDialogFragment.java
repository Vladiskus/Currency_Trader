package com.fintech_school.currency_trader.exchange.exchange_screen;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.fintech_school.currency_trader.R;

public class ExchangeDialogFragment extends DialogFragment {

    public static final String TEXT = "text";

    public static ExchangeDialogFragment newInstance(String text) {
        ExchangeDialogFragment fragment = new ExchangeDialogFragment();
        Bundle args = new Bundle();
        args.putString(TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString(TEXT))
                .setPositiveButton(R.string.dialog_yes, (dialog, id) ->
                        ((ExchangeFragment) getParentFragment()).getViewModel().onSuccessTransaction())
                .setNegativeButton(R.string.dialog_no, (dialog, id) -> {});
        return builder.create();
    }
}