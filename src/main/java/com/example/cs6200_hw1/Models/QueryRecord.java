package com.example.cs6200_hw1.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryRecord {
    @JacksonXmlProperty(isAttribute = true)
    private String score;

    @JacksonXmlElementWrapper(localName = "Item", useWrapping = false)
    @JacksonXmlProperty(localName = "Item")
    @JacksonXmlText
    private  String Item;

    public QueryRecord() {
    }

    public QueryRecord(String item) {
        super();
        item = item;
    }

    public String getItem() {
        return Item;
    }

    public void setItem(String item) {
        Item = item;
    }

    public String getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "QueryRecord{" +
                "Item=" + Item +
                '}';
    }
}
