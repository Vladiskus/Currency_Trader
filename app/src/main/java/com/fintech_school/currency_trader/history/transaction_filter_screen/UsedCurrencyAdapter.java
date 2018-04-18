package com.fintech_school.currency_trader.history.transaction_filter_screen;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.databinding.UsedCurrencyItemBinding;
import com.fintech_school.currency_trader.data.Filter;

import java.util.List;

public class UsedCurrencyAdapter extends RecyclerView.Adapter<UsedCurrencyAdapter.ViewHolder> {

    private OnUsedCurrencyClickListener listener;
    private List<String> currencies;
    private Filter filter;

    public UsedCurrencyAdapter(OnUsedCurrencyClickListener listener, List<String> currencies, Filter filter) {
        this.listener = listener;
        this.currencies = currencies;
        this.filter = filter;
    }

    @Override
    public UsedCurrencyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UsedCurrencyItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.used_currency_item, parent, false);
        return new UsedCurrencyAdapter.ViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(final UsedCurrencyAdapter.ViewHolder holder, int position) {
        holder.bind(currencies.get(position), filter.getSelectedCurrencies().contains(currencies.get(position)));
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private OnUsedCurrencyClickListener listener;
        private UsedCurrencyItemBinding binding;

        private ViewHolder(UsedCurrencyItemBinding binding, OnUsedCurrencyClickListener listener) {
            super(binding.getRoot());
            this.listener = listener;
            this.binding = binding;
        }

        private void bind(String currency, boolean isSelected) {
            binding.text.setText(currency);
            binding.checkBox.setChecked(isSelected);
            binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                            listener.onCheckedChange(currency, isChecked));
        }
    }
}
