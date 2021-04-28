package com.example.cs6200_hw1.Indexer;

import com.example.cs6200_hw1.Classes.FileConst;
import com.example.cs6200_hw1.DataPreProcess.FilePathGenerator;
import com.example.cs6200_hw1.DataPreProcess.CosineScoredDoc;

import java.io.*;
import java.util.*;

/**
 * read the ridx file and store the index.
 * returns a list of topK cosineScored Doc (sorted by scores).
 */
public class IndexReader {
    private BufferedReader dictBufferedReader;
    private Map<String, String> termDict; // term dict is map of term:term_id pair
    private BufferedReader docIdBufferedReader;
    private Map<String, String> mapDocIdNo; // mapDocIdNo is map of doc_number: doc_id pair (doc_no is the new doc no)
    private Map<String, String> mapDocIdNo_reversed; // mapDocIdNo is map of doc_id:doc_number pair
    private BufferedReader invertIndexBufferedReader;
    private BufferedReader termCountBufferedReader;
    private Map<Integer, Integer> termCountDict; // termCountDict is map of doc_id: termCounts (in that doc)
    private Map<String, Integer> totalDocByTerm = new HashMap<>();
    private Map<String, Map<String, Integer>> termFreqByDocId= new HashMap<>();
    private Set<String> docIdBag = new HashSet<>();


    public IndexReader() throws IOException {

        FilePathGenerator fpg = new FilePathGenerator(".dict");
        String path = fpg.getPath();
        String str = "";
        // build termDict dictionary which is term: term_id pair from .dict
        InputStream dictInputStream = new FileInputStream(path); // "data//" + ".dict"
        dictBufferedReader = new BufferedReader(new InputStreamReader(dictInputStream));
        termDict = new HashMap<>();
        while ((str = dictBufferedReader.readLine()) != null){
            String[] s = str.split(",");
            termDict.put(s[0], s[1]);
        }

        // build mapDocIdNo dictionary which is doc_number : doc_id pair from .dict
        FilePathGenerator fpg_idno = new FilePathGenerator(".idno");
        String path_idno = fpg_idno.getPath();
        InputStream fisIdno = new FileInputStream(path_idno); // "data//" + ".idno"
        docIdBufferedReader = new BufferedReader(new InputStreamReader(fisIdno));
        mapDocIdNo = new HashMap<>();
        mapDocIdNo_reversed = new HashMap<>();

        while ((str = docIdBufferedReader.readLine()) != null){
            String[] s = str.split(",");
            mapDocIdNo.put(s[0], s[1]);
            mapDocIdNo_reversed.put(s[1], s[0]);

        }


        // build inverse index, which is term: docIds pair from .ridx_docId
        FilePathGenerator fpg_dict = new FilePathGenerator(".ridx_docId");
        String path_dict = fpg_dict.getPath();
        InputStream invertIndexInputStream = new FileInputStream(path_dict); // "data//" + ".ridx_docId"
        invertIndexBufferedReader = new BufferedReader(new InputStreamReader(invertIndexInputStream));

        System.out.println("Size of the Inverted Index File: " + (double) path_dict.length()/FileConst.kb_length + " kb");


        // build total term counts by doc dictionary, which is docId:totalTermCounts pair from .termCount
        FilePathGenerator fpg_termCount = new FilePathGenerator(".termCount");
        String path_termCount = fpg_termCount.getPath();
        InputStream termCountInputStream = new FileInputStream(path_termCount); // "data//" + ".ridx_docId"
        termCountBufferedReader = new BufferedReader(new InputStreamReader(termCountInputStream));

        termCountDict = new HashMap<>();
        while ((str = termCountBufferedReader.readLine()) != null){
            String[] s = str.split(",");
            termCountDict.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
        }

        // build term: <docId:termFreq> hashmap.
        buildTermFreqByDocId();
    }

    public int getTotalDocNumber() throws IOException{
        return mapDocIdNo.keySet().size();
    }

    /**
     * get the total number of docs that contains the token
     * @param token
     * @return
     * @throws IOException
     */
    public int getDocFreq(String token) throws IOException{
        // 27:3,135,184,202;38:2,64,164;61:1,193;145:1,76;
        // doc_id: term_positions;
        String postingStr = this.getPostings(token);
        if (postingStr != ""){
            String[] postings = postingStr.split(";");
            return postings.length;
        }
        return 0;
    }

