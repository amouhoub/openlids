package org.openlids.facewrap;
//
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class FacewrapServlet extends HttpServlet {

    Logger _log = Logger.getLogger(this.getClass().getName());
    JSONParser parser = new JSONParser();
    public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
    static Map<String, String> namespaces = new HashMap<String, String>();

    static {
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        namespaces.put("fw", "http://openlids.org/facewrap/vocab#");
        namespaces.put("foaf", "http://xmlns.com/foaf/0.1/");
        namespaces.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        namespaces.put("v", "http://www.w3.org/2006/vcard/ns#");
        namespaces.put("og", "http://ogp.me/ns#");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("owl", "http://www.w3.org/2002/07/owl#");
    }
    static Map<String, String> propTranslation = new HashMap<String, String>();

    static {
        propTranslation.put("name", "foaf:name");
        propTranslation.put("picture", "foaf:depiction");
        // propTranslation.put( "category", "");
        propTranslation.put("website", "foaf:homepage");
        propTranslation.put("link", "foaf:homepage");
        propTranslation.put("location", "v:adr");
        propTranslation.put("street", "v:street-address");
        propTranslation.put("city", "v:locality");
        // propTranslation.put("state", "v:???"); aaa
        propTranslation.put("country", "v:country-name");
        // propTranslation.put("zip", "v:???"); aaa
        propTranslation.put("latitude", "geo:lat");
        propTranslation.put("longitude", "geo:long");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        /*
         * URIs:
         * .../thing/{facebook_id}#thing or ../page?facebookid={facebook_id}#page
         * .../search?url=...#uri => shares or even more info about the uri itself
         * .../posts?q={keywords}.optional:until={
         *      type=posts
         *
         * optional paging, until, ... parameters: are not used in description of the list, list is the same, but are added as seeAlso
         *
         */

        String path = req.getServletPath();

        String type = "";
        boolean search = false;

        if (path.endsWith("search")) {
            search = true;
        } else if (path.endsWith("thing")) {
        } else {
            String[] types = {"post", "user", "page", "event", "group", "place"};
            for (String t1 : types) {
                if (path.endsWith(t1 + "s")) {
                    type = t1;
                    search = true;
                }
            }
            if(!search) {
                _log.warning("Unknown path for facewrap Servlet: " + path);
                resp.sendError(404);
                return;
            }
        }

        String singleParameter = null;
        if (req.getParameterMap().size() == 0) {
            String restURI = req.getRequestURI().substring(path.length());
            if (restURI.startsWith("/")) {
                singleParameter = restURI.substring(1);
            }
        }

        String facebookid = null;
        String q = null;
        // Optional Parameters:
        String lat = null;
        String lng = null;


        URL url = null;

        if (search) {
            q = req.getParameter("q");
            if (q == null) {
                if (singleParameter == null) {
                    resp.sendError(400, "supply q parameter for search operations");
                    return;
                } else {
                    q = singleParameter;
                }
            }
            // Optional Parameters:
            lat = req.getParameter("lat");
            lng = req.getParameter("lng");

            if (lat != null && lng != null) {
                url = new URL("https://graph.facebook.com/search?q=" + URLEncoder.encode(q, "utf-8") + "&type=" + type + "&center=" + lat + "," + lng);
            } else {
                url = new URL("https://graph.facebook.com/search?q=" + URLEncoder.encode(q, "utf-8") + "&type=" + type);
            }

        } else {
            facebookid = req.getParameter("facebookid");

            if (facebookid == null) {
                if (singleParameter == null) {
                    resp.sendError(400, "please supply facebook id parameter.");
                    return;
                } else {
                    facebookid = singleParameter;
                }
            }
            url = new URL("https://graph.facebook.com/" + URLEncoder.encode(facebookid, "utf-8"));
        }

        _log.info("URL: " + url);

        ServletContext ctx = getServletContext();
       // Cache cache = (Cache) ctx.getAttribute(Listener.CACHE);

        try {

            String result = null;

            /*if (cache != null) {
                if (cache.containsKey(url)) {
                    result = (String) cache.get(url);
                }
            }*/

            if (result == null) {

                _log.warning("Retrieving: " + url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();

                String encoding = conn.getContentEncoding();
                if (encoding == null) {
                    encoding = "ISO_8859-1";
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    _log.info("Parsing JSON ... ");
                    JSONObject page = (JSONObject) parser.parse(new InputStreamReader(is));
                    _log.info("[OK]");
                    _log.warning("Retrieved: " + page.toJSONString());

//                      _log.severe("result: " + page.toJSONString());
                          

                    if (page.containsKey("error")) {
                        JSONObject error = (JSONObject) page.get("error");
                        resp.sendError(400, "Error (" + error.get("type") + "):\n" + error.get("message"));
                        return;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    XMLOutputFactory factory = XMLOutputFactory.newInstance();
                    XMLStreamWriter ch = factory.createXMLStreamWriter(baos, "utf-8");
                    ch.writeStartDocument("utf-8", "1.0");
                    ch.writeStartElement("rdf:RDF");
                    for (String abbr : namespaces.keySet()) {
                        ch.writeNamespace(abbr, namespaces.get(abbr));
                    }
                    ch.writeStartElement("rdf:Description");
                    ch.writeAttribute("rdf:about", "");
                    ch.writeStartElement("rdfs:comment");
                    ch.writeCharacters("Source: facebook Graph API (http://developers.facebook.com/docs/api).");
                    ch.writeEndElement();
                    ch.writeEndElement();

                    if (search) {
                        ch.writeStartElement("rdf:Description");
                        ch.writeAttribute("rdf:ID", "list");
                        ch.writeStartElement("fw:search_term");
                        ch.writeCharacters(q);
                        ch.writeEndElement();
                        ch.writeStartElement("fw:search_type");
                        ch.writeCharacters(type);
                        ch.writeEndElement();
                        if (lat != null && lng != null) {
                            ch.writeStartElement("fw:search_center");
                            ch.writeStartElement("rdf:Description");
                            ch.writeStartElement("geo:lat");
                            ch.writeCharacters(lat);
                            ch.writeEndElement();
                            ch.writeStartElement("geo:long");
                            ch.writeCharacters(lng);
                            ch.writeEndElement();
                            ch.writeEndElement();
                            ch.writeEndElement();
                        }
                        if (page.containsKey("data")) {
                            List list = (List) page.get("data");
                            _log.severe("List.size: " + list.size());
                            for (Object o : list) {
                                jsonToRDF(factory, ch, "foaf:topic", o);
                            }
                        } 
                        ch.writeEndDocument();
                    } else {

                        ch.writeStartElement("rdf:Description");
                        ch.writeAttribute("rdf:ID", "thing");

                        if (page.containsKey("location")) {
                            JSONObject loc = (JSONObject) page.get("location");
                            if (loc.containsKey("latitude") && loc.containsKey("longitude")) {
                                ch.writeStartElement("foaf:based_near");
                                ch.writeStartElement("rdf:Description");
                                ch.writeStartElement("geo:lat");
                                ch.writeCharacters((String) loc.get("latitude").toString());
                                ch.writeEndElement();
                                ch.writeStartElement("geo:long");
                                ch.writeCharacters((String) loc.get("longitude").toString());
                                ch.writeEndElement();
                                ch.writeEndElement();
                                ch.writeEndElement();
                            }
                        }
                        if (page.containsKey("latitude") && page.containsKey("longitude")) {
                            ch.writeStartElement("foaf:based_near");
                            ch.writeStartElement("rdf:Description");
                            ch.writeStartElement("geo:lat");
                            ch.writeCharacters((String) page.get("latitude").toString());
                            ch.writeEndElement();
                            ch.writeStartElement("geo:long");
                            ch.writeCharacters((String) page.get("longitude").toString());
                            ch.writeEndElement();
                            ch.writeEndElement();
                            ch.writeEndElement();
                        }

                        for (Object key : page.keySet()) {
                            if (key instanceof String) {
                                String prop = propTranslation.get((String) key);
                                if (prop == null) {
                                    prop = "og:" + (String) key;
                                }

                                jsonToRDF(factory, ch, prop, page.get(key));

                            }
                        }
                        ch.writeEndElement();
                        ch.writeEndElement();
                        ch.writeEndDocument();
                    }
                    ch.close();

                    result = new String(baos.toByteArray(), "utf-8");

               //     if (cache != null) {
                 //       cache.put(url, result);
                   // }

                } else {
                    _log.info("Parsing JSON ... ");
                    JSONObject page = (JSONObject) parser.parse(new InputStreamReader(is));
                    _log.info("[OK]");
                    _log.warning("Retrieved: " + page.toJSONString());
                    if (page.containsKey("error")) {
                        JSONObject error = (JSONObject) page.get("error");
                        resp.sendError(400, "Error (" + error.get("type") + "):\n" + error.get("message"));
                        return;
                    } else {
                        resp.sendError(conn.getResponseCode(), conn.getResponseMessage());
                        return;
                    }
                    // Server returned HTTP error code.
                }
            }

            resp.setContentType("application/rdf+xml");
            PrintWriter out = resp.getWriter();

            resp.setHeader("Cache-Control", "public");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1);
            resp.setHeader("Expires", RFC822.format(cal.getTime()));

            out.print(result);


        } catch (MalformedURLException e) {
            // ...
        } catch (IOException e) {
            // ...
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //		resp.setContentType("text/plain");
        //		resp.getWriter().println("Hello, world");
    }

    String addTrailingSlash(String uri) {
        if (uri.endsWith("/")) {
            return uri;
        }
        if (!uri.startsWith("http") && !uri.startsWith("mailto")) {
            uri = "http://" + uri;
        }
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            if (uri.lastIndexOf('/') == uri.indexOf('/') + 1) {
                return uri + "/";
            }
            String lastSubstring = uri.substring(uri.lastIndexOf('/'));
            if (lastSubstring.contains(".")) {
                return uri;
            }
            return uri + "/";
        }
        return uri;
    }

    void jsonToRDF(XMLOutputFactory factory, XMLStreamWriter ch, String prop, Object json) throws XMLStreamException {
        if (json == null) {
            return;
        }

        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            ch.writeStartElement(prop);
            ch.writeStartElement("rdf:Description");
            if (obj.containsKey("id")) {
                ch.writeAttribute("rdf:about", "/facewrap/thing/" + obj.get("id") + "#thing");
            }
            for (Object key : obj.keySet()) {
                if (key instanceof String) {
                    String prop2 = propTranslation.get((String) key);
                    if (prop2 == null) {
                        prop2 = "og:" + (String) key;
                    }
                    jsonToRDF(factory, ch, prop2, obj.get(key));
                }
            }
            ch.writeEndElement();
            ch.writeEndElement();
        } else if (json instanceof List) {
            for (Object obj : (List) json) {
                jsonToRDF(factory, ch, prop, obj);
            }
        } else if (json instanceof String && prop.equals("foaf:homepage")) {
            for (String hp : ((String) json).split("\n")) {
                if (hp == null || hp.trim().equals("")) {
                    continue;
                }
                ch.writeStartElement(prop);
                hp = addTrailingSlash(hp);
                ch.writeAttribute("rdf:resource", hp.trim());
                // ch.writeCharacters(hp);
                ch.writeEndElement();
            }
        } else {
            if (!json.toString().equals("")) {
                ch.writeStartElement(prop);
                ch.writeCharacters(json.toString());
                ch.writeEndElement();
            }
        }
    }
}
