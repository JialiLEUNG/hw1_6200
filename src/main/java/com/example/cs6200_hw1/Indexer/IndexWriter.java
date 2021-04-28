package com.example.cs6200_hw1.Indexer;

import com.example.cs6200_hw1.DataPreProcess.FilePathGenerator;

import java.io.*;
import java.nio.Buffer;
import java.util.*;

public class IndexWriter {

    private int new_doc_id;
    private int new_num_in_block;
    private RawIndex rawIndexBlock;
    private int new_block_id;
    private BufferedWriter bufferedWriterId; // docNo:docId pairs
    private BufferedWriter bufferedWriterId_reversed; // docId:docNo pairs
    private BufferedWriter bufferedWriter;
    private BufferedWriter bufferedWriterDocId;

    private BufferedWriter bufferedWriterTotalTermCount;
    private OutputStream outputStreamId;
    private OutputStream outputStreamId_reversed;
    private OutputStream outputStreamTermCount;
    private int totalTermsCount;
    private static final int MAX_IN_BLOCK = 30000;

    public IndexWriter() throws IOException {
        // Initiate FileWriter to output the index files
        FilePathGenerator fpg = new FilePathGenerator(".idno");
        String path = fpg.getPath();
        File id = new File(path); // "data//"+".idno"
        outputStreamId = new FileOutputStream(id, false);
        bufferedWriterId = new BufferedWriter(new OutputStreamWriter(outputStreamId));

        // Initiate FileWriter to output the index files
        FilePathGenerator fpg_reversed = new FilePathGenerator(".idno_reversed");
        String path_reversed = fpg_reversed.getPath();
        File id_reversed = new File(path_reversed); // "data//"+".idno"
        outputStreamId_reversed = new FileOutputStream(id_reversed, false);
        bufferedWriterId_reversed = new BufferedWriter(new OutputStreamWriter(outputStreamId_reversed));


        FilePathGenerator fpg_termcount = new FilePathGenerator(".termcount");
        String path_termcount = fpg_termcount.getPath();
        File termcount = new File(path_termcount); // "data//"+".idno"
        outputStreamTermCount = new FileOutputStream(termcount, false);
        bufferedWriterTotalTermCount = new BufferedWriter(new OutputStreamWriter(outputStreamTermCount));

        rawIndexBlock = new RawIndex();
    }

    /**
     * RawIndex is to store all key-value pairs into one termMap.
     * key: term (string)
     * value: ArrayList of ArrayList of Integer
     * for example, "Alice": [[0, 1, 60], [3, 1, 63]] --> local termMap
     * for each subarray in a local termMap, [doc_id, term_frequency, term_position]
     */
    private static final class RawIndex {
        private Map<String, ArrayList<ArrayList<Integer>>> globalTermMap;

        // constructor
        RawIndex() {
            globalTermMap = new HashMap<>();
        }

        // Update the global termMap by adding the localTermMap (if term does not exist)
        // or attaching the localTermMap to the end of the existing arrayList (if term exists)
        private boolean update(Map<String, ArrayList<Integer>> doc) {
            if (doc != null) {
                doc.forEach((term, localTermMap) -> {
                    if (!this.globalTermMap.containsKey(term))
                        this.globalTermMap.put(term, new ArrayList<>(Arrays.asList(localTermMap)));
                    else
                        this.globalTermMap.get(term).add(localTermMap);
                });
                return true;
            }
            return false;
        }

