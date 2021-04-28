package com.example.cs6200_hw1.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {
    @JacksonXmlProperty(localName = "RECORDNUM")
    private String RECORDNUM;
//    @JacksonXmlProperty(localName = "AUTHORS")
//    private String[] AUTHORS;
    @JacksonXmlProperty(localName = "TITLE")
    private String TITLE;
    @JacksonXmlProperty(localName = "SOURCE")
    private String SOURCE;
    @JacksonXmlProperty(localName = "MAJORSUBJ")
    private MajorSubj MAJORSUBJ;
    @JacksonXmlProperty(localName = "MINORSUBJ")
    private MinorSubj MINORSUBJ;
    @JacksonXmlProperty(localName = "EXTRACT")
    private String EXTRACT;
    @JacksonXmlProperty(localName = "ABSTRACT")
    private String ABSTRACT;

    public Record() {
    }

    public Record(String RECORDNUM, String TITLE) {
        this.RECORDNUM = RECORDNUM;
        this.TITLE = TITLE;
    }

    public String getRECORDNUM() {
        return RECORDNUM;
    }

    public void setRECORDNUM(String RECORDNUM) {
        this.RECORDNUM = RECORDNUM;
    }

//    public String[] getAUTHORS() {
//        return AUTHORS;
//    }

//    public void setAUTHORS(String[] AUTHORS) {
//
//        this.AUTHORS = AUTHORS;
//    }

    public String getTITLE() {
        if (TITLE == null){
            return "";
        }
        return TITLE;
    }

    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
    }

    public String getSOURCE() {
        if (SOURCE == null){
            return "";
        }
        return SOURCE;
    }

    public void setSOURCE(String SOURCE) {
        this.SOURCE = SOURCE;
    }

    public MajorSubj getMAJORSUBJ() {
        return MAJORSUBJ;
    }

    public void setMAJORSUBJ(MajorSubj MAJORSUBJ) {
        this.MAJORSUBJ = MAJORSUBJ;
    }

    public MinorSubj getMINORSUBJ() {
        return MINORSUBJ;
    }

    public void setMINORSUBJ(MinorSubj MINORSUBJ) {
        this.MINORSUBJ = MINORSUBJ;
    }

    public String getEXTRACT() {
        if (EXTRACT == null){
            return "";
        }
        return EXTRACT;
    }

    public void setEXTRACT(String EXTRACT) {
        this.EXTRACT = EXTRACT;
    }

    public String getABSTRACT() {
        if (ABSTRACT == null){
            return "";
        }
        return ABSTRACT;
    }

    public void setABSTRACT(String ABSTRACT) {
        this.ABSTRACT = ABSTRACT;
    }

    public String getContent() {
        String majorSubj_placeholder;
        String minorSubj_placeholder;
        if (MAJORSUBJ == null){
            majorSubj_placeholder = "";
        } else {
            majorSubj_placeholder = Arrays.toString(MAJORSUBJ.getTOPIC());
        }

        if (MAJORSUBJ == null){
            minorSubj_placeholder = "";
        } else {
            minorSubj_placeholder = Arrays.toString(MINORSUBJ.getTOPIC());
        }

        return
//                Arrays.toString(AUTHORS) + " " +
                TITLE + " " +
                SOURCE + " " +
                majorSubj_placeholder + " " +
                minorSubj_placeholder + " " +
                this.getEXTRACT() + " " +
                this.getABSTRACT();
    }

    @Override
    public String toString() {
        return "Record{" +
                "RECORDNUM='" + RECORDNUM + '\'' +
//                ", AUTHORS=" + Arrays.toString(AUTHORS) +
                ", TITLE='" + TITLE + '\'' +
                ", SOURCE='" + SOURCE + '\'' +
                ", MAJORSUBJ=" + MAJORSUBJ +
                ", MINORSUBJ=" + MINORSUBJ +
                ", EXTRACT='" + EXTRACT + '\'' +
                ", ABSTRACT='" + ABSTRACT + '\'' +
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
