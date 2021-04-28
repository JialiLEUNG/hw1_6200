package com.example.cs6200_hw1.DataPreProcess;

import com.example.cs6200_hw1.Models.Query;
import com.example.cs6200_hw1.Models.Record;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.*;
import java.util.*;


public class XmlFileCollection implements DocCollection{
    private ObjectMapper mapper;
    private InputStream inputStream;
    private TypeReference<List<Record>> recordReference;
    private TypeReference<List<Query>> queryReference;

    public XmlFileCollection(String xmlFileDir) throws Exception {
        mapper = new XmlMapper();
        inputStream = new FileInputStream(xmlFileDir);
        recordReference = new TypeReference<>() {};
        queryReference = new TypeReference<>() {};
    }


    /**
     * nextDoc loads one document from the corpus,
     * and return this document's id and content.
     * (doc_id: doc_content pair)
     * @return document's id and content, and return null when no document left
     * @throws IOException
     */

    public Map<String, String> nextDoc() throws Exception {
        Map<String, String> docMap = new HashMap<>(); // doc_id, doc_content pair
        List<Record> records = mapper.readValue(inputStream, recordReference);
        for(Record r : records) {
            String docId = r.getRECORDNUM();
            if (docId == null || docId.length() == 0){
                docId = UUID.randomUUID().toString().replace("-", "");
            }
//            System.out.println("major subject: " + r.getMAJORSUBJ().toString());
            docMap.put(docId, r.getContent());
        }
        return docMap;
    }

//    public Map<String, List<String>> nextQuery() throws Exception{
//        Map<String, List<String>> queryMap = new HashMap<>(); // doc_id, doc_content pair
//        List<Query> queries = mapper.readValue(inputStream, queryReference);
//        for(Query q : queries) {
//            String queryNumber = q.getQueryNumber();
//            if (queryNumber == null || queryNumber.length() == 0){
//                queryNumber = UUID.randomUUID().toString().replace("-", "");
//            }
//            List<String> results_records = new ArrayList();
//            results_records.add(q.getResults());
//            results_records.add(Arrays.toString(q.getRecords().getItems()));
//            queryMap.put(q.getQueryText(), results_records);
//        }
//        return queryMap;
//    }
}
