package com.example.cs6200_hw2;

import com.example.cs6200_hw1.Hw1Part2Main;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticsearchTest {

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

    @AfterAll
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

    /**
     * Every call requires you to supply a RequestOptions object.
     * You can use RequestOptions.DEFAULT or you can use a custom what,
     * that has special headers set
     * (for example a different authorization on a per request base,
     * when you have a tenant based app).
     */
    @Test
    public void testClusterVersion() throws Exception {
        final ClusterHealthResponse response = client.cluster().health(
                new ClusterHealthRequest(), RequestOptions.DEFAULT);
        // check for yellow or green cluster health
        assertThat(response.getStatus()).isNotEqualTo(ClusterHealthStatus.RED);

        // async party
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ClusterHealthResponse> reference = new AtomicReference<>();
        final ActionListener<ClusterHealthResponse> listener = ActionListener.wrap(
                r -> { reference.set(r); latch.countDown();},
                e -> { e.printStackTrace(); latch.countDown();});
        client.cluster().healthAsync(new ClusterHealthRequest(), RequestOptions.DEFAULT, listener);
        latch.await(10, TimeUnit.SECONDS);
        assertThat(reference.get().getStatus()).isNotEqualTo(ClusterHealthStatus.RED);
    }

    @Test
    public void indexMedicineWithoutId() throws Exception {
        Medicine medicine = createMedicines(1).get(0);
        medicine.setId(null);
        assertThat(medicine.getId()).isNull();

        medicineService.save(medicine);

        assertThat(medicine.getId()).isNotNull();
    }

    @Test
    public void indexMedicineWithId() throws Exception {
        Medicine medicine = createMedicines(1).get(0);
        assertThat(medicine.getId()).isEqualTo("0");

        medicineService.save(medicine);

        medicine = medicineService.findById("0");
        assertThat(medicine.getId()).isEqualTo("0");
    }

    @Test
    public void testFindMedicineById() throws Exception {
        medicineService.save(createMedicines(3));

        for (int i = 0; i < 3; i++){
            final Medicine medicine1 = medicineService.findById(String.valueOf(i));
            assertThat(medicine1.getId()).isEqualTo(String.valueOf(i));
        }
    }

    @Test
    public void testSearch() throws Exception {
        medicineService.save(createMedicines(10));
        client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);

        final Page<Medicine> page = medicineService.search("9");
        assertThat(page.get()).hasSize(1);
        assertThat(page.get()).first().extracting("id").isEqualTo("9");
    }


    @Test
    public void testPagination() throws Exception {
        Hw1Part2Main assign1 = new Hw1Part2Main("What are the effects of calcium on the physical properties of mucus from CF patients?", 20);
//        String[] xmlFileDir = assign1.getXMLdirectory();
//        for (String xml : xmlFileDir) {
//            assign1.docDataPreProcess(xml);
//        }

        // sample query_text:
        // "What are the effects of calcium on the physical properties of mucus from CF patients?"
        List<Medicine> medicines = assign1.WriteIndex();
//        assign1.queryDataPreProcess("What are the effects of calcium on the physical properties of mucus from CF patients?");
//        Set<String> query_tokens = assign1.getQuery_tokens();

        medicineService.save(medicines);

        client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);

//        String query_str = String.join(" ", query_tokens);
        // matches all products
        final Page<Medicine> page = medicineService.search("What are the effects of calcium on the physical properties of mucus from CF patients?");
//        assertThat(page.get()).hasSize(10);
        final Page<Medicine> secondPage = medicineService.next(page);
//        assertThat(page.get()).hasSize(10);
        List<String> firstPageIds = page.get().stream().map(Medicine::getDocId).collect(Collectors.toList());
        for (String i : firstPageIds) {
            System.out.println("docId - 1st Page: " + i);
        }
        List<String> secondPageIds = secondPage.get().stream().map(Medicine::getDocId).collect(Collectors.toList());
        for (String i : secondPageIds) {
            System.out.println("docId - 2nd Page: " + i);
        }
    }

    /**
     * When we have a typed searchable JSON document indexed,
     * we can proceed and search using the search() method.
     * The results returned by the search() method is called Hits.
     * Each Hit refers to a JSON document matching a search request.
     * @throws Exception
     */
    @Test
    public void testSearchAfter() throws Exception {
        medicineService.save(createMedicines(21));
        client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);

        SearchRequest searchRequest = new SearchRequest(INDEX);
        searchRequest.source().query(QueryBuilders.matchQuery("description", "medicine"));
//        searchRequest.source().sort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        final List<String> ids = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getId).collect(Collectors.toList());

        // create a new search request
        SearchRequest searchAfterRequest = new SearchRequest(INDEX);
        searchAfterRequest.source()
                .query(QueryBuilders.matchQuery("description", "medicine"));
