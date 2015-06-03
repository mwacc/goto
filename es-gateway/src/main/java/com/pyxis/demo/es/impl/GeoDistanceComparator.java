package com.pyxis.demo.es.impl;

import com.pyxis.demo.models.SearchGeonames;

import javax.xml.crypto.dsig.TransformException;
import java.util.Comparator;
import static java.lang.Math.*;

/**
 * Created by kostya on 3/9/15.
 */
public class GeoDistanceComparator implements Comparator<SearchGeonames> {

    private double lat;
    private double lon;

    public GeoDistanceComparator(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public int compare(SearchGeonames o1, SearchGeonames o2) {
        double lat1 = o1.getLocation().getLat();
        double lon1 = o1.getLocation().getLon();

        double lat2 = o2.getLocation().getLat();
        double lon2 = o2.getLocation().getLon();


        try {
            Double distanceTo1 = calculateDistance(lat, lon, lat1, lon1);
            Double distanceTo2 = calculateDistance(lat, lon, lat2, lon2);

            return distanceTo1.compareTo( distanceTo2 );
        } catch (TransformException e) {
            return 0;
        }
    }

    /**
     * The Haversine distance is great-circle distances between two points on a sphere from their longitudes and latitudes. I
     * Read more http://en.wikipedia.org/wiki/Haversine_formula
     */
    private static strictfp Double calculateDistance(double firstLat, double firstLon, double secondLat, double secondLon)
            throws TransformException {

        double earthRadius = 6371; // kilometers
        double latitudeDelta = toRadians(secondLat - firstLat);
        double longitudeDelta = toRadians(secondLon - firstLon);
        double a = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
                cos(toRadians(firstLat)) * cos(toRadians(secondLat)) * sin(longitudeDelta / 2) * sin(longitudeDelta / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        return earthRadius * c;
    }
}
