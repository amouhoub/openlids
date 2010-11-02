/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.dbpwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import javax.cache.Cache;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ssp
 */
public class DBPediaHPSearch extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String homepage = request.getParameter("homepage");

        if (homepage == null) {
            response.sendError(400, "please supply homepage parameter.");
            return;
        }

        ServletContext ctx = getServletContext();
        Cache cache = (Cache) ctx.getAttribute(Listener.CACHE);



        try {
            URL url = new URL("http://dbpedia.org/sparql");

            // URL url = new URL("http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&should-sponge=&query=CONSTRUCT+{+%3Fs+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfStudents%3E+%3Fn+}+where+{+%3Fs+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2Fhomepage%3E+%3C" + URLEncoder.encode(homepage, "utf-8") + "%3Fs+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfStudents%3E+%3Fn+}&format=application%2Frdf%2Bxml&debug=on&timeout=");
            // URL url = new URL("http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&should-sponge=&query=SELECT+%3Fn+where+{+%3Fs+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2Fhomepage%3E+%3C" + URLEncoder.encode(homepage, "utf-8") + "%3E+.+%3Fs+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfStudents%3E+%3Fn+}&format=text%2Fhtml&debug=on&timeout=");

            String result = null;

            if (cache != null) {
                if (cache.containsKey(homepage)) {
                    result = (String) cache.get(homepage);
                }
            }

            if (result == null) {

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                String postq = URLEncoder.encode("default-graph-uri", "utf-8") + "=" + URLEncoder.encode("http://dbpedia.org", "utf-8") + "&"
                        + URLEncoder.encode("should-sponge", "utf-8") + "=" + "&"
                        + URLEncoder.encode("query", "utf-8") + "=" + URLEncoder.encode("CONSTRUCT { <http://localhost:8887/DBPediaHPSearch?homepage=" + URLEncoder.encode(homepage, "utf-8") + "#entity> owl:sameAs ?entity } where { ?entity <http://xmlns.com/foaf/0.1/homepage> <" + homepage + "> . }", "utf-8") + "&"
                        + URLEncoder.encode("format", "utf-8") + "=" + URLEncoder.encode("application/rdf+xml", "utf-8");
                System.out.println(homepage);
                System.out.println(postq);
                wr.write(postq);
                wr.flush();

                InputStream is = conn.getInputStream();

                String encoding = conn.getContentEncoding();
                if (encoding == null) {
                    encoding = "ISO_8859-1";
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));
                    String l;
                    StringBuilder sb = new StringBuilder();

                    String maxMatch = "";
                    while ((l = in.readLine()) != null) {
                        sb.append(l);
                    }
                    in.close();
                    result = sb.toString();
                    result = result.replace(homepage, URLEncoder.encode(homepage, "utf-8"));



                    if (cache != null) {
                        cache.put(homepage, result);
                    }

                } else {
                    response.sendError(400, "DBpedia SPARQL Endpoint returned Error " + conn.getResponseCode() + "\n" + conn.getResponseMessage());
                    return;
                    // Server returned HTTP error code.
                }
            }

            response.setContentType("application/rdf+xml");
            PrintWriter out = response.getWriter();

          //  response.setHeader("Cache-Control", "public");
           // Calendar cal = Calendar.getInstance();
           // cal.add(Calendar.DATE, 1);
           // response.setHeader("Expires", RFC822.format(cal.getTime()));

            out.print(result);


        } catch (MalformedURLException e) {
            this.log("MalformedURLException " + e);
            // ...
        } catch (IOException e) {
            // ...
            this.log("IOException " + e);
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
