package com.fintech_school.currency_trader.history.transaction_list_screen;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fintech_school.currency_trader.R;
import com.fintech_school.currency_trader.data.Transaction;
import com.fintech_school.currency_trader.databinding.TransactionItemBinding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public void setTransactions(List<Transaction> transactions, Runnable runnable) {
        if (this.transactions == null) {
            this.transactions = transactions;
            notifyItemRangeInserted(0, transactions.size());
        } else new TransactionAdapter.DiffCalculatorTask(this, transactions, runnable).execute();
    }

    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TransactionItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.transaction_item, parent, false);
        return new TransactionAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final TransactionAdapter.ViewHolder holder, int position) {
        holder.bind(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TransactionItemBinding binding;

        private ViewHolder(TransactionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(Transaction transaction) {
            binding.setTransaction(transaction);
        }
    }

    private static class DiffCalculatorTask extends AsyncTask<Void, Void, DiffUtil.DiffResult> {

        private WeakReference<TransactionAdapter> adapter;
        private List<Transaction> newCurrencyList;
        private Runnable runnable;

        private DiffCalculatorTask(TransactionAdapter adapter, List<Transaction> newCurrencyList,
                                   Runnable runnable) {
            this.adapter = new WeakReference<>(adapter);
            this.newCurrencyList = newCurrencyList;
            this.runnable = runnable;
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(Void... voids) {
            return DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return adapter.get().transactions.size();
                }

                @Override
                public int getNewListSize() {
                    return newCurrencyList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return adapter.get().transactions.get(oldItemPosition).getDate().getTime() ==
                            newCurrencyList.get(newItemPosition).getDate().getTime();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return adapter.get().transactions.get(oldItemPosition).equals(
                            newCurrencyList.get(newItemPosition));
                }
            });
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            if (adapter.get() == null) return;
            adapter.get().transactions = newCurrencyList;
            diffResult.dispatchUpdatesTo(adapter.get());
            runnable.run();
        }
    }
}