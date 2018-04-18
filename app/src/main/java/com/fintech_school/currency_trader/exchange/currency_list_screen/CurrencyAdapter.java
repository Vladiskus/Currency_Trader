package com.fintech_school.currency_trader.exchange.currency_list_screen;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.data.Currency;
import com.fintech_school.currency_trader.databinding.CurrencyItemBinding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {

    private List<Currency> currencies = new ArrayList<>();
    private OnCurrencyClickListener listener;

    public CurrencyAdapter(OnCurrencyClickListener listener) {
        this.listener = listener;
    }

    public void setCurrencies(List<Currency> currencies, Runnable runnable) {
        if (this.currencies == null) {
            this.currencies = currencies;
            notifyItemRangeInserted(0, currencies.size());
        } else new DiffCalculatorTask(this, currencies, runnable).execute();
    }

    public void addCurrency(Currency currency, int position) {
        currencies.add(position, currency);
        notifyItemInserted(position);
    }

    public void removeCurrency(Currency currency) {
        if (!currencies.contains(currency)) return;
        int position = currencies.indexOf(currency);
        currencies.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CurrencyItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.currency_item, parent, false);
        return new ViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bind(currencies.get(position), position);
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CurrencyItemBinding binding;
        private OnCurrencyClickListener listener;

        private ViewHolder(CurrencyItemBinding binding, OnCurrencyClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        private void bind(Currency currency, int position) {
            binding.setCurrency(currency);
            binding.getRoot().setOnClickListener((view) -> listener.onCurrencyClick(currency));
            binding.getRoot().setOnLongClickListener((view) -> listener.onLongCurrencyClick(currency, position));
            binding.mark.setOnClickListener((view) -> listener.onFavoriteMarkClick(currency));
        }
    }

    private static class DiffCalculatorTask extends AsyncTask<Void, Void, DiffUtil.DiffResult> {

        private WeakReference<CurrencyAdapter> adapter;
        private List<Currency> newCurrencyList;
        private Runnable runnable;

        private DiffCalculatorTask(CurrencyAdapter adapter, List<Currency> newCurrencyList, Runnable runnable) {
            this.adapter = new WeakReference<>(adapter);
            this.newCurrencyList = newCurrencyList;
            this.runnable = runnable;
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(Void... voids) {
            return DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return adapter.get().currencies.size();
                }

                @Override
                public int getNewListSize() {
                    return newCurrencyList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return adapter.get().currencies.get(oldItemPosition).equals(
                            newCurrencyList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return adapter.get().currencies.get(oldItemPosition).isFavorite() ==
                            newCurrencyList.get(newItemPosition).isFavorite();
                }
            });
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            if (adapter.get() == null) return;
            adapter.get().currencies = newCurrencyList;
            diffResult.dispatchUpdatesTo(adapter.get());
            runnable.run();
        }
    }
}