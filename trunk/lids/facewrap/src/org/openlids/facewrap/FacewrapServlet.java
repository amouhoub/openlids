package org.openlids.facewrap;
//
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
        propTranslation.put("from", "foaf:maker");
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
        String authorization = req.getHeader("Authorization");
        String access_token = null;
        if (authorization != null && authorization.startsWith("OAuth2")) {
            String key_vals[] = authorization.substring(7).split(",");
            for(String key_val : key_vals) {
                String kv[] = key_val.split("=");
                if(kv.length == 2) {
                    if(kv[0].equals("oauth2_access_token")) {
                        access_token = URLEncoder.encode(kv[1].substring(1, kv[1].length() - 1), "utf-8");
                    }
                }
            }
        }
        if(access_token == null) {
            access_token = req.getParameter("access_token");
            if(access_token != null && access_token.equals(""))
                access_token = null;
            if(access_token != null) {
                access_token = URLEncoder.encode(access_token, "utf-8");
            }
        }


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
        if (req.getParameterMap().size() == 0 || (req.getParameterMap().size() == 1 && req.getParameter("access_token") != null)) {
            String pathinfo = req.getPathInfo();
            if(pathinfo == null) pathinfo = "";
            String[] parts = pathinfo.split("/");
            if(parts.length > 0) {
                singleParameter = parts[parts.length - 1];
            }
//            String restURI = req.getRequestURI().substring(path.length());
  //          if (restURI.startsWith("/")) {
    //            singleParameter = restURI.substring(1);
      //      }
        }

        String facebookid = null;
        String q = null;
        // Optional Parameters:
        String lat = null;
        String lng = null;


        String url_str = null;
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
                url_str = "https://graph.facebook.com/search?q=" + URLEncoder.encode(q, "utf-8") + "&type=" + type + "&center=" + lat + "," + lng;
            } else {
                url_str = "https://graph.facebook.com/search?q=" + URLEncoder.encode(q, "utf-8") + "&type=" + type;
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
            url_str = "https://graph.facebook.com/" + URLEncoder.encode(facebookid, "utf-8");
        }
        
        if(access_token != null) {
            if(url_str.contains("?")) {
                url_str += "&";
            } else {
                url_str += "?";
            }
            url_str += "access_token=" + access_token;
        }

        url = new URL(url_str);

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


                        String url_friends_str = "https://graph.facebook.com/" + URLEncoder.encode(facebookid, "utf-8") + "/friends";
                        if(access_token != null) {
                            url_friends_str += "?access_token=" + access_token;
                            URL url_friends = new URL(url_friends_str);
                            HttpURLConnection friends_conn = (HttpURLConnection) url_friends.openConnection();
                            InputStream friends_is = friends_conn.getInputStream();
                            encoding = friends_conn.getContentEncoding();
                            if (encoding == null) {
                                encoding = "ISO_8859-1";
                            }
                            if (friends_conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                _log.info("Parsing JSON ... ");
                                JSONObject friends = (JSONObject) parser.parse(new InputStreamReader(friends_is));
                                _log.info("[OK]");
                                _log.warning("Retrieved: " + friends.toJSONString());

                                if (friends.containsKey("error")) {

                                } else if (friends.containsKey("data")) {
                                    Object flisto = friends.get("data");
                                    if (flisto instanceof List) {
                                        List flist = (List) flisto;
                                        for (Object friendo : flist) {
                                            if (friendo instanceof JSONObject) {
                                                JSONObject friend = (JSONObject) friendo;
                                                if (friend.containsKey("id") && friend.containsKey("id")) {
                                                    ch.writeStartElement("foaf:knows");
                                                    ch.writeStartElement("rdf:Description");
                                                    ch.writeAttribute("about", "/facewrap/thing/" + friend.get("id") + "#thing");
                                                    ch.writeStartElement("foaf:name");
                                                    ch.writeCharacters(friend.get("name").toString());
                                                    ch.writeEndElement();
                                                    ch.writeEndElement();
                                                    ch.writeEndElement();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        ch.writeEndElement();

                        
                        String url_feed_str = "https://graph.facebook.com/" + URLEncoder.encode(facebookid, "utf-8") + "/feed";
                        if (access_token != null) {
                            url_feed_str += "?access_token=" + access_token;
                            URL url_feed = new URL(url_feed_str);
                            HttpURLConnection feed_conn = (HttpURLConnection) url_feed.openConnection();
                            InputStream feed_is = feed_conn.getInputStream();
                            encoding = feed_conn.getContentEncoding();
                            if (encoding == null) {
                                encoding = "ISO_8859-1";
                            }
                            if (feed_conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                _log.info("Parsing JSON ... ");
                                JSONObject feed = (JSONObject) parser.parse(new InputStreamReader(feed_is));
                                _log.info("[OK]");
                                _log.warning("Retrieved: " + feed.toJSONString());

                                if (feed.containsKey("error")) {

                                } else if (feed.containsKey("data")) {
                                    Object flisto = feed.get("data");
                                    this.jsonToRDF(factory, ch, null, flisto);
                                }
                            }
                        }

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
            if(prop != null) {
                ch.writeStartElement(prop);
            }
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
            if(prop != null) {
                ch.writeEndElement();
            }
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
                if(json.toString().startsWith("http://") || json.toString().startsWith("https://")) {
                    try {
                        URI isItURI = new URI(json.toString());
                        ch.writeStartElement(prop);
                        ch.writeAttribute("rdf:resource", json.toString());
                        ch.writeEndElement();
                    } catch(URISyntaxException use) {
                        ch.writeStartElement(prop);
                        ch.writeCharacters(json.toString());
                        ch.writeEndElement();
                    }
                } else {
                    ch.writeStartElement(prop);
                    ch.writeCharacters(json.toString());
                    ch.writeEndElement();
                }
            }
        }
    }
}

