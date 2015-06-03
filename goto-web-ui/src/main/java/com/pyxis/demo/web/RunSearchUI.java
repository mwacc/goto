package com.pyxis.demo.web;

import com.pyxis.demo.es.impl.GeoNamesIndexDAOImpl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by kostya on 3/1/15.
 */
public class RunSearchUI {

    private GeoNamesIndexDAOImpl indexDAO;

    public RunSearchUI(Properties props) {
        indexDAO = new GeoNamesIndexDAOImpl(props);
    }

    public static void main(String[] args) throws Exception {
        if( args.length != 1 ) {
            System.err.println("Usage:  <path to config file>");
            System.exit(-1);
        }
        InputStream input = new FileInputStream(args[0]);

        Properties props = new Properties();
        // load a properties file
        props.load(input);

        new RunSearchUI(props).runEmbeddedHttpServer();
    }

    private void runEmbeddedHttpServer() throws Exception {
        Server httpServer = new Server(8080);
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setBaseResource(Resource.newClassPathResource("."));
        webAppContext.addServlet(SearchPageServlet.class.getName(), "/");
        webAppContext.addServlet(SearchPlacesServlet.class.getName(), "/search");
        webAppContext.setConfigurations(new Configuration[]{new WebXmlConfiguration()});

        SearchPlacesServlet.setIndexDAO(indexDAO);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{webAppContext, new DefaultHandler()});
        httpServer.setHandler(handlers);
        httpServer.start();
    }

}
