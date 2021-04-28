package com.example.cs6200_hw1.Models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Arrays;

public class MajorSubj {
    @JacksonXmlElementWrapper(localName = "TOPIC", useWrapping = false)
    @JacksonXmlProperty(localName = "TOPIC")
    private String[] TOPIC;

    public MajorSubj(){};

    public MajorSubj(String[] TOPIC) {
        super();
        this.TOPIC = TOPIC;
    }

    public String[] getTOPIC() {
        if (TOPIC == null){
            String[] strings = new String[0];
            return strings;
        }
        return TOPIC;
    }

    public void setTOPIC(String[] TOPIC) {
        this.TOPIC = TOPIC;
    }

    @Override
    public String toString() {
        return "MajorSubj{" +
                "TOPIC=" + Arrays.toString(TOPIC) +
                '}';
    }
}
