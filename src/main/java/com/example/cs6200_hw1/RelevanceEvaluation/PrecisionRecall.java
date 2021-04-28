package com.example.cs6200_hw1.RelevanceEvaluation;

import com.example.cs6200_hw1.DataPreProcess.CosineScoredDoc;
import com.example.cs6200_hw1.DataPreProcess.DeleteFile;
import com.example.cs6200_hw1.DataPreProcess.FilePathGenerator;
import com.example.cs6200_hw1.Hw1Part2Main;
import com.example.cs6200_hw1.Models.Query;
import com.example.cs6200_hw1.Models.QueryRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.util.*;

public class PrecisionRecall {
    private HashMap<String, double[]> precisionRecallMap = new HashMap<>();
    private int topK = 0;


    public HashMap<String, double[]> getPrecisionRecallMap() {
        return precisionRecallMap;
    }

    public PrecisionRecall(int topK) {
        this.topK = topK;
    }

    public void run() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path  = Objects.requireNonNull(classLoader.getResource("static/txt")).getPath();
        System.out.println("precision recall path: " + path);

        ObjectMapper mapper = new XmlMapper();
        InputStream inputStream = new FileInputStream(path + "cfquery.xml");
        TypeReference<List<Query>> queryReference = new TypeReference<>() {};
        List<Query> queries = new ArrayList<>();

        // write the precisionRecall map to disk (k,queryNo,precisionAtK,recallAtK)
        FilePathGenerator fpg = new FilePathGenerator("precisionRecall.txt");
        String path_pr = fpg.getPath();
        File prFile = new File(path_pr);
        OutputStream outputStream = new FileOutputStream(prFile, true);
        BufferedWriter bufferedWriterId = new BufferedWriter(new OutputStreamWriter(outputStream));
        try{
            queries = mapper.readValue(inputStream, queryReference);
        } catch (IOException e) {
            System.out.println("error");
            e.printStackTrace();
        }

        for (Query q : queries){
            // delete files: result.txt, etc
            String[] filenames = {"result.txt", ".idno", ".ridx_docId",
                    ".idno_reversed", ".termcount", ".dict", ".ridx", ".ranking"};
            DeleteFile df = new DeleteFile(filenames);
            df.delete();

            String query = q.getQueryText();
            String queryNo = q.getQueryNumber();
            List<QueryRecord> relevantDocsStrArr = q.getRecords();

            int allRelevantDoc = Integer.parseInt(q.getResults());
            double precisionAtK;
            double recallAtK;
            double matchedDoc = 0.0;

            Hw1Part2Main assign1 = new Hw1Part2Main(query, topK);
            try {
                System.out.println("query: " + query);
                assign1.run();
                CosineScoredDoc[] rankedResult = assign1.getRankedResult();
                for (CosineScoredDoc r : rankedResult){
                    for (QueryRecord qr : relevantDocsStrArr){
                        String itemNumber = qr.getItem();
                        if (Integer.parseInt(itemNumber) == Integer.parseInt(r.getId())){
                            matchedDoc++;
                        }
                    }
                }
                precisionAtK =  Math.round((matchedDoc/ this.topK) * 1000);
                precisionAtK = precisionAtK / 1000; // round to the 2nd decimal
                recallAtK = Math.round((matchedDoc / allRelevantDoc) * 1000);
                recallAtK = recallAtK / 1000;
                double[] val = {precisionAtK, recallAtK, matchedDoc};
                precisionRecallMap.put(queryNo, val);

                System.out.println("=== K: " + topK + ", P@K: " + precisionAtK + ", R@K: "
                        + recallAtK + ", MatchedDocNumber: " + matchedDoc + " ====");
                bufferedWriterId.write(queryNo + "," + topK + "," + precisionAtK + "," + recallAtK + "," + matchedDoc +"\n");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bufferedWriterId.close();
    }
}
