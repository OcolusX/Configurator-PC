package com.example.configurator_pc.model;

import java.util.Objects;

public class Attribute implements Comparable<Attribute> {
    private final String name;
    private final String value;

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public int compareTo(Attribute o) {
        if (o != null) {
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
        Attribute attribute = (Attribute) o;
        if (!Objects.equals(this.name, attribute.name) || !Objects.equals(this.value, attribute.value)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.value});
    }
}
