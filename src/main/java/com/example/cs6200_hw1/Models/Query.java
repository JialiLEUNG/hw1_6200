package com.example.cs6200_hw1.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.*;


@JsonIgnoreProperties(ignoreUnknown = true)
//@JacksonXmlRootElement(localName="QUERIES")
public class Query {
    @JacksonXmlProperty(localName = "QueryNumber")
    private String QueryNumber;
    @JacksonXmlProperty(localName = "QueryText") // query text
    private String QueryText;
    @JacksonXmlProperty(localName = "Results") // how many total relevant documents are there per query
    private String Results;
    @JacksonXmlProperty(localName = "Records") // the set of relevant document IDs
    private List<QueryRecord> Records;


    public Query() {
    }

    public Query(String queryText) {
        QueryText = queryText;
    }

    public String getQueryNumber() {
        return QueryNumber;
    }

    public void setQueryNumber(String queryNumber) {
        QueryNumber = queryNumber;
    }

    public String getQueryText() {
        return QueryText;
    }

    public void setQueryText(String queryText) {
        QueryText = queryText;
    }

    public String getResults() {
        return Results;
    }

    public void setResults(String results) {
        Results = results;
    }

    public List<QueryRecord> getRecords() {
        sortRecordsByScore();
        return Records;
    }

    public void setRecords(List<QueryRecord> records) {
        Records = records;
    }

    public String getContent() {
        return QueryText + " [" + Results + ", " ;
    }

    public void sortRecordsByScore() { // sort scores in a descending order
        Collections.sort(Records, new Comparator<QueryRecord>() {
            @Override
            public int compare(QueryRecord o1, QueryRecord o2) {
                return o2.getScore().compareTo(o1.getScore());
            }
        });

    }


    @Override
    public String toString() {
        return "Query{" +
                "QueryNumber='" + QueryNumber + '\'' +
                ", QueryText=" + QueryText + '\'' +
                ", Results='" + Results + '\'' +
                ", Records=" + Records +
                '}';
    }
}


//     String docId = 'RECORDNUM';
//    String[] authors = 'AUTHORS';
//    String title = 'TITLE'
//    String source = 'SOURCE';
//    String majorSubj = 'MAJORSUBJ';
//    String minorSubj = 'MINORSUBJ';
//    String extract = 'EXTRACT';
//    String abstracts = 'ABSTRACT';
