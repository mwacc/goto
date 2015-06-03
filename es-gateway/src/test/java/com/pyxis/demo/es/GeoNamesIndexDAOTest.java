package com.pyxis.demo.es;

import com.pyxis.demo.es.impl.GeoNamesIndexDAOImpl;
import com.pyxis.demo.models.Geoname;
import com.pyxis.demo.models.SearchResponseWithSuggestions;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by kostya on 3/7/15.
 */
public class GeoNamesIndexDAOTest {

    private Node node;
    private final String  tmpDataDirectory = "~/.tmpes";
    private GeoNamesIndexDAO indexDAO;

    @BeforeClass
    public void setUp() {
        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings
                .settingsBuilder()
                .put("http.enabled", "false")
                .put("path.data", tmpDataDirectory)
                .put("node.name", "integrationTest")
                .put("discovery.zen.ping.multicast.enabled", "false")
                .put("index.store.type", "memory")
                .put("index.store.fs.memory.enabled", "true")
                .put("index.gateway.type", "none")
                .put("gateway.type", "none")
                .put("index.number_of_shards", "1")
                .put("index.number_of_replicas", "0");

        node = NodeBuilder.nodeBuilder()
                .settings(elasticsearchSettings.build()).node();

        indexDAO = new GeoNamesIndexDAOImpl(node);
        try {
            addGeoNames();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public void tearDown() {
        try {
            node.stop();
        } finally {
            deleteDataDirectory();
        }
    }

    public void addGeoNames() throws Exception {
        Geoname g = new Geoname();

        g.setName("London"); g.setGeonameId(1); g.setCountryCode("UK"); g.setLat(1.1); g.setLng(2.4); g.setFcode("ADF");
        indexDAO.addGeoName(g);

        g.setName("London"); g.setGeonameId(1); g.setCountryCode("CA"); g.setLat(41.1); g.setLng(122.4); g.setFcode("ABF");
        indexDAO.addGeoName(g);

        g.setName("Paris"); g.setGeonameId(1); g.setCountryCode("FR"); g.setLat(2.1); g.setLng(2.4); g.setFcode("ADF");
        indexDAO.addGeoName(g);

        g.setName("Parije"); g.setGeonameId(1); g.setCountryCode("FR"); g.setLat(9.1); g.setLng(7.4); g.setFcode("ASF");
        indexDAO.addGeoName(g);

        g.setName("Berlin"); g.setGeonameId(1); g.setCountryCode("GE"); g.setLat(1.1); g.setLng(3.4); g.setFcode("ADF");
        indexDAO.addGeoName(g);

        // some time is required to complete indexing
        Thread.sleep(2000);
    }

    @Test
    public void testSimpleSearch() throws Exception {
        SearchResponseWithSuggestions searchResult = indexDAO.search("Berlin");

        Assert.assertEquals( searchResult.getGeonames().size(), 1 );
    }

    @Test
    public void testFuzzySearch() throws Exception {
        SearchResponseWithSuggestions searchResult = indexDAO.search("Parij");

        Assert.assertEquals( searchResult.getGeonames().size(), 2 );
    }

    @Test
    public void testmultipleEqualSearch() throws Exception {
        SearchResponseWithSuggestions searchResult = indexDAO.search("London");

        Assert.assertEquals( searchResult.getGeonames().size(), 2 );
    }

    @Test
    public void testSuggestionSearch() throws Exception {
        SearchResponseWithSuggestions searchResult = indexDAO.search("Pacos");

        Assert.assertEquals( searchResult.getSuggestions().size(), 1 );
        Assert.assertEquals( searchResult.getSuggestions().get(0).toLowerCase(), "Paris".toLowerCase() );
    }


    private void deleteDataDirectory() {
        try {
            FileUtils.deleteDirectory(new File(tmpDataDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
