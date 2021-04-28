package com.example.cs6200_hw2;

import com.example.cs6200_hw1.Hw1Part2Main;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class H2Main {

    private String query_text = "What are the effects of calcium on the physical properties of mucus from CF patients?";
    private int topK=20; // for retrieving the topK relevant doc

    private static final ElasticsearchContainer container =
            new ElasticsearchContainer(
                    "docker.elastic.co/elasticsearch/elasticsearch:7.11.1")
                    .withExposedPorts(9200);

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

    private static final String INDEX = "my_index";
    private static RestHighLevelClient client;
    private static MedicineServiceImpl medicineService;
    private static final ObjectMapper mapper = new ObjectMapper();



    public H2Main() {
//        this.query_text = query_text;
//        this.topK = topK;
    }

    /**
     * Instantiates a high level client,
     * which automatically also instantiates a low level client.
     * The Apache HTTP client is not fully abstracted away,
     * as classes like HttpHost are from that dependency.
     */
    @BeforeAll
    public static void startElasticsearchCreateLocalClient() {
        container.start();
        HttpHost host = new HttpHost("localhost",
                container.getMappedPort(9200));
        final RestClientBuilder builder = RestClient.builder(host);
        builder.setNodeSelector(INGEST_NODE_SELECTOR);
        client = new RestHighLevelClient(builder);
        medicineService = new MedicineServiceImpl(INDEX, client);
    }

//    @AfterAll
    public static void closeResources() throws Exception {
        client.close();
    }

    @BeforeEach
    public void deleteMedicineIndex() throws Exception {
        try {
            client.indices()
                    .delete(new DeleteIndexRequest(INDEX),
                            RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void run() throws Exception{


        Hw1Part2Main assign1 = new Hw1Part2Main(query_text, topK);
        String[] xmlFileDir = assign1.getXMLdirectory();
        for (String xml : xmlFileDir){
            assign1.docDataPreProcess(xml);
        }

        // sample query_text:
        // "What are the effects of calcium on the physical properties of mucus from CF patients?"
        List<Medicine> medicines = assign1.WriteIndex();
        assign1.queryDataPreProcess(query_text);
        Set<String> query_tokens = assign1.getQuery_tokens();

        indexMedicineWithoutId(medicines);

        checkClusterVersion();

//        PaginationFirst20Entries(String.join(",", query_tokens));

        closeResources();

    }

    public void indexMedicineWithoutId(List<Medicine> medicines) throws Exception {
        medicineService.save(medicines);
    }

    public void checkClusterVersion() throws Exception {

        final ClusterHealthResponse response = client.cluster().health(
                new ClusterHealthRequest(), RequestOptions.DEFAULT);
        // check for yellow or green cluster health

        if (response.getStatus() == ClusterHealthStatus.RED){
            System.out.println("cluster is RED");
        }

        // async party
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<ClusterHealthResponse> reference = new AtomicReference<>();
//        final ActionListener<ClusterHealthResponse> listener = ActionListener.wrap(
//                r -> { reference.set(r); latch.countDown();},
//                e -> { e.printStackTrace(); latch.countDown();});
//        client.cluster().healthAsync(new ClusterHealthRequest(), RequestOptions.DEFAULT, listener);
//        latch.await(10, TimeUnit.SECONDS);
//        assertThat(reference.get().getStatus()).isNotEqualTo(ClusterHealthStatus.RED);
    }

//    public void PaginationFirst20Entries(String query_string) throws Exception {
//        medicineService.save(medicines);
//        client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);
//
//        // matches all products
//        final Page<Medicine> page = medicineService.search(query_string);
////        assertThat(page.get()).hasSize(10);
//        final Page<Medicine> secondPage = medicineService.next(page);
////        assertThat(page.get()).hasSize(10);
//        List<String> firstPageIds = page.get().stream().map(Medicine::getDocId).collect(Collectors.toList());
//        for (String i: firstPageIds) {
//            System.out.println("docId - 1st Page: " + i);
//        }
//        List<String> secondPageIds = secondPage.get().stream().map(Medicine::getDocId).collect(Collectors.toList());
//        for (String i: secondPageIds) {
//            System.out.println("docId - 2nd Page: " + i);
//        }
////        assertThat(firstPageIds).isNotEqualTo(secondPageIds);
//
//    }
}
