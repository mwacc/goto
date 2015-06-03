package com.pyxis.demo.es.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pyxis.demo.es.GeoNamesIndexDAO;
import com.pyxis.demo.models.Geoname;
import com.pyxis.demo.models.Geonames;
import com.pyxis.demo.models.SearchGeonames;
import com.pyxis.demo.models.SearchResponseWithSuggestions;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by kostya on 3/5/15.
 * Gateway to Elasticsearch instance
 */
public class GeoNamesIndexDAOImpl implements GeoNamesIndexDAO {
    private final static Logger logger = Logger.getLogger(GeoNamesIndexDAOImpl.class);

    private final String SUGGESTION_NAME = "by_name_1";

    private Client client;
    private String indexName;
    private Gson gson;  // convert to/from JSON
    public GeoNamesIndexDAOImpl(Properties props) {
        //Create Client
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", props.getProperty("cluster.name", "escluster"))
                .build();
        TransportClient transportClient = new TransportClient(settings);
        transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        client = (Client) transportClient;


        initIndices(props);
    }

    public GeoNamesIndexDAOImpl(Node embedded) {
        client = embedded.client();

        initIndices(new Properties());
    }

    private void initIndices(Properties props) {
        indexName = props.getProperty("index.name", "geonames");

        if( !checkIfIndexExist() ) {
            try {
                createGeonamesIndex();
            } catch (IOException e) {
                logger.error("Error on creating index in ElasticSearch "+indexName, e);
                throw new RuntimeException("Index " + indexName + " can't be created due to exception "+e.getMessage());
            }
        }

        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    // Add new geoname to ES index
    //TODO: perform upload in bulk
    public void addGeoName(Geoname geoname) throws IOException {
        XContentBuilder xb = XContentFactory.jsonBuilder()
                .startObject()
                .field("name", geoname.getName())
                .field("nameSug", geoname.getName())
                .field("geonameId", geoname.getGeonameId())
                .field("countryCode", geoname.getCountryCode())
                .field("fcode", geoname.getFcode())
                .startObject("location")
                    .field("lat", geoname.getLat())
                    .field("lon", geoname.getLng())
                .endObject()
            .endObject();

        client.prepareIndex(indexName, indexName).setSource(xb).execute().actionGet();
    }

    /**
     * Perform fuzzy search in ES based on specified search query
     * Request ES to give suggestions
     * @param searchStr geoname to find
     * @return
     */
    public SearchResponseWithSuggestions search(String searchStr) {
        List<SearchGeonames> geonames = new ArrayList<>(10);
        List<String> suggestions = new ArrayList<>();

        // form request for suggestions
        TermSuggestionBuilder termSuggestionBuilder = new TermSuggestionBuilder(SUGGESTION_NAME);
        termSuggestionBuilder.text(searchStr);
        termSuggestionBuilder.field("nameSug");

        // form search fuzzy request to ES
        SearchResponse searchResponse = client.prepareSearch(indexName).setTypes(indexName)
                .setSearchType(SearchType.QUERY_THEN_FETCH) // can be used DFS_QUERY_THEN_FETCH for more reliability, but in this case performance will be lower
                .setQuery(
                        QueryBuilders.fuzzyQuery("name", searchStr)
                                .fuzziness(Fuzziness.TWO)
                                .prefixLength(0)  // more about conf parameters http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html
                )
                .addSuggestion(termSuggestionBuilder)
                .setSize(10) // query up to 10 elements
                .execute()
                .actionGet();

        // parse answer from ES, if something was found
        SearchHit[] hits = searchResponse.getHits().getHits();
        if(hits.length > 0) {
            for(SearchHit h : hits) {
                SearchGeonames foundGeoname = unmarshall(h.getSourceAsString());
                foundGeoname.setScore(h.getScore());
                foundGeoname.setFcode( foundGeoname.getFcode() + ": " + getFcodeDescription(foundGeoname.getFcode()) );
                geonames.add( foundGeoname );
            }
        }

        // parse suggestions and put them into answer if any suggestions are possible
        Iterator<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> iterator =
                searchResponse.getSuggest().getSuggestion(SUGGESTION_NAME).iterator();
        while ( iterator.hasNext() ) {
            Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry = iterator.next();
            for (Suggest.Suggestion.Entry.Option option : entry.getOptions()) {
                suggestions.add(option.getText().string());
            }

        }


        return new SearchResponseWithSuggestions(geonames, suggestions);
    }

    /**
     * As fuzzy search is used to perform look up, the result might contains either exact matched geonames and similar.
     * The idea is to filter out similar geonames if the exact match is present: it's called 'cleaning'
     * After cleaning the output is sorted if current customer location is known: geonames are sorted by Harvesine distance
     * from current user position
     * @param placeToSearch name of place to search
     * @param lat current user's latitude or null if unknown
     * @param lon current user's longitude or null if unknown
     * @return
     */
    public SearchResponseWithSuggestions searchWithOrdering(String placeToSearch, Double lat, Double lon) {
        SearchResponseWithSuggestions res = cleanUpSearchResponse(placeToSearch, search(placeToSearch) );

        if( lat != null && lon != null ) {
            Collections.sort(res.getGeonames(), new GeoDistanceComparator(lat, lon));
        }

        return res;
    }

    // if exact search was successful, we need to remove all additional (because fuzzy search might return more then we want to have)
    private SearchResponseWithSuggestions cleanUpSearchResponse(String placeToSearch, SearchResponseWithSuggestions resp) {
        List<SearchGeonames> geonames = resp.getGeonames();
        // check if search result contains exact match
        boolean isExactMatch = false;
        for(SearchGeonames g : geonames) {
            if( g.getName().equalsIgnoreCase(placeToSearch) ) {
                isExactMatch = true;
                break;
            }
        }
        // if there is exact match, filter all other
        if( isExactMatch ) {
            Iterator<SearchGeonames> it = geonames.iterator();
            while (it.hasNext()) {
                SearchGeonames geoname = it.next();
                if( !placeToSearch.equalsIgnoreCase(geoname.getName()) ) {
                    it.remove();
                }
            }
        }
        return resp;
    }


    private SearchGeonames unmarshall(String json) {
        return gson.fromJson(json, SearchGeonames.class);
    }

    private boolean checkIfIndexExist() {
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
        ClusterStateResponse response =
                client.admin().cluster().prepareState().execute().actionGet();
        return response.getState().metaData().hasIndex(indexName);
    }

    private void createGeonamesIndex() throws IOException {
        // create index
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
        // add mapping
        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("_id")
                    .field("path", "geonameId") // instead of standard _id field we are going to use geonameId as unique ID of document
                .endObject()
                .startObject("properties")  // reserved ES keyword to describe mapping
                    .startObject("location")
                    .field("type", "geo_point")
                    .field("store", "true")
                    .endObject()

                    .startObject("name")
                    .field("type", "string")
                    .field("analyzer", "standard")
                    .field("store", "true")
                    .endObject()

                    .startObject("nameSug")  // sugestion name
                    .field("type", "string")
                    .field("analyzer", "simple")
                    .field("store", "true")
                    .endObject()


                    .startObject("geonameId")
                    .field("type", "integer")
                    .field("store", "true")
                    .endObject()

                    .startObject("countryCode")
                    .field("type", "string")
                    .field("store", "true")
                    .endObject()

                    .startObject("fcode")
                    .field("type", "string")
                    .field("store", "true")
                    .endObject()

                .endObject()
            .endObject();

        createIndexRequestBuilder.addMapping(indexName, mapping);
        // create index with mapping
        createIndexRequestBuilder.execute().actionGet();
    }

    /**
     * Convert fcode to meaningful description
     * @param code fcode to be converted
     * @return meaningful description
     */
    private String getFcodeDescription(String code) {
        if( code.startsWith("ADM1") ) {
            return "first-order administrative division	";
        } else if( code.startsWith("ADM2") ) {
            return "second-order administrative division	";
        } else if( code.startsWith("ADM3") ) {
            return "third-order administrative division	";
        } else if( code.startsWith("ADM4") ) {
            return "fourth-order administrative division	";
        } else if( code.startsWith("ADM5") ) {
            return "fifth-order administrative division	";
        } else if( code.startsWith("ADMD") ) {
            return "undifferentiated administrative division	";
        } else if( code.startsWith("LTER") ) {
            return "leased area";
        } else if( code.startsWith("PCL") ) {
            return "political entity";
        } else if( code.startsWith("P") ) {
            return "populated place (a city, town, village, or other agglomeration of buildings where people live and work)";
        } else if( code.startsWith("R") ) {
            return "road, railroad";
        } else if( code.startsWith("L") ) {
            return "parks, lakes";
        } else if( code.startsWith("S") ) {
            return "spot, building, farm";
        } else if( code.startsWith("T") ) {
            return "mountain,hill,rock,...";
        } else if( code.startsWith("U") ) {
            return "undersea";
        } else if( code.startsWith("V") ) {
            return "forest,heath,...";
        }
        return "Unclasified";
    }

}
