package com.example.configurator_pc.model;

public enum Currency {
    RUB('₽'),
    USD('$'),
    EUR('€');

    private char sign;

    Currency(char sign) {
        this.sign = sign;
    }

    public char getSign() {
        return sign;
    }
}