//                .sort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        SearchHit lastHit = response.getHits().getHits()[response.getHits().getHits().length-1];
        searchAfterRequest.source().searchAfter(lastHit.getSortValues());
        final SearchResponse searchAfterResponse = client.search(searchAfterRequest,
                RequestOptions.DEFAULT);
        final List<String> searchAfterIds = Arrays.stream(searchAfterResponse.getHits().getHits())
                .map(SearchHit::getId).collect(Collectors.toList());

        assertThat(ids).isNotEqualTo(searchAfterIds);
    }

    @Test
    public void testBulkProcessor() throws Exception {
        final Map<Long, String> bulkMap = new HashMap<>();

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                bulkMap.put(executionId, "BEFORE");
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                bulkMap.put(executionId, "AFTER");
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                bulkMap.put(executionId, "EXCEPTION");
            }
        };

        try (BulkProcessor bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener)
                .setConcurrentRequests(0)
                .setBulkActions(10)
                // extra long to see if it has been applied
                .setFlushInterval(TimeValue.timeValueDays(1))
                .build()) {

            final List<Medicine> medicines = createMedicines(19);
            for (Medicine medicine : medicines) {
                bulkProcessor.add(indexRequest(medicine));
            }

            client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);

            // nine elements should still be in the bulk processor
            CountResponse countResponse = client.count(new CountRequest(INDEX), RequestOptions.DEFAULT);
            assertThat(countResponse.getCount()).isEqualTo(10);

            // lets flush out the remaining elements manually
            bulkProcessor.flush();

            client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);
            countResponse = client.count(new CountRequest(INDEX), RequestOptions.DEFAULT);
            assertThat(countResponse.getCount()).isEqualTo(19);

            assertThat(bulkMap).hasSize(2);
            assertThat(bulkMap).containsValues("AFTER");
            assertThat(bulkMap).doesNotContainValue("BEFORE");
            assertThat(bulkMap).doesNotContainValue("EXCEPTION");
        }

    }

    private IndexRequest indexRequest(Medicine medicine) throws IOException {
        final byte[] bytes = mapper.writeValueAsBytes(medicine);
        final IndexRequest request = new IndexRequest(INDEX);
        if (medicine.getId() != null) {
            request.id(medicine.getId());
        }
        request.source(bytes, XContentType.JSON);
        return request;
    }


    @Test
    public void testQueryBuilders() throws Exception {
//        Medicine product1 = new Medicine();
//        product1.setId("book-world-records-2020");
//        product1.setDescription("The book of the year!");
//
//        Medicine product2 = new Medicine();
//        product2.setId("book-world-records-2010");
//        product2.setDescription("The book of the year!");
//
//        Medicine product3 = new Medicine();
//        product3.setId("book-world-records-1890");
//        product3.setDescription("The book of the year!");
//
//        medicineService.save(Arrays.asList(product1, product2, product3));
//        client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);

        Hw1Part2Main assign1 = new Hw1Part2Main("What are the effects of calcium on the physical properties of mucus from CF patients?", 20);
        String[] xmlFileDir = assign1.getXMLdirectory();
        for (String xml : xmlFileDir) {
            assign1.docDataPreProcess(xml);
        }

        // sample query_text:
        // "What are the effects of calcium on the physical properties of mucus from CF patients?"
        List<Medicine> medicines = assign1.WriteIndex();
        assign1.queryDataPreProcess("What are the effects of calcium on the physical properties of mucus from CF patients?");
        Set<String> query_tokens = assign1.getQuery_tokens();

        medicineService.save(medicines);

        client.indices().refresh(new RefreshRequest(INDEX), RequestOptions.DEFAULT);

        String query_str = String.join(" ", query_tokens);

        final BoolQueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.multiMatchQuery(query_str));
//                .should(QueryBuilders.rangeQuery("price").lt(100))
//                .filter(QueryBuilders.rangeQuery("stock_available").gt(0))
//                .filter(QueryBuilders.rangeQuery("price").gt(0));

        SearchRequest searchRequest = new SearchRequest(INDEX);
        searchRequest.source().query(qb);
        final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        final SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits){
            System.out.println("doc_id in hit: " + hit.getId());
        }
        // exact hit count
//        assertThat(response.getHits().getTotalHits().value).isEqualTo(2);
//        assertThat(response.getHits().getTotalHits().relation).isEqualTo(TotalHits.Relation.EQUAL_TO);

        // first hit should be 2010 edition due to its price and the above should clause
//        final SearchHit[] hits = response.getHits().getHits();
//        assertThat(hits[0].getId()).isEqualTo("book-world-records-2010");
//        assertThat(hits[1].getId()).isEqualTo("book-world-records-2020");
    }















}
