package com.fintech_school.currency_trader.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.fintech_school.currency_trader.BR;

@Entity(primaryKeys = {"name"}, tableName = "currencies")
public class Currency extends BaseObservable implements Comparable<Currency>, Parcelable {

    private static int currentMaxMarkedIndex;
    private static int currentMaxIndex;

    @NonNull private String name;
    private double value;
    private int sortIndex;
    private boolean isFavorite;

    @Ignore
    public Currency(@NonNull String name, double value) {
        this.name = name;
        this.value = value;
    }

    public Currency(@NonNull String name, double value, int sortIndex, boolean isFavorite) {
        this.name = name;
        this.value = value;
        this.sortIndex = sortIndex;
        this.isFavorite = isFavorite;
        if (isFavorite && sortIndex > currentMaxMarkedIndex) currentMaxMarkedIndex = sortIndex;
        else if (!isFavorite && sortIndex > currentMaxIndex) currentMaxIndex = sortIndex;
    }

    public void setMaxIndex() {
        if (isFavorite) {
            sortIndex = currentMaxMarkedIndex + 1;
            currentMaxMarkedIndex = sortIndex;
        } else {
            sortIndex = currentMaxIndex + 1;
            currentMaxIndex = sortIndex;
        }
    }

    @Bindable
    @NonNull
    public String getName() {
        return name;
    }

    @Bindable
    public double getValue() {
        return value;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    @Bindable
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setName(@NonNull String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public void setValue(double value) {
        this.value = value;
        notifyPropertyChanged(BR.value);
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
        notifyPropertyChanged(BR.favorite);
    }

    @Override
    public int compareTo(@NonNull Currency currency) {
        if (isFavorite() && !currency.isFavorite()) return 1;
        else if (!isFavorite() && currency.isFavorite()) return -1;
        else if (sortIndex != currency.getSortIndex())
            return Integer.compare(sortIndex, currency.getSortIndex());
        else return (-1) * name.compareToIgnoreCase(currency.getName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeDouble(this.value);
        dest.writeInt(this.sortIndex);
        dest.writeByte(this.isFavorite ? (byte) 1 : (byte) 0);
    }

    protected Currency(Parcel in) {
        this.name = in.readString();
        this.value = in.readDouble();
        this.sortIndex = in.readInt();
        this.isFavorite = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Currency> CREATOR = new Parcelable.Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel source) {
            return new Currency(source);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return name.equals(currency.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}