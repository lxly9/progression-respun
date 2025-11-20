package com.progression_respun.util;

import net.minecraft.util.StringIdentifiable;

public enum Pebbles implements StringIdentifiable {
    ONE("1"),
    TWO("2"),
    THREE("3");


    private final String name;

    Pebbles(
            final String name
    ) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
