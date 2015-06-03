package com.pyxis.demo.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Presents one typical geoname available in the system
 *
 * Created by kostya on 2/28/15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Geoname {

    @XmlElement
    private int geonameId;
    @XmlElement
    private String name;
    @XmlElement
    private double lat;
    @XmlElement
    private double lng;
    @XmlElement
    private String countryCode;
    @XmlElement
    private String fcode; // http://www.geonames.org/export/codes.html

    public int getGeonameId() {
        return geonameId;
    }

    public void setGeonameId(int geonameId) {
        this.geonameId = geonameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFcode() {
        return fcode;
    }

    public void setFcode(String fcode) {
        this.fcode = fcode;
    }

    public String getFcl() {
        return fcode.substring(0,1);
    }


    @Override
    public String toString() {
        return "Geoname{" +
                "geonameId=" + geonameId +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", countryCode='" + countryCode + '\'' +
                ", fcl='" + getFcl() + '\'' +
                ", fcode='" + fcode + '\'' +
                '}';
    }
}
