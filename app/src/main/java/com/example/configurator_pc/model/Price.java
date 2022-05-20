package com.example.configurator_pc.model;

import java.util.Date;
import java.util.Objects;

public class Price implements Comparable<Price> {
    private final Currency currency;
    private final Date date;
    private final float price;
    private final String storeName;
    private final String url;

    public Price(float price, Currency currency, String storeName, String url, Date date) {
        this.price = price;
        this.currency = currency;
        this.storeName = storeName;
        this.url = url;
        this.date = date;
    }

    public float getPrice() {
        return this.price;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public String getStoreName() {
        return this.storeName;
    }

    public String getUrl() {
        return this.url;
    }

    public Date getDate() {
        return this.date;
    }

    public int compareTo(Price o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (this == o) {
            return 0;
        } else {
            int compare = Float.compare(this.price, o.price);
            if (compare != 0) {
                return compare;
            }
            return this.storeName.compareTo(o.storeName);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Price price2 = (Price) o;
        if (Float.compare(price2.price, this.price) != 0 || !Objects.equals(this.url, price2.url)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Float.valueOf(this.price), this.url});
    }
}
