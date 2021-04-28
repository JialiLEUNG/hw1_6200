package com.example.cs6200_hw2;


import com.example.cs6200_hw1.Indexer.CorpusReader;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.*;

import java.util.*;
import java.util.stream.Collectors;

public class Hw2Main {
    private static final String INDEX = "my_index";
    private static RestHighLevelClient client;
    private static MedicineServiceImpl medicineService;
    private String query_text;
    private int topK; // for retrieving the topK relevant doc

    public Hw2Main(String query_text, int topK) {
        this.query_text = query_text;
        this.topK = topK;
    }

    private static final NodeSelector INGEST_NODE_SELECTOR = nodes -> {
        final Iterator<Node> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            // roles may be null if we don't know, thus we keep the node in then...
            if (node.getRoles() != null && node.getRoles().isIngest() == false) {
                iterator.remove();
            }
        }
    };

    public static void startElasticsearchCreateLocalClient() {

//        container.start();
        HttpHost host = new HttpHost("localhost",
               9200, "http");
        final RestClientBuilder builder = RestClient.builder(host);
        builder.setNodeSelector(INGEST_NODE_SELECTOR);
        client = new RestHighLevelClient(builder);
        medicineService = new MedicineServiceImpl(INDEX, client);
    }

    public static void closeResources() throws Exception {
        client.close();
    }

    public void deleteMedicineIndex() throws Exception {
        try {
            client.indices()
                    .delete(new DeleteIndexRequest(INDEX),
                            RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException e) {
            e.printStackTrace();
        }
    }

    public List<String> run() throws Exception {
        startElasticsearchCreateLocalClient();
        deleteMedicineIndex();

        // sample query_text:
        // "What are the effects of calcium on the physical properties of mucus from CF patients?"
        List<Medicine> medicines = WriteIndex();

        // Indexing is the process of adding data to Elasticsearch.
        // This is because when you feed data into Elasticsearch,
        // the data is placed into Apache Lucene indexes.
        // Elasticsearch uses the Lucene indexes to store and retrieve its data.
        medicineService.save(medicines);

        client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);

        // matches all products
        final Page<Medicine> page = medicineService.search(query_text);
        final Page<Medicine> secondPage = medicineService.next(page);
        // for every page, save 10 docs
        List<String> firstPageIds = page.get().stream().map(Medicine::getDocId).collect(Collectors.toList());
        List<String> secondPageIds = secondPage.get().stream().map(Medicine::getDocId).collect(Collectors.toList());

        List<String> top20docs = new ArrayList<>();
        top20docs.addAll(firstPageIds);
        top20docs.addAll(secondPageIds);
        closeResources();
        return top20docs;
    }

    /**
     * (1) indexing the doc, write the indexes to docs.
     * (2) turn each doc into Medicine object, and put them into an arraylist
     * so that it could be used as a Json object (serialization/deserialization) later on
     * @throws Exception
     */
    public List<Medicine> WriteIndex() throws Exception {
        // initiate corpus, which is a preprocessed collection of file reader
        CorpusReader corpus = new CorpusReader();

        // Map to hold doc_id and doc content;
        Map<String, String> doc;

        // turn each doc into json object, and put them in to a list
        List<Medicine> medList = new ArrayList<>();

        // index the corpus, load the doc one by one
        while ((doc = corpus.NextDoc()) != null){
            // get the doc_id and content of the current doc.
            String doc_id = doc.get("DOC_ID");
            String content = doc.get("CONTENT");

            // create a medicine json object for the doc
            Medicine med = new Medicine();
            med.setDocId(doc_id);
            med.setDescription(content);

            // put it into the list
            medList.add(med);
        }
        return medList;
    }
}
