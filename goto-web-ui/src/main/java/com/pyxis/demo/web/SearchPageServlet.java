package com.pyxis.demo.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by kostya on 3/1/15.
 *
 * This servlet display main page of the application
 */
@WebServlet(urlPatterns = "/")
public class SearchPageServlet extends HttpServlet {

    private final String pathToHtml = "/web-static/index.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        String page = readFile(pathToHtml);

        PrintWriter out = resp.getWriter();
        out.println(page);
        out.close();
    }

    private String readFile( String file ) throws IOException {
        InputStream in = getClass().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }

}
