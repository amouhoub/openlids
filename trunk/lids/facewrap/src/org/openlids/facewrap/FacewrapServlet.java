package org.openlids.facewrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.cache.Cache;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@SuppressWarnings("serial")
public class FacewrapServlet extends HttpServlet {

	JSONParser parser = new JSONParser();

	public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);


	static String namespaces = "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
	"    xmlns:foaf='http://xmlns.com/foaf/0.1/'\n" +
	"    xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'\n" +
	"    xmlns:v='http://www.w3.org/2006/vcard/ns#'\n" +
	"    xmlns:og='http://ogp.me/ns#'";

	static Map<String,String> propTranslation = new HashMap<String,String>();
	static {
		propTranslation.put("name", "foaf:name");
		propTranslation.put("picture","foaf:depiction");
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


		String facebookid = req.getParameter("facebookid");

		if (facebookid == null) {
			resp.sendError(400, "please supply facebook id parameter.");
			return;
		}

		ServletContext ctx = getServletContext();
		Cache cache = (Cache)ctx.getAttribute(Listener.CACHE);


		
		try {
			URL url = new URL("https://graph.facebook.com/" + URLEncoder.encode(facebookid, "utf-8"));

			String result = null;

			if(cache != null) {
				if(cache.containsKey(url)) {
					result = (String) cache.get(url);
				}
			}

			if(result == null) {

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				InputStream is = conn.getInputStream();

				String encoding = conn.getContentEncoding();
				if (encoding == null) {
					encoding = "ISO_8859-1";
				}

				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

					JSONObject page = (JSONObject) parser.parse(new InputStreamReader(is));

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					XMLOutputFactory factory = XMLOutputFactory.newInstance();
					XMLStreamWriter ch = factory.createXMLStreamWriter(baos, "utf-8");
					ch.writeStartDocument("utf-8", "1.0");
					ch.writeStartElement("rdf:RDF");
					ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
					ch.writeNamespace("foaf", "http://xmlns.com/foaf/0.1/");
					ch.writeNamespace("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
					ch.writeNamespace("v","http://www.w3.org/2006/vcard/ns#");
					ch.writeNamespace("og","http://ogp.me/ns#");



					ch.writeStartElement("rdf:Description");
					ch.writeAttribute("rdf:ID", "page");

					if(page.containsKey("location")) {
						JSONObject loc = (JSONObject) page.get("location");
						if(loc.containsKey("latitude") && loc.containsKey("longitude")) {
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
					if(page.containsKey("latitude") && page.containsKey("longitude")) {
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
					
					for(Object key : page.keySet()) {
						if(key instanceof String) {
							String prop = propTranslation.get((String) key);
							if(prop == null) {
								prop = "og:" + (String) key;
							}
							
							jsonToRDF(factory, ch, prop, page.get(key));
							
						}
					}
					ch.writeEndElement();
					ch.writeEndElement();
					ch.writeEndDocument();
					
					ch.close();
					
					result = new String(baos.toByteArray(), "utf-8");
					
					if (cache != null) {
	   					cache.put(url, result);
	   				}
					
				} else {
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
		if(uri.endsWith("/")) {
			return uri;
		}
		if(uri.startsWith("http://") || uri.startsWith("https://")) {
			if(uri.lastIndexOf('/') == uri.indexOf('/') + 1) {
				return uri + "/";
			}
			String lastSubstring = uri.substring(uri.lastIndexOf('/'));
			if(lastSubstring.contains("."))
				return uri;
			return uri + "/";
		}
		return uri;
	}

	void jsonToRDF(XMLOutputFactory factory, XMLStreamWriter ch, String prop, Object json) throws XMLStreamException {
		if(json == null) {
			return;
		}
					
		if(json instanceof JSONObject) {
			JSONObject obj = (JSONObject) json;
			ch.writeStartElement(prop);
			ch.writeStartElement("rdf:Description");
			
			for(Object key : obj.keySet()) {
				if(key instanceof String) {
					String prop2 = propTranslation.get((String) key);
					if(prop2 == null) {
						prop2 = "og:" + (String) key;
					}
					jsonToRDF(factory, ch, prop2, obj.get(key));
				}
			}
			ch.writeEndElement();
			ch.writeEndElement();
		} else if(json instanceof List) {
			for(Object obj : (List) json) {
				jsonToRDF(factory, ch, prop, obj);
			}
		} else if (json instanceof String && prop.equals("foaf:homepage")) {
			for(String hp : ((String) json).split("\n")) {
				if(hp == null || hp.trim().equals(""))
					continue;
				ch.writeStartElement(prop);
				hp = addTrailingSlash(hp);
				ch.writeAttribute("rdf:resource", hp.trim());
				// ch.writeCharacters(hp);
				ch.writeEndElement();
			}
		} else {
			if(!json.toString().equals("")) {
				ch.writeStartElement(prop);
				ch.writeCharacters(json.toString());
				ch.writeEndElement();
			}
		}
	}

}
