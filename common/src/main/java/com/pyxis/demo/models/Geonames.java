package com.pyxis.demo.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the output of http://api.geonames.org/children web service
 *
 * Created by kostya on 2/28/15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Geonames {

    @XmlElement
    private int totalResultsCount;

    @XmlElement(name = "geoname")
    private List<Geoname> geonames = new ArrayList<>();

    public int getTotalResultsCount() {
        return totalResultsCount;
    }

    public void setTotalResultsCount(int totalResultsCount) {
        this.totalResultsCount = totalResultsCount;
    }

    public List<Geoname> getGeonames() {
        return geonames;
    }

    public void setGeonames(List<Geoname> geonames) {
        this.geonames = geonames;
    }
}
