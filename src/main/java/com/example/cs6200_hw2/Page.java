package com.example.cs6200_hw2;

import java.util.Collections;
import java.util.List;

public class Page<T> {

    public static final Page EMPTY = new Page(Collections.emptyList(), null, 0, 0);

    private final List<T> medicine;
    private final String input;
    private final int from;
    private final int size;

    public Page(List<T> medicine, String input, int from, int size) {
        this.medicine = medicine;
        this.input = input;
        this.from = from;
        this.size = size;
    }

    List<T> get() {
        return Collections.unmodifiableList(medicine);
    }

    public String getInput() {
        return input;
    }

    public int getFrom() {
        return from;
    }

    public int getSize() {
        return size;
    }
}
