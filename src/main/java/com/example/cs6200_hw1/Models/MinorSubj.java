package com.example.cs6200_hw1.Models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Arrays;

public class MinorSubj {
    @JacksonXmlElementWrapper(localName = "TOPIC", useWrapping = false)
    @JacksonXmlProperty(localName = "TOPIC")
    private String[] TOPIC;

    public MinorSubj() {
    }

    public MinorSubj(String[] TOPIC) {
        super();
        this.TOPIC = TOPIC;
    }

    public String[] getTOPIC() {
        return TOPIC;
    }

    public void setTOPIC(String[] TOPIC) {
        this.TOPIC = TOPIC;
    }

    @Override
    public String toString() {
        return "MinorSubj{" +
                "TOPIC=" + Arrays.toString(TOPIC) +
                '}';
    }
}
