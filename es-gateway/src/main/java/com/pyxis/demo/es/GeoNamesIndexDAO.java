package com.pyxis.demo.es;

import com.pyxis.demo.models.Geoname;
import com.pyxis.demo.models.SearchResponseWithSuggestions;

import java.io.IOException;

/**
 * Created by kostya on 3/9/15.
 */
public interface GeoNamesIndexDAO {

    /**
     * Add specified geoname to index
     * @param geoname place to index
     * @throws IOException
     */
    public void addGeoName(Geoname geoname) throws IOException;

    /**
     * Perform search over index by name
     * @param searchStr geoname to find
     * @return compex object with a list of relevant search results and list of suggestions for similar items
     */
    public SearchResponseWithSuggestions search(String searchStr);

    /**
     * Perform search over index by name and order result w/ personalization per user or ordering by closest geolocations
     * @param placeToSearch name of place to search
     * @param lat current user's latitude or null if unknown
     * @param lon current user's longitude or null if unknown
     * @return
     */
    public SearchResponseWithSuggestions searchWithOrdering(String placeToSearch, Double lat, Double lon);

}