    /**
     * calculate IDF (Inverse Document Frequency):
     * The main purpose of doing a search is to find out relevant documents matching the query.
     * In the first step all terms are considered equally important.
     * In fact certain terms that occur too frequently have little power in determining the relevance.
     * We need a way to weigh down the effects of too frequently occurring terms.
     * Also the terms that occur less in the document can be more relevant.
     * We need a way to weigh up the effects of less frequently occurring terms.
     * Logarithms helps us to solve this problem.
     * @param token
     * @return
     * @throws IOException
     */
    public double getIDF(String token) throws IOException{
        int totalDocInATerm = totalDocByTerm.getOrDefault(token, 0);
        if (totalDocInATerm > 0){
            // IDF(game) = 1 + log(Total Number Of Documents / Number Of Documents with term game in it)
//            return 1.0 + Math.log((double) getTotalDocNumber()/this.getDocFreq(token));
            return 1.0 + Math.log((double) getTotalDocNumber()/totalDocInATerm);

        } else {
            return 1.0;
        }
    }

    public void buildTermFreqByDocId() throws IOException {
        // acutely 33:1,85;176:2,71,105;
        FilePathGenerator fpg_ridx = new FilePathGenerator(".ridx");
        String path_ridx = fpg_ridx.getPath();
        InputStream fileInputStream = new FileInputStream(path_ridx); // "data//" + ".ridx"
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String currLine;

        while ((currLine = bufferedReader.readLine()) != null){
            String[] s = currLine.split("\\s");
            String term = s[0];

            String postingStr = s[1];
            // 33:1,85
            String[] postings = postingStr.split(";");
            totalDocByTerm.put(term, postings.length);
            for (String posting : postings){
                // 33:1,85; --> <docId: termFreq, termPositions...>
                String[] p = posting.split(":");
                String docId = p[0];
                int termFreq = Integer.parseInt(p[1].split(",")[0]);
                if (!termFreqByDocId.containsKey(term)){
                    termFreqByDocId.put(term, new HashMap<>());
                }

                if (!termFreqByDocId.get(term).containsKey(docId)){
                    termFreqByDocId.get(term).put(docId, termFreq);
//                    System.out.println("term: " + term + ", docId: " + docId + ", termFreq: " + termFreq);
                }
            }
        }
    }

    /**
     * get postings
     * @param token
     * @return
     * @throws IOException
     */
    public String getPostings(String token) throws IOException{
        int currLine = Integer.parseInt(termDict.getOrDefault(token, "-1"));
        String[] pos;
        FilePathGenerator fpg_ridx = new FilePathGenerator(".ridx");
        String path_ridx = fpg_ridx.getPath();
        InputStream fileInputStream = new FileInputStream(path_ridx); // "data//" + ".ridx"
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        for (int i = 0; i < currLine; i++){
            bufferedReader.readLine();
        }
        // rabbit 27:3,135,184,202;38:2,64,164;61:1,193;145:1,76;
        // separate dict from postings, and return postings only.
        pos = bufferedReader.readLine().split("\\s");
        bufferedReader.close();
//        System.out.println("posting: " + pos[1]);
        return pos[1];
    }

    /**
     * get the total number of tokens in the collection.
     * @param token
     * @return
     * @throws IOException
     */
    public long getCollectionFrequency(String token) throws IOException{
        String postingsStr = this.getPostings(token);
        if (postingsStr != ""){
            // e.g., 27:3,135,184,202;38:2,64,164;61:1,193;145:1,76;
            // dic_id:term_frequency,term_positions...
            // extract term_frequency
            String[] postings = postingsStr.split(";");
            int doc_count = 0;
            for (String posting : postings){
                if (posting != ""){
                    doc_count += Integer.parseInt(posting.split(":")[1].split(",")[0]);
                }
            }
            return doc_count;
        }
        return 0;
    }

    /**
     * Build a 2d array to store the doc_id and corresponding term frequency
     * for example, for term "rabbit" (in the following matrix below)
     * it appears in doc_id 27, 38, 61, 145 (as in left column)
     * and the right column shows the term frequency.
     *
     * doc_id    term_frequency
     * 27         3
     * 38         2
     * 61         1
     * 145        1

     * @param token
     * @return
     * @throws IOException
     */
    public int[][] getPostingList(String token) throws IOException{
        String postingStr = this.getPostings(token);
        if (postingStr != ""){
            String[] postings = postingStr.split(";");
            int[][] tf_table = new int[postings.length][2];
            int i = 0;
            for (String posting : postings){
                if (posting != ""){
                    tf_table[i][0] = Integer.parseInt(posting.split(":")[0]); // term
                    tf_table[i][1] = Integer.parseInt(posting.split(":")[1].split(",")[0]); // docId
                    i++;
                }
            }
            return tf_table;
        }
        return null;
    }

    public String getDocId(String doc_no) {
        return mapDocIdNo.get(doc_no);
    }


    public String getDocNo(int doc_id){
        return mapDocIdNo_reversed.get(Integer.toString(doc_id));
    }

    /**
     * calculate the total number of terms in a specified doc
     * @param doc_id
     * @return
     */
    public int getTotalTermsCount(int doc_id){
        if (termCountDict.containsKey(doc_id)){
            return termCountDict.get(doc_id);
        }
        return 0;
    }

