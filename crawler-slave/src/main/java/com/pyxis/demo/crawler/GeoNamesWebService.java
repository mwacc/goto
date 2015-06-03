package com.pyxis.demo.crawler;

import com.pyxis.demo.models.Country;
import com.pyxis.demo.models.Geoname;

import java.util.List;

/**
 * Created by kostya on 2/28/15.
 */
public interface GeoNamesWebService {

    /**
     * Get information about country
     * @param countryCode country code of interest
     * @return available information about country
     */
    public Country getCountry(String countryCode);

    /**
     * Return information about all known children geonames
     * @param geonameId geonameid of interest
     * @return information about all known children geoname for specified ID
     */
    public List<Geoname> getChildrenGeonames(int geonameId);

}
