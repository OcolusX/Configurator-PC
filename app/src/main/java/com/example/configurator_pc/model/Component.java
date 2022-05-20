package com.example.configurator_pc.model;

import java.util.List;
import java.util.Objects;

public class Component implements Comparable<Component> {
    private final List<Attribute> attributeList;
    private final String description;

    private final int id;
    private final String image;
    private final String name;
    private final List<Price> priceList;
    private final ComponentType type;

    public Component(int id, String name, String description, ComponentType type, String image, List<Attribute> attributeList, List<Price> priceList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.image = image;
        this.attributeList = attributeList;
        this.priceList = priceList;
    }

    public float getAveragePrice() {
        float sum = 0.0f;
        int notNullPriceNumber = 0;
        for (Price price : this.priceList) {
            float value = price.getPrice();
            if (value != -1.0f) {
                sum += value;
                notNullPriceNumber++;
            }
        }
        return sum / ((float) notNullPriceNumber);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public ComponentType getType() {
        return this.type;
    }

    public String getImage() {
        return this.image;
    }

    public List<Attribute> getAttributeList() {
        return this.attributeList;
    }

    public List<Price> getPriceList() {
        return this.priceList;
    }

    public int compareTo(Component o) {
        if (o != null) {
            int compare = this.type.getId() - o.type.getId();
            if (compare != 0) {
                return compare;
            }
            return this.name.compareTo(o.name);
        }
        throw new NullPointerException();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.id == ((Component) o).id) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.id)});
    }
}
