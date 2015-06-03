package com.pyxis.demo.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class wraps response from http://api.geonames.org/countryInfo for particular county
 *
 * Created by kostya on 2/28/15.
 */
@XmlRootElement(name = "geonames")
@XmlAccessorType(XmlAccessType.FIELD)
public class CountryWrapper {

    @XmlElement
    private Country country;

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
