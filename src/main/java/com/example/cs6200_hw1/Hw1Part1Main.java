package com.example.cs6200_hw1;

import com.example.cs6200_hw1.Classes.FileConst;
import com.example.cs6200_hw1.DataPreProcess.*;
import com.example.cs6200_hw1.Indexer.CorpusReader;
import com.example.cs6200_hw1.Indexer.IndexReader;
import com.example.cs6200_hw1.Indexer.IndexWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Hw1Part1Main {
    private String xmlFileDir;
    private Set<String> query_tokens = new HashSet<String>();
    private int K = 20;


    public Hw1Part1Main(String xmlFileDir) {
        this.xmlFileDir = xmlFileDir;
    }


    public void run() throws Exception{
        long startTime = System.currentTimeMillis();
        Hw1Part1Main assign1 = new Hw1Part1Main(xmlFileDir);
        assign1.docDataPreProcess(xmlFileDir);

        assign1.queryDataPreProcess(
                "What are the effects of calcium " +
                        "on the physical properties of mucus from CF patients?");
        assign1.WriteIndex();
        long endTime = System.currentTimeMillis();
        System.out.println("Inverted Indexing Time: " + (endTime - startTime)/1000.0 + " seconds.");

        assign1.ReadIndex(K);

    }

    public void docDataPreProcess(String xmlFileDir) throws Exception {
        // Load the xml files and initiate DocCollection
        XmlFileCollection corpus = new XmlFileCollection(xmlFileDir);

        // Load stopword list and initiate the StopWordRemover and WordNormalizer
        StopwordRemover stopwordRemover = new StopwordRemover();
        WordNormalizer wordNormalizer = new WordNormalizer();

        // initiate the BufferedWriter to output result
        FilePathGenerator fpg = new FilePathGenerator("result.txt");
        String path = fpg.getPath();

        FileWriter fileWriter = new FileWriter(path, true); // Path.ResultAssignment1
        Map<String, String> curr_docs = corpus.nextDoc(); // doc_id:doc_content pairs
        Set<String> doc_ids = curr_docs.keySet();
        for (String doc_id : doc_ids){
            // load doc content
            char[] content = curr_docs.get(doc_id).toCharArray();
            // write doc_id into the result file
            fileWriter.append(doc_id + "\n");

            // initiate a word object to hold a word
            char[] word = null;

            // initiate the WordTokenizer
            WordTokenizer tokenizer = new WordTokenizer(content);

            // process the doc word by word iteratively
            while ((word = tokenizer.nextWord()) != null){
                word = wordNormalizer.lowercase(word);
//                if (word.length == 1 && Character.isAlphabetic(word[0])){
//                    continue;
//                }
                String wordStr = String.valueOf(word);
                // write only non-stopword into result file
                if (!stopwordRemover.isStopword(wordStr)){
//                    fileWriter.append(wordNormalizer.toStem(word) + " ");
                    fileWriter.append(wordStr).append(" ");
                }
            }
            fileWriter.append("\n");
        }
        fileWriter.close();
    }


    public void queryDataPreProcess(String queryText) throws Exception {
        // Load stopword list and initiate the StopWordRemover and WordNormalizer
        StopwordRemover stopwordRemover = new StopwordRemover();
        WordNormalizer wordNormalizer = new WordNormalizer();

        // initiate the BufferedWriter to output result
        FilePathGenerator fpg = new FilePathGenerator("result.txt");
        String path = fpg.getPath();

        // queryId
        String queryId = "";
        if (queryId == null || queryId.length() == 0){
//            queryId = UUID.randomUUID().toString().replace("-", "");
            queryId = String.valueOf(FileConst.query_doc_id);
        }

        // append query_id:query_text to exiting doc_id:doc_content text file
        try (FileWriter fileWriter = new FileWriter(path, true); // Path.ResultAssignment1
             BufferedWriter bw = new BufferedWriter(fileWriter)){
            // write doc_id into the result file
            bw.write(queryId + '\n');

            // load doc content
            char[] content = queryText.toCharArray();

            // initiate a word object to hold a word
            char[] word;

            // initiate the WordTokenizer
            WordTokenizer tokenizer = new WordTokenizer(content);

            // process the query word by word iteratively
            while ((word = tokenizer.nextWord()) != null){
                word = wordNormalizer.lowercase(word);
                // write only non-stopword into result file
                if (!stopwordRemover.isStopword(Arrays.toString(word))){
                    bw.append(wordNormalizer.toStem(word)).append(" ");
                    query_tokens.add(wordNormalizer.toStem(word));
                }
            }
            bw.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void WriteIndex() throws Exception {
        // initiate corpus, which is a preprocessed collection of file reader
        CorpusReader corpus = new CorpusReader();
        // initiate the output object
        IndexWriter output = new IndexWriter();

        // Map to hold doc_id and doc content;
        Map<String, String> doc;

        // index the corpus, load the doc one by one
        while ((doc = corpus.NextDoc()) != null){
            // get the doc_id and content of the current doc.
            String doc_id = doc.get("DOC_ID");
            String content = doc.get("CONTENT");

            // index the doc
            output.IndexADoc(doc_id, content);
        }
        output.close_index_writer();
    }

    public void ReadIndex(int K) throws IOException {
        // initiate the index file reader
        IndexReader indexReader = new IndexReader();

        // get doc frequency, termFrequency in all docs
//        int df = indexReader.getDocFreq(token);
//        long collectionTf = indexReader.getCollectionFrequency(token);
////        System.out.println("  The token \"" + token + "\" appeared in " + df + " documents and " + collectionTf
////                + " times in total");
//        if (df > 0) {
//            int[][] posting = indexReader.getPostingList(token);
//            for (int i = 0; i < posting.length; i++) {
//                int doc_id = posting[i][0];
////                int term_freq = posting[i][1];
//                String doc_no = indexReader.getDocNo(doc_id);
////                System.out.println("doc_no: " + doc_id);
//
////                System.out.printf("%10s    %6d    %6d\n", doc_no, doc_id, term_freq);
////                System.out.printf("%10s    10%6d    %6d\n", doc_no, doc_id, indexReader.getTF(token, doc_id));
//                System.out.println("tf_idf: " + indexReader.getTF_IDF_weight(token, doc_id));
//
//            }
//        }


        String doc_no = indexReader.getDocNo(FileConst.query_doc_id);
        // retreived top K relevant doc ids
        System.out.println("=== Top 20 Most Relevant Documents and its Cosine Similarity Score ===");
        indexReader.retrieveRank(query_tokens, Integer.parseInt(doc_no), K);

        indexReader.close();
    }

}