    /**
     * Term Frequency measures the number of times a term (word) occurs in a document.
     * @param token
     * @param doc_id
     * @return
     * @throws IOException
     */
    public double getTF(String token, int doc_id) throws IOException {
       return termFreqByDocId.get(token).getOrDefault(String.valueOf(doc_id), 0);
    }

    /**
     * divide the term frequency by the total number of terms
     * @param token
     * @param doc_id
     * @return
     * @throws IOException
     */
    public double getTF_normalized(String token, int doc_id) throws IOException{
        int totalTermsCount = getTotalTermsCount(doc_id);

        if ( totalTermsCount != 0){
            return getTF(token, doc_id)/totalTermsCount;
        } else {
            return 0.0;
        }
    }


    /**
     * Computes the TF-IDF score for each term
     * @param token
     * @param doc_id
     * @return
     * @throws IOException
     */
    public double getTF_IDF_weight(String token, int doc_id) throws IOException{
        double tf = this.getTF_normalized(token, doc_id);
        if (tf == 0){
            return 0.0;
        }
        double idf = this.getIDF(token);
        if (idf == 0){
            return 0.0;
        }
        return Math.log(1 + tf) * idf;
    }

    /**
     * get cosine similarity score between a doc and query on a list of query tokens
     * @param tokens
     * @param doc_id
     * @param query_id
     * @return
     * @throws IOException
     */
    public double getCosineSimilarity(Set<String> tokens, int doc_id, int query_id) throws IOException{
        double dot_product = 0.0;
        double d1 = 0.0;
        double d2 = 0.0;

        for (String token : tokens ){
            if (token == "" || token.length() == 0){
                continue;
            }
            double weight1 = getTF_IDF_weight(token, doc_id);
            double weight2 = getTF_IDF_weight(token, query_id);

            dot_product += weight1 * weight2;
            d1 += Math.pow(weight1, 2);
            d2 += Math.pow(weight2, 2);
        }
        if (dot_product == 0 || d1 == 0 || d2 == 0){
            return 0.0;
        }
        return dot_product / (Math.sqrt(d1) * Math.sqrt(d2));
    }

    /**
     * get the cosine similarity between query text and each doc,
     * and then sort the doc by cosine similarity score
     * @param tokens
     * @param queryId
     * @param topK
     * @return a list of CosineScoredDoc
     * @throws IOException
     */
    public CosineScoredDoc[] retrieveRank(Set<String> tokens, int queryId, int topK) throws IOException {
        List<CosineScoredDoc> list = new ArrayList<>();
        CosineScoredDoc[] rankedDocList = new CosineScoredDoc[topK];

        if (topK > mapDocIdNo.keySet().size()){
            return rankedDocList;
        }

        for (String doc_no_str : mapDocIdNo.keySet()){
            String docId = mapDocIdNo.get(doc_no_str);
            if (docIdBag.contains(docId)){
                continue;
            }
            else {
                docIdBag.add(docId);
            }
//            System.out.println("docId: " + docId);
//            System.out.println("doc_no_str: " + doc_no_str);
//            System.out.println("String.valueOf(FileConst.query_doc_id): " + String.valueOf(FileConst.query_doc_id));
            if (docId.equals(String.valueOf(FileConst.query_doc_id))){ //skip the query itself
                continue;
            }
            list.add(new CosineScoredDoc(docId, getCosineSimilarity(tokens, Integer.parseInt(doc_no_str), queryId)));
        }

        Collections.sort(list);
        Collections.reverse(list);
        for (int i = 0; i < topK; i++){
            rankedDocList[i] = list.get(i);
        }
        writeCosineScoredDocToDisk(rankedDocList);
        return rankedDocList;
    }

    /**
     * write the cosineScoreDoc to disk.
     * The file name is called ".ranking"
     * @param rankedDocs
     * @throws IOException
     */
    public void writeCosineScoredDocToDisk(CosineScoredDoc[] rankedDocs) throws IOException {
        // Initiate FileWriter to output the index files
        FilePathGenerator fpg = new FilePathGenerator(".ranking");
        String path = fpg.getPath();
        File id = new File(path); // "data//"+".idno"
        OutputStream outputStream = new FileOutputStream(id, false);
        BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(outputStream));

        for (CosineScoredDoc r : rankedDocs){
            bf.write(r.toString() + '\n');
        }
        bf.close();
    }

    public void close() throws IOException {
        if (dictBufferedReader != null) {
            dictBufferedReader.close();
        }
        if (docIdBufferedReader != null){
            docIdBufferedReader.close();
        }
        if (invertIndexBufferedReader != null) {
            invertIndexBufferedReader.close();
        }
        termDict.clear();
        mapDocIdNo.clear();
        mapDocIdNo_reversed.clear();
    }
}