        public void sortIndexMapByTerm(){
            // Convert IndexMap into List
            List<Map.Entry<String, ArrayList<ArrayList<Integer>>>> list = new LinkedList<> (globalTermMap.entrySet());
            // Sort List with Comparator, to compare Key (term) values,
            // aka sort terms alphabetically
            Collections.sort(list, new Comparator<Map.Entry<String, ArrayList<ArrayList<Integer>>>>() {
                @Override
                public int compare(Map.Entry<String, ArrayList<ArrayList<Integer>>> o1, Map.Entry<String, ArrayList<ArrayList<Integer>>> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });

            //Convert sorted map back to a Map
            HashMap<String,ArrayList<ArrayList<Integer>>> sortedMap = new LinkedHashMap<>();
            for (Iterator<Map.Entry<String, ArrayList<ArrayList<Integer>>>> it = list.iterator(); it.hasNext();){
                Map.Entry<String, ArrayList<ArrayList<Integer>>> entry = it.next();
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            globalTermMap = sortedMap;
        }

        private void clear() {
            if (!globalTermMap.isEmpty())
                globalTermMap.clear();
        }
    }


    /**
     * RawIndex is to store all local termMaps into one termMap.
     * key: term (string)
     * value: ArrayList of ArrayList of Integer
     * for example, "Alice": [[0, 1, 60], [3, 1, 63]] --> local termMap
     * for each subarray in a local termMap, [doc_id, term_frequency, term_position]
     */
    private static final class CountOfTermsByDoc {
        private Map<String, Integer> globalTermCountMap;

        // constructor
        CountOfTermsByDoc() {
            globalTermCountMap = new HashMap<>();
        }

        // Update the global termMap by adding the localTermCountMap (if docId does not exist)
        // or updating localTermCountMap (if term exists)
        private boolean update(Map<String, Integer> doc) {
            if (doc != null) {
                doc.forEach((docId, totalTermsCount) -> {
                    this.globalTermCountMap.put(docId, totalTermsCount);
                });
                return true;
            }
            return false;
        }

        private void clear() {
            if (!globalTermCountMap.isEmpty())
                globalTermCountMap.clear();
        }
    }




    /**
     * This is the key method of IndexWriter.
     * It inverses the index.
     * If the token does not appear before:
     * put <currToken-[Doc_ID, term_frequency, term_position]> pair into indexMap
     * the first element is doc_id, second is term frequency,
     * and the third one is the position of the term (term_id)
     * If the token already exists in the indexMap:
     * update the current term_id (add it to the end of the value array),
     * and increment of the term frequency by 1.
     * @param content
     * @return
     */
    private Map<String, ArrayList<Integer>> inverse(String content){
        Map<String, ArrayList<Integer>> indexMap = new HashMap<>();
        // preprocessed content separated by whitespace
        String[] tokens = content.split("\\s");
        totalTermsCount = tokens.length; // total number of terms in the current doc

        for (int i = 0; i < totalTermsCount; i++){
            String currToken = tokens[i];
            if (currToken.length() == 0){
                continue;
            }
            if (!indexMap.containsKey(currToken)){
                indexMap.put(currToken, new ArrayList<>(Arrays.asList(new_doc_id, 1, i)));
//                totalTermsCount++;
            }
            else {
                indexMap.get(currToken).add(i);
                indexMap.get(currToken).set(1, indexMap.get(currToken).get(1) + 1);
            }
        }
        return indexMap;
    }



    /**
     * Index a document, and assign the new doc_id to each doc.
     * @param doc_id
     * @param content
     * @throws IOException
     */
    public void IndexADoc(String doc_id, String content) throws IOException{
        Map<String, ArrayList<Integer>> mapInverted;
        mapInverted = this.inverse(content);
        // update the map term across docs
        // (i.e., add the current map term into rawIndexBlock (existing term map))
        rawIndexBlock.update(mapInverted);
        rawIndexBlock.sortIndexMapByTerm();

        // count total docs indexed in rawIndexBlock
        new_num_in_block++;
        // write new_doc_id:doc_id pair to file
        bufferedWriterId.write(new_doc_id + "," + doc_id + '\n');
        // write doc_id:new_doc_id pair to file
        bufferedWriterId_reversed.write(doc_id + "," + new_doc_id + '\n');
        bufferedWriterTotalTermCount.write(new_doc_id + "," + totalTermsCount + '\n');
//        System.out.println("total term count: " + totalTermsCount);
        // wrote new_doc_id:totalTermsCount to file
        // increment (new) doc_id for next doc
        new_doc_id++;
        if (new_num_in_block == MAX_IN_BLOCK) {
            this.write_block_to_disk();
        }
    }


    private void write_block_to_disk() throws IOException{
        // write index (partial) onto the disk
        FilePathGenerator fpg = new FilePathGenerator(".ridx");
        String path = fpg.getPath();
        File file = new File( path + new_block_id); // "data//." + ".ridx" + new_block_id
        OutputStream outputStream = new FileOutputStream(file, false);
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

        // write index (partial) with doc_id only onto the disk
        FilePathGenerator fpg_docId = new FilePathGenerator(".ridx_docId");
        String path_docId = fpg_docId.getPath();
        File doc_id_only_index = new File(path_docId); // "data//" + ".ridx_docId"
        OutputStream outputStream_docId = new FileOutputStream(doc_id_only_index, false);
        bufferedWriterDocId = new BufferedWriter(new OutputStreamWriter(outputStream_docId));

        // posting: [0, 1, 60, etc] is [doc_id, term_frequency, term_position]
        // posting_docId: [doc_id]
        // write them to token and posting to disk
        rawIndexBlock.globalTermMap.forEach((token, posting) -> {
            try{
                bufferedWriter.write(token + " ");
                bufferedWriterDocId.write(token + ":[");
//                bufferedWriterDocId_simplied.write(token + ":");
                for (int i = 0; i < posting.size(); i++){
                    Integer doc_id = posting.get(i).get(0);
                    Integer term_frequency = posting.get(i).get(1);
                    bufferedWriter.write(doc_id + ":" + term_frequency + ",");
                    String docId_str = ( i == (posting.size() -1 ) ? "]" : ","  );
                    bufferedWriterDocId.write(doc_id + docId_str);
//                    bufferedWriterDocId_simplied.write(doc_id);
                    for (int j = 2; j < posting.get(i).size(); j++){
                        String s = (j == (posting.get(i).size() - 1) ? ";": ",");
                        Integer term_position = posting.get(i).get(j);
                        bufferedWriter.write(term_position + s);
                    }
                }
                bufferedWriter.write("\n");
                bufferedWriterDocId.write("\n");
//                bufferedWriterDocId_simplied.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        new_num_in_block = 0;
        new_block_id++;
        bufferedWriter.close();
        bufferedWriterDocId.close();
        rawIndexBlock.clear();
    }

    /**
     * close the index writer and output all the buffered content if any.
     * fuse all the indexed files together and write the last block to file.
     * @throws IOException
     */
    public void close_index_writer() throws IOException {
        this.write_block_to_disk();
        if (bufferedWriter != null) {
            bufferedWriter.close();
        }
        if (bufferedWriterId != null) {
            bufferedWriterId.close();
        }
        if (bufferedWriterId_reversed != null){
            bufferedWriterId_reversed.close();
        }
        if (bufferedWriterDocId != null){
            bufferedWriterDocId.close();
        }
        if (bufferedWriterTotalTermCount != null){
            bufferedWriterTotalTermCount.close();
        }
        rawIndexBlock.clear();
        this.fuse();
        this.build_dict();
    }


    /**
     * fuse all the indexed files together
     * @throws IOException
     */
    private void fuse() throws IOException {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < new_block_id; i++){
            FilePathGenerator fpg = new FilePathGenerator(".ridx");
            String path = fpg.getPath();
            File file = new File(path + i); // "data//." + ".ridx" + i
            InputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String str = "";
            // str is a string of "term doc_id:term_frequency, term_positions"
            while((str = bufferedReader.readLine()) != null){
                // split by whitespace
                String[] sp = str.split("\\s");
                if (!map.containsKey(sp)){
                    map.put(sp[0], sp[1]);
                } else {
                    map.put(sp[0], map.get(sp[0]) + sp[1]);
                }
            }
            file.delete();
            bufferedReader.close();
        }

        FilePathGenerator fpg = new FilePathGenerator(".ridx");
        String path = fpg.getPath();
        File fidx = new File(path); // "data//" + ".ridx"
        OutputStream indexFileOutputStream = new FileOutputStream(fidx, false);
        BufferedWriter indexBufferedWriter = new BufferedWriter(new OutputStreamWriter(indexFileOutputStream));
        map.forEach((k, v) -> {
            try {
                indexBufferedWriter.write(k + " " + v + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        map.clear();
        indexBufferedWriter.close();
    }

    /**
     * read from ".ridx" file and get all the terms in one file called ".dict"
     * @throws IOException
     */
    private void build_dict() throws IOException{
        FilePathGenerator fpg = new FilePathGenerator(".ridx");
        String path = fpg.getPath();
        File indexFile = new File(path); // "data//" + ".ridx"
        InputStream indexFileInputStream = new FileInputStream(indexFile);
        BufferedReader indexBufferedReader = new BufferedReader(new InputStreamReader(indexFileInputStream));
        FilePathGenerator fpg_dict = new FilePathGenerator(".dict");
        String path_dict = fpg_dict.getPath();
        File dicFile = new File(path_dict); // "data//" + ".dict"
        OutputStream dicFileOutputStream = new FileOutputStream(dicFile, false);
        BufferedWriter dictBufferedWriter = new BufferedWriter(new OutputStreamWriter(dicFileOutputStream));
        String strLine = "";
        int lineNo = 0;
        while ((strLine = indexBufferedReader.readLine()) != null) {
            // str is a string of "term doc_id:term_frequency,term_positions",
            // so split by space will end split the term(or token)
            // from "doc_id:term_frequency,term_positions"
            String[] term = strLine.split("\\s");
            dictBufferedWriter.write(term[0] + "," + lineNo++ + "\n");
        }
        indexBufferedReader.close();
        dictBufferedWriter.close();
    }
}
