package com.pyxis.demo;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.pyxis.demo.crawler.impl.GeoNamesWebServiceImpl;
import com.pyxis.demo.es.impl.GeoNamesIndexDAOImpl;
import com.pyxis.demo.models.Country;
import com.pyxis.demo.models.Geoname;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

/**
 * Created by kostya on 2/27/15.
 */
public class RunSlaveCrawler {

    private final static Logger logger = Logger.getLogger(RunSlaveCrawler.class);

    private BlockingQueue<String> queue = null;
    private GeoNamesWebServiceImpl geoNamesWebService;
    private GeoNamesIndexDAOImpl indexDAO;

    public RunSlaveCrawler(Properties props) {
        ClientConfig clientConfig = new ClientConfig();
        final HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        queue = client.getQueue(Constants.COUNTRIES_QUEUE);

        geoNamesWebService = new GeoNamesWebServiceImpl(props.getProperty("geonames.account", "demo"));
        indexDAO = new GeoNamesIndexDAOImpl(props);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                client.shutdown();
            }
        } ));

    }

    public static void main(String[] args) throws IOException {
        if( args.length != 1 ) {
            System.err.println("Usage:  <path to config file>");
            System.exit(-1);
        }
        InputStream input = new FileInputStream(args[0]);

        Properties props = new Properties();
        // load a properties file
        props.load(input);


        new RunSlaveCrawler(props).run();

    }

    public void run() {
        while ( queue.size() > 0 ) {
            String countryCode = queue.poll();
            logger.info("Start indexing "+countryCode);
            Country country = geoNamesWebService.getCountry(countryCode);
            proceedGeoNames(country.getGeonameId(), 0);
        }

        System.out.println("Slave was gracefully switched off");
        System.exit(0);
    }

    private void proceedGeoNames(int geoNameId, @Deprecated int level) {
        List<Geoname> geonames = geoNamesWebService.getChildrenGeonames(geoNameId);
        if( geonames != null ) {
            for(Geoname geoname : geonames) {
                try {
                    indexDAO.addGeoName(geoname);
                    logger.info(String.format("Indexed %s %s", geoname.getName(), geoname.getCountryCode()));
                } catch (IOException e) {
                    logger.error( String.format("Can't index %s, %s due to exception %s",
                            geoname.getName(), geoname.getCountryCode(), e.getMessage()), e );
                }
                proceedGeoNames(geoname.getGeonameId(), level + 2);
            }
        }
    }

    // debug function
    @Deprecated
    private void printSpaces(int count) {
        for(int i = 0; i < count; i++) {
            System.out.print(' ');
        }
    }


}
