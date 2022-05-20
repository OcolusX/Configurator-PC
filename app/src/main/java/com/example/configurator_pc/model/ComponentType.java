package com.example.configurator_pc.model;

public enum ComponentType {
    MOTHERBOARD(1),
    CPU(2),
    COOLER(3),
    GRAPHICS_CARD(4),
    RAM(5),
    HDD(6),
    SSD(7),
    CASE(8),
    POWER_SUPPLY(9);

    private final int id;

    ComponentType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static ComponentType getById(int id) {
        return values()[id - 1];
    }
}
