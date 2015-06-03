package com.pyxis.demo.models;

import java.util.List;

/**
 * Created by kostya on 3/8/15.
 * ElasticSearch response to query with suggestions
 */
public class SearchResponseWithSuggestions {
    private List<SearchGeonames> geonames;
    private List<String> suggestions;

    public SearchResponseWithSuggestions(List<SearchGeonames> geonames, List<String> suggestions) {
        this.geonames = geonames;
        this.suggestions = suggestions;
    }

    public List<SearchGeonames> getGeonames() {
        return geonames;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
