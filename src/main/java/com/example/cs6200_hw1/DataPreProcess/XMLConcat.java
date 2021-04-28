package com.example.cs6200_hw1.DataPreProcess;

import java.io.*;
import java.util.Objects;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

public class XMLConcat {
    private File[] rootFiles;


    public XMLConcat() {
        ClassLoader classLoader = getClass().getClassLoader();
        String path  = Objects.requireNonNull(classLoader.getResource("static/txt")).getPath();
        System.out.println("path: " + path);

        File dir = new File(path); //Directory where xml file exists

        rootFiles = dir.listFiles();
    }

    public void xmlConcatenate() throws Throwable {

//        File dir = new File("/tmp/rootFiles");
//        File[] rootFiles = dir.listFiles();

        FilePathGenerator fpg = new FilePathGenerator(".mergedFile.xml");
        String path = fpg.getPath();
//        File id = new File(path); // "data//"+".idno"
//        outputStreamId = new FileOutputStream(id, false);
//        bufferedWriterId = new BufferedWriter(new OutputStreamWriter(outputStreamId));

        Writer outputWriter = new FileWriter(path);
        XMLOutputFactory xmlOutFactory = XMLOutputFactory.newFactory();
        XMLEventWriter xmlEventWriter = xmlOutFactory.createXMLEventWriter(outputWriter);
        XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();

        xmlEventWriter.add(xmlEventFactory.createStartDocument());
        xmlEventWriter.add(xmlEventFactory.createStartElement("", null, "rootSet"));

        XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();
        for (File rootFile : rootFiles) {
            XMLEventReader xmlEventReader = xmlInFactory.createXMLEventReader(new StreamSource(rootFile));
            XMLEvent event = xmlEventReader.nextEvent();
            // Skip ahead in the input to the opening document element
            while (event.getEventType() != XMLEvent.START_ELEMENT) {
                event = xmlEventReader.nextEvent();
            }
            do {
                xmlEventWriter.add(event);
                event = xmlEventReader.nextEvent();
            } while (event.getEventType() != XMLEvent.END_DOCUMENT);
            xmlEventReader.close();
        }

        xmlEventWriter.add(xmlEventFactory.createEndElement("", null, "rootSet"));
        xmlEventWriter.add(xmlEventFactory.createEndDocument());

        xmlEventWriter.close();
        outputWriter.close();
    }
}