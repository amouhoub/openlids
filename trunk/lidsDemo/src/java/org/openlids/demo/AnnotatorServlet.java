/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openlids.linking.Annotator;
import org.openlids.model.LIDSDescription;
import org.openlids.model.data.DataSet;
import org.openlids.model.data.impl.DataSetNx;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

/**
 *
 * @author ssp
 */
public class AnnotatorServlet extends HttpServlet {
   
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
            URI to_annot;
            try {
                to_annot = new URI(req.getParameter("uri"));
            } catch (URISyntaxException ex) {
                resp.sendError(500, ex.toString());
                return;
            }
            if (to_annot == null) {
                resp.sendError(500, "Please supply uri paramater.");
                return;
            }
            String bstr = to_annot.toString();
            if (bstr.contains("#")) {
                if(!bstr.endsWith("#")) {
                    bstr = bstr.substring(0, bstr.lastIndexOf("#") + 1);
                }
            } else {
                if(!bstr.endsWith("/")) {
                    bstr = bstr.substring(0, bstr.lastIndexOf("/") + 1);
                }
            }

            ServletContext ctx = getServletContext();
            Set<LIDSDescription> lidsList = (Set<LIDSDescription>) ctx.getAttribute("lidsList");

            DataSet dataSet = new DataSetNx();
            dataSet.crawlURI(to_annot.toString());

            Annotator a = new Annotator();
            for(LIDSDescription lids : lidsList) {
                a.addLIDS(lids);
            }
            Set<Node[]> sames = a.annotate(dataSet);

            Resource baser = new Resource(to_annot.toString());
            String baser_str = to_annot.toString();
            resp.setContentType("text/html");
            PrintWriter w = resp.getWriter();

            w.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            w.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><style type=\"text/css\">");
            w.println("h1 {\n"
                    + "font-size: 48px;\n"
                    + "text-align: center;\n"
                    + "            }\n"

                    + "            body {\n"
                    //+"                border-collapse: collapse;\n"
                    + "                font-family: monospace;\n"
                    + "            }\n"

                    + "          li {\n"
                    // +"                border: 1px solid black;\n"
                    + "                padding: 5px;\n"
                    + "                padding-bottom: 7px;\n"
                    + "            }\n"

                    + "            td {\n"
                    + "                vertical-align: top;\n"
                    + "            }"
                    + "        </style><title>Annotated RDF</title></head><body>");
            w.println(URIShortener.shortenURIStr(baser.toString(), bstr));
            w.println("<ul>");
//            Set<String[]> sames = sa.getSames();
            for (Node[] same : sames) {
                if (same[0].equals(baser) && !same[1].equals(baser)) {
                    w.println("<li>(NEW) owl:sameAs <a href=\"http://localhost:8080/annotator?uri=" + URLEncoder.encode(same[2].toString(), "UTF-8") + "\">" + same[2] + "</a></li>");
                }
            }
            for (Node[] r : dataSet.getTriples()) {
                if (r[0].equals(baser)) {
                    if(r[2] instanceof Resource) {
                        w.println("<li> " + URIShortener.shortenURIStr(r[1].toString(), bstr) + " <a href=\"" + r[2].toString() + "\">"+ URIShortener.shortenURIStr(r[2].toString(), bstr) + "</a><ul>");
                    } else {
                        w.println("<li> " + URIShortener.shortenURIStr(r[1].toString(), bstr) + " " + URIShortener.shortenURIStr(r[2].toString(), bstr) + "<ul>");
                    }

                    for (Node[] same : sames) {
                        if(same[0].equals(r[2])) {
                            w.println("<li>(NEW) owl:sameAs <a href=\"http://localhost:8080/annotator?uri=" + URLEncoder.encode(same[2].toString(), "UTF-8") + "\">" + same[2] + "</a></li>");
                        }
                    }
                    for (Node[] r2: dataSet.getTriples()) {
                        if(r2[0].equals(r[2])) {
                            if(r2[2] instanceof Resource) {
                                w.println("<li> " + URIShortener.shortenURIStr(r2[1].toString(), bstr) + " <a href=\"" + r2[2].toString() +"\">" + URIShortener.shortenURIStr(r2[2].toString(), bstr) +"</a>");
                            } else {
                                w.println("<li> " + URIShortener.shortenURIStr(r2[1].toString(), bstr) + " " + URIShortener.shortenURIStr(r2[2].toString(), bstr));
                            }

                            for(Node[] same : sames) {
                                if(same[0].equals(r2[2])) {
                                    w.print("(NEW: owl:sameAs <a href=\"http://localhost:8080/annotator?uri=" + URLEncoder.encode(same[2].toString(), "UTF-8") + "\">" + same[2] + "</a>)");
                                }
                            }
                            w.println("</li>");
                        }
                    }
                    w.println("</ul></li>");
                }
            }
            w.println("</ul></body></html>");
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
