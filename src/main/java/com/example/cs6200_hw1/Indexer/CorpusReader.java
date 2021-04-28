package com.example.cs6200_hw1.Indexer;

import com.example.cs6200_hw1.DataPreProcess.FilePathGenerator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CorpusReader{

    private BufferedReader bufferedReader;

    public CorpusReader() throws IOException {
        FilePathGenerator fpg = new FilePathGenerator("result.txt");
        String path = fpg.getPath();
        System.out.println("path of result.txt in Corpus Reader: " + path);
        InputStream inputStream = new FileInputStream(path); // Path.ResultAssignment1
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    /**
     * make a <"DOC_ID", doc_id> and a <"CONTENT", content> map.
     * @return the map
     * @throws IOException
     */

    public Map<String, String> NextDoc() throws IOException {
        Map<String, String> docMap = new HashMap<>();

        String doc_id = "";
        String doc_content = "";

        if ((doc_id = bufferedReader.readLine()) != null){
            doc_content = bufferedReader.readLine();
            docMap.put("DOC_ID", doc_id);
            docMap.put("CONTENT", doc_content);
            return docMap;
        }

        if (bufferedReader != null){
            bufferedReader.close();
        }
        return null;
    }
}
