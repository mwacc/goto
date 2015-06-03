package com.pyxis.demo.crawler.impl;

import com.pyxis.demo.crawler.GeoNamesWebService;
import com.pyxis.demo.models.Country;
import com.pyxis.demo.models.CountryWrapper;
import com.pyxis.demo.models.Geoname;
import com.pyxis.demo.models.Geonames;
import org.apache.log4j.Logger;

import javax.xml.bind.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by kostya on 2/28/15.
 */
public class GeoNamesWebServiceImpl implements GeoNamesWebService {
    private final static Logger logger = Logger.getLogger(GeoNamesWebServiceImpl.class);

    private static final String countryEndrpoint = "http://api.geonames.org/countryInfo?username=%s&country=%s";
    private static final String childrenEndpoint = "http://api.geonames.org/children?geonameId=%d&username=%s";

    private Unmarshaller countryMarshaller;
    private Unmarshaller childrenMarshaller;

    private String userId;

    /**
     * Create webservice parser
     * @param userId authorised webservices user
     */
    public GeoNamesWebServiceImpl(String userId) {
        this.userId = userId;
        try {
            countryMarshaller = JAXBContext.newInstance(CountryWrapper.class).createUnmarshaller();
        } catch (JAXBException e) {
            logger.fatal("Can't create unmarshaller for CountryWrapper", e);
            new RuntimeException(e.getMessage());
        }
        try {
            childrenMarshaller = JAXBContext.newInstance(Geonames.class).createUnmarshaller();
        } catch (JAXBException e) {
            logger.fatal("Can't create unmarshaller for CountryWrapper", e);
            new RuntimeException(e.getMessage());

        }
    }

    @Override
    public Country getCountry(String countryCode) {
        CountryWrapper countryWrapper =
                (CountryWrapper) makeCall(String.format(countryEndrpoint, userId, countryCode), countryMarshaller);
        if( countryWrapper != null ) {
            return countryWrapper.getCountry();
        }
        return null;
    }

    @Override
    public List<Geoname> getChildrenGeonames(int geonameId) {
        Geonames geonames =
                (Geonames) makeCall(String.format(childrenEndpoint, geonameId, userId), childrenMarshaller);
        if( geonames != null && geonames.getTotalResultsCount() > 0 ) {
            return geonames.getGeonames();
        }
        return null;
    }

    private Object makeCall(String endpoint, Unmarshaller unmarshaller) {
        Object res = null;
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/xml");

            InputStream is = connection.getInputStream();
            res = unmarshaller.unmarshal(is);

            connection.getResponseCode();
            connection.disconnect();
        } catch(UnmarshalException je) {
            logger.error("Exception parsing answer from geonames service", je);
         } catch(Exception e) {
            logger.error("Exception during making call to geonames service", e);
        }
        return res;
    }

}
