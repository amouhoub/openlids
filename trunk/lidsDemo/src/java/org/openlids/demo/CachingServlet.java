/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 *
 * @author ssp
 */
public class CachingServlet extends HttpServlet {
    final ArrayList<String> keywords = new ArrayList<String>();
    final ArrayList<String> entities = new ArrayList<String>();


    @Override
     public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            InputStream in = this.getServletContext().getResourceAsStream("/data/cache.n3");
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            try {
                Iterator<Node[]> parser;
                parser = new NxParser(in, new Callback() {

                    public void startDocument() {
                    }

                    public void endDocument() {
                    }

                    public void processStatement(Node[] nodes) {
                        keywords.add(nodes[2].toString());
                        entities.add(nodes[0].toString());
                    }

                });
            } catch (ParseException pe) {
                throw new IOException(pe.toString());
            }

            String line;
            while ((line = r.readLine()) != null) {
                String parts[] = line.split("\",<");

            }
        } catch(IOException ioe) {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE,"Could not load cache.txt");
        }
    }
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        try {
           String search = req.getParameter("search");
            HashSet<String> uris_sent = new HashSet<String>();
            for(int i=0;i<keywords.size();i++) {
                if(keywords.get(i).contains(search) || entities.get(i).contains(search)) {
                    if(!uris_sent.contains(entities.get(i))) {
                        uris_sent.add(entities.get(i));
                        out.println(entities.get(i));
                    }
                }
            }
        } finally { 
            out.close();
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
