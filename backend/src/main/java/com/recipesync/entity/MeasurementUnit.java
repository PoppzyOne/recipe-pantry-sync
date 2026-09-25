package com.recipesync.entity;

public enum MeasurementUnit {
    G("g"),
    KG("kg"),
    ML("ml"),
    CL("cl"),
    DL("dl"),
    L("l"),
    KRM("krm"),
    TSK("tsk"),
    MSK("msk"),
    ST("st"),
    PKT("pkt"),
    BURK("burk"),
    KLYFTA("klyfta"),
    PORT("port"),
    OTHER("övrigt");

    private final String symbol;

    MeasurementUnit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static MeasurementUnit fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim().toLowerCase();
        for (MeasurementUnit unit : values()) {
            if (unit.name().equalsIgnoreCase(clean) || unit.symbol.equalsIgnoreCase(clean)) {
                return unit;
            }
        }
        return OTHER;
    }
}
