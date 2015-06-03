package com.pyxis.demo.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pyxis.demo.es.GeoNamesIndexDAO;
import com.pyxis.demo.es.impl.GeoNamesIndexDAOImpl;
import com.pyxis.demo.models.SearchResponseWithSuggestions;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by kostya on 3/1/15.
 *
 * This servlet provides simple REST API to perform search in available Geo Index
 */
@WebServlet(urlPatterns = "/search")
public class SearchPlacesServlet extends HttpServlet {
    private final static Logger logger = Logger.getLogger(SearchPlacesServlet.class);

    private static GeoNamesIndexDAO indexDAO;
    private Gson gson;

    public static void setIndexDAO(GeoNamesIndexDAO i) {
        indexDAO = i;
    }

    public SearchPlacesServlet() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String placeToSearch = req.getParameter("q");
        String latStr = req.getParameter("lat");
        String lonStr = req.getParameter("lon");

        if( placeToSearch != null && !"".equals(placeToSearch.trim()) ){
            Double lat = null;
            Double lon = null;

            try {
                lat = Double.valueOf(latStr);
                lon = Double.valueOf(lonStr);
            }catch (NumberFormatException | NullPointerException nfe) {
                logger.warn( String.format("Error parsing lat/lon (%s/%s), not geo sorting would be used", latStr, lonStr));
            }

            SearchResponseWithSuggestions searchResult = indexDAO.searchWithOrdering(placeToSearch.trim(), lat, lon);

            String jsonResp = gson.toJson(searchResult);
            try ( OutputStream responseOutputStream = resp.getOutputStream() ) {
                resp.setContentLength( jsonResp.getBytes().length );
                responseOutputStream.write( jsonResp.getBytes() );
            }
        }

    }


}
