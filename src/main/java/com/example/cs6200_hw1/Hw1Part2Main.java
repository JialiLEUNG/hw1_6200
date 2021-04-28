package com.example.cs6200_hw1;

import com.example.cs6200_hw1.Classes.FileConst;
import com.example.cs6200_hw1.DataPreProcess.*;
import com.example.cs6200_hw1.Indexer.CorpusReader;
import com.example.cs6200_hw1.Indexer.IndexReader;
import com.example.cs6200_hw1.Indexer.IndexWriter;
import com.example.cs6200_hw2.Medicine;

import java.io.*;
import java.util.*;

/**
 * This class is the main class for retrieving top K relevant documents
 * (and corresponding cosine similarity)
 */
public class Hw1Part2Main {
    private String query_text;
    private Set<String> query_tokens = new HashSet<String>();
    private CosineScoredDoc[] rankedResult;
    private int topK; // for retrieving the topK relevant doc


    public Hw1Part2Main(String query_text, int topK) {
        this.query_text = query_text;
        this.topK = topK;
    }

    public CosineScoredDoc[] getRankedResult() {
        return rankedResult;
    }

    public void run() throws Exception{
        long startTime = System.currentTimeMillis();
        Hw1Part2Main assign1 = new Hw1Part2Main(query_text, topK);
        String[] xmlFileDir = getXMLdirectory();
        for (String xml : xmlFileDir){
            assign1.docDataPreProcess(xml);
        }

        // sample query_text:
        // "What are the effects of calcium on the physical properties of mucus from CF patients?"
        assign1.queryDataPreProcess(query_text);

        assign1.WriteIndex();
        long endTime = System.currentTimeMillis();
        System.out.println("Inverted Indexing Time: " + (endTime - startTime)/1000.0 + " seconds.");

        rankedResult = assign1.ReadIndex();
    }

    public String[] getXMLdirectory(){
        ClassLoader classLoader = getClass().getClassLoader();
        String path  = Objects.requireNonNull(classLoader.getResource("static/txt")).getPath();
        File dir = new File(path); //Directory where xml file exists
        File[] files = dir.listFiles();
        List<String> paths = new ArrayList<>();

        for(File file : files) {
            // You can validate file name with extension if needed
            if(file.isFile() && file.getName().endsWith(".xml") && !file.getName().endsWith("cfquery.xml")) {
                paths.add(file.toString());
            }
        }
        String[] pathList = new String[paths.size()];
        for (int i = 0; i < paths.size();i++){
            pathList[i] = paths.get(i);
        }
        return pathList;
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

            // initiate the DigitsRemover
            DigitsRemover digitsRemover = new DigitsRemover();

            // process the doc word by word iteratively
            while ((word = tokenizer.nextWord()) != null){
                word = wordNormalizer.lowercase(word);

                String wordStr = String.valueOf(word);
                if (digitsRemover.isNumeric(wordStr)){
                    continue;
                }
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

    /**
     * tokenize, normalize and index the user input of query texts
     * This process is the same as tokenizing, normalizing and indexing
     * the documents as shown in HW1 part1
     * @param queryText
     * @throws Exception
     */
    public void queryDataPreProcess(String queryText) throws Exception {
        // Load stopword list and initiate the StopWordRemover and WordNormalizer
        StopwordRemover stopwordRemover = new StopwordRemover();
        WordNormalizer wordNormalizer = new WordNormalizer();

        // initiate the BufferedWriter to output result
//        FilePathGenerator fpg = new FilePathGenerator("result.txt");
        FilePathGenerator fpg = new FilePathGenerator("query.txt");

        String path = fpg.getPath();

        // queryId
        String queryId = "";
        if (queryId == null || queryId.length() == 0){
//            queryId = UUID.randomUUID().toString().replace("-", "");
            queryId = String.valueOf(FileConst.query_doc_id);
        }

        // append query_id:query_text to exiting query_id:doc_content text file
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

            // initiate the DigitsRemover
            DigitsRemover digitsRemover = new DigitsRemover();
            // process the query word by word iteratively
            while ((word = tokenizer.nextWord()) != null){
                word = wordNormalizer.lowercase(word);

                String wordStr = String.valueOf(word);
                if (digitsRemover.isNumeric(wordStr)){
                    continue;
                }
                // write only non-stopword into result file
                if (!stopwordRemover.isStopword(wordStr)){
//                    fileWriter.append(wordNormalizer.toStem(word) + " ");
                    bw.append(wordStr).append(" ");
                    query_tokens.add(wordStr);
                }
            }
            bw.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Medicine> createMedicines(int count) {
        List<Medicine> medicines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Medicine medicine = new Medicine();
            medicine.setId(i + "");
            medicine.setDescription("Description of " + i + " medicine");
            medicines.add(medicine);
        }
        return medicines;
    }


    /**
     * (1) indexing the doc, write the indexes to docs.
     * (2) turn each doc into Medicine object, and put them into an arraylist
     * so that it could be used as a Json object (serialiation/deserialization) later on
     * @throws Exception
     */
    public List<Medicine> WriteIndex() throws Exception {
        // initiate corpus, which is a preprocessed collection of file reader
        CorpusReader corpus = new CorpusReader();
        // initiate the output object
        IndexWriter output = new IndexWriter();

        // Map to hold doc_id and doc content;
        Map<String, String> doc;

        // turn each doc into json object, and put them in to a list
        List<Medicine> medList = new ArrayList<>();

        // index the corpus, load the doc one by one
        while ((doc = corpus.NextDoc()) != null){
            // get the doc_id and content of the current doc.
            String doc_id = doc.get("DOC_ID");
            String content = doc.get("CONTENT");

            // index the doc
            output.IndexADoc(doc_id, content);

            // create a medicine json object for the doc
            Medicine med = new Medicine();
            med.setDocId(doc_id);
            med.setDescription(content);

            // put it into the list
            medList.add(med);
        }
        output.close_index_writer();
        return medList;
    }


    /**
     * read the inverted index file, compute the cosine similarity between query and doc,
     * and return a list of topK relevant documents
     * @return
     * @throws IOException
     */
    public CosineScoredDoc[] ReadIndex() throws IOException{
        // initiate the index file reader
        IndexReader indexReader = new IndexReader();
        String doc_no = indexReader.getDocNo(FileConst.query_doc_id);
        // retrieved top K relevant doc ids
        System.out.println("=== Top 20 Most Relevant Documents and its Cosine Similarity Score ===");
        CosineScoredDoc[] rankedResult = indexReader.retrieveRank(query_tokens, Integer.parseInt(doc_no), topK);
        indexReader.close();
        return rankedResult;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }


    public Set<String> getQuery_tokens() {
        return query_tokens;
    }
}
