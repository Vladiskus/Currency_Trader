package com.fintech_school.currency_trader.analytics;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.databinding.SimpleCurrencyItemBinding;

import java.util.List;

public class SimpleCurrencyAdapter extends RecyclerView.Adapter<SimpleCurrencyAdapter.ViewHolder> {

    private OnSimpleCurrencyClickListener listener;
    private List<String> currencies;
    private boolean isFirstAdapter;
    private String selectedCurrency;

    public SimpleCurrencyAdapter(OnSimpleCurrencyClickListener listener, List<String> currencies,
                                 boolean isFirstAdapter, String selectedCurrency) {
        this.listener = listener;
        this.currencies = currencies;
        this.isFirstAdapter = isFirstAdapter;
        this.selectedCurrency = selectedCurrency;
    }

    public void setSelectedCurrency(String selectedCurrency) {
        if (this.selectedCurrency.equals(selectedCurrency)) return;
        int oldPosition = currencies.indexOf(this.selectedCurrency);
        this.selectedCurrency = selectedCurrency;
        notifyItemChanged(oldPosition);
        notifyItemChanged(currencies.indexOf(selectedCurrency));
    }

    @Override
    public SimpleCurrencyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SimpleCurrencyItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.simple_currency_item, parent, false);
        return new SimpleCurrencyAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final SimpleCurrencyAdapter.ViewHolder holder, int position) {
        holder.bind(currencies.get(position), selectedCurrency, isFirstAdapter, listener);
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private SimpleCurrencyItemBinding binding;

        private ViewHolder(SimpleCurrencyItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(String currency, String selectedCurrency, boolean isFirstAdapter,
                          OnSimpleCurrencyClickListener listener) {
            binding.text.setText(currency);
            binding.text.setBackground(binding.text.getContext().getResources().getDrawable(
                    currency.equals(selectedCurrency) ? R.color.colorAccent : R.color.colorPrimaryLight));
            binding.text.setTextColor(binding.text.getContext().getResources().getColor(
                    currency.equals(selectedCurrency) ? android.R.color.white : android.R.color.black));
            binding.text.setOnClickListener(view -> {
                if (isFirstAdapter) listener.onFirstCurrencyClick(currency);
                else listener.onSecondCurrencyClick(currency);
            });
        }
    }
}