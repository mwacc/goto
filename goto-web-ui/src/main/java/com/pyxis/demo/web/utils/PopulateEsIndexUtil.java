package com.pyxis.demo.web.utils;

import com.pyxis.demo.es.impl.GeoNamesIndexDAOImpl;
import com.pyxis.demo.models.Geoname;

import java.io.*;
import java.util.Properties;

/**
 * Created by kostya on 3/5/15.
 */
public class PopulateEsIndexUtil {

    private GeoNamesIndexDAOImpl indexDAO;

    public PopulateEsIndexUtil(Properties p) {
        indexDAO = new GeoNamesIndexDAOImpl(p);
    }

    public static void main(String[] args) throws Exception {
        if( args.length != 2 ) {
            System.err.println("Usage:  <path to ES config file> <path to TSV file with data>");
            System.exit(-1);
        }
        InputStream input = new FileInputStream(args[0]);

        Properties props = new Properties();
        // load a properties file
        props.load(input);

        new PopulateEsIndexUtil(props).run(args[1]);
    }

    public void run(String fileName) {
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for(String line; (line = br.readLine()) != null; ) {
                indexDAO.addGeoName( getGeonameFromTSV(line.split("\t")) );
            }
            // line is not visible here.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Geoname getGeonameFromTSV(String[] arr) {
        Geoname g = new Geoname();
        g.setGeonameId( Integer.valueOf(arr[0]) );
        g.setName( arr[1] );
        g.setCountryCode( arr[2] );
        g.setLat( Double.valueOf(arr[3]) );
        g.setLng(Double.valueOf(arr[4]));
        g.setFcode( arr[5] );
        return g;
    }

}
