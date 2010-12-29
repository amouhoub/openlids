package edu.kit.aifb.lids.twitterwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

@SuppressWarnings("serial")
public class GeoSearchServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/rdf+xml");

		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();
		//OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

		Map<String, String[]> params = req.getParameterMap();

		String path = req.getServletPath();

		_log.info("path: " + path);

		String url = Listener.generateURL("http://api.twitter.com/1" + path + ".xml", params);

		URL u = new URL(url);

		_log.info("url: " + u);

		HttpURLConnection conn = (HttpURLConnection)u.openConnection();

		if (conn.getResponseCode() != 200) {
			resp.sendError(conn.getResponseCode(), u + ": " + Listener.streamToString(conn.getErrorStream()));
			return;
		}

		String encoding = conn.getContentEncoding();
		if (encoding == null) {
			encoding = "utf-8";
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));

		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = in.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}

		in.close();

		if (sb.length() < 40) {
			resp.sendError(404, "response too short");
			return;
		}

		String str = sb.toString();

		//sr = new StringReader(str);
		
		resp.setHeader("Cache-Control", "public");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		resp.setHeader("Expires", Listener.RFC822.format(c.getTime()));

		XMLSerializer serializer = new XMLSerializer(); 
		JSON json = JSONSerializer.toJSON(str); 
		String xml = serializer.write(json);  
		PrintWriter pw = new PrintWriter(os);
		pw.println(xml);     

		pw.close();
		/*

        XMLStreamWriter ch;

		XMLOutputFactory factory = XMLOutputFactory.newInstance();

		try {
			ch = factory.createXMLStreamWriter(os);
			ch.writeStartDocument("utf-8", "1.0");

			ch.writeStartElement("rdf:RDF");
			ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			ch.writeNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			ch.writeNamespace("owl", "http://www.w3.org/2002/07/owl#"); 
			ch.writeNamespace("dc", "http://purl.org/dc/elements/1.1/");
			ch.writeNamespace("dcterms", "http://purl.org/dc/terms/");
			ch.writeNamespace("skos", "http://www.w3.org/2008/05/skos#");
			ch.writeNamespace("foaf", "http://xmlns.com/foaf/0.1/");

			ch.writeStartElement("rdf:Description");
			ch.writeAttribute("rdf:about", "");
			ch.writeStartElement("rdfs:comment");
			ch.writeCharacters("Source: Twitter API (http://www.twitter.com/) via twitterwrap.");
			ch.writeEndElement();
			ch.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
			resp.sendError(500, e.getMessage());
		}
	*/
		os.close();
	}
}

/*

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@SuppressWarnings("serial")
public class GeoSearchServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/rdf+xml");

		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();
		//OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

		StringReader sr = null;

		try {
			Map<String, String[]> params = req.getParameterMap();
			
			String path = req.getServletPath();
			
			_log.info("path: " + path);

			String url = Listener.generateURL("http://api.twitter.com/1" + path + ".xml", params);

			URL u = new URL(url);

			_log.info("url: " + u);

			if (sr == null) {
				HttpURLConnection conn = (HttpURLConnection)u.openConnection();

				if (conn.getResponseCode() != 200) {
					resp.sendError(conn.getResponseCode(), u + ": " + Listener.streamToString(conn.getErrorStream()));
					return;
				}

				String encoding = conn.getContentEncoding();
				if (encoding == null) {
					encoding = "utf-8";
				}

				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));

				StringBuilder sb = new StringBuilder();
				String line = null;

				while ((line = in.readLine()) != null) {
					sb.append(line);
					sb.append('\n');
				}

				in.close();

				if (sb.length() < 40) {
					resp.sendError(404, "response too short");
					return;
				}

				String str = sb.toString();

				sr = new StringReader(str);
			}

			Transformer t = (Transformer)ctx.getAttribute(Listener.FRIENDS);

			resp.setHeader("Cache-Control", "public");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			resp.setHeader("Expires", Listener.RFC822.format(c.getTime()));

			try {
				t.transform(new StreamSource(sr), new StreamResult(os));
			} catch (TransformerException e) {
				e.printStackTrace(); 
				resp.sendError(500, e.getMessage());
			}
		} catch (IOException ioex) {
			resp.sendError(500, ioex.getMessage());
			return;
		}

		os.close();
	}
}

package com.ontologycentral.cbasewrap.webapp;

import java.io.BufferedReader;

public class JtoR {
	public static String COMPANY = "company";
	public static String FINORG = "financial-organization";
	public static String SERVICEPRO = "service-provider";

	public static String LAT = "latitude";
	public static String LON = "longitude";
	
	JSONObject _obj;
	XMLStreamWriter _ch;
	
	public JtoR(StringReader sr, OutputStreamWriter osw) throws XMLStreamException {
		BufferedReader reader = new BufferedReader(sr);
		
		_obj = (JSONObject)JSONValue.parse(reader);
		
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		XMLOutputFactory factory = XMLOutputFactory.newInstance();

		_ch = factory.createXMLStreamWriter(osw);
		_ch.writeStartDocument("utf-8", "1.0");

		_ch.writeStartElement("rdf:RDF");
		_ch.writeDefaultNamespace("http://ontologycentral.com/2010/05/cb/vocab#");
		_ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		_ch.writeNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		_ch.writeNamespace("owl", "http://www.w3.org/2002/07/owl#"); 
		_ch.writeNamespace("dc", "http://purl.org/dc/elements/1.1/");
		_ch.writeNamespace("dcterms", "http://purl.org/dc/terms/");
		_ch.writeNamespace("skos", "http://www.w3.org/2008/05/skos#");
		_ch.writeNamespace("foaf", "http://xmlns.com/foaf/0.1/");
		
		_ch.writeStartElement("rdf:Description");
		_ch.writeAttribute("rdf:about", "");
		_ch.writeStartElement("rdfs:comment");
		_ch.writeCharacters("Source: CrunchBase (http://www.crunchbase.com/) via cbasewrap (http://cbasewrap.ontologycentral.com/).");
		_ch.writeEndElement();
		_ch.writeStartElement("rdfs:seeAlso");
		_ch.writeAttribute("rdf:resource", "http://www.crunchbase.com/help/licensing-policy");
		_ch.writeEndElement();		
		_ch.writeEndElement();
	}

	public void close() throws XMLStreamException {
		_ch.writeEndElement();
	}
	
	public void convert(String type, String uri) throws IOException, XMLStreamException {
		if (COMPANY.equals(type)) {
			convertCompany(_obj, uri);
		} else if (FINORG.equals(type)) {
			convertCompany(_obj, uri);
		} else if (SERVICEPRO.equals(type)) {
			convertCompany(_obj, uri);
		} else {
			convertGeneric(_obj, "id");
		}
	}

	public void convertGeneric(JSONObject obj, String id) throws XMLStreamException {
		convertGeneric(obj, id, null);
	}
	
	public void convertGeneric(JSONObject obj, String id, String uri) throws XMLStreamException {
		_ch.writeStartElement("rdf:Description");
		if (id != null) {
			_ch.writeAttribute("rdf:ID", id);
		}
		
		Set keys = obj.keySet();
		for (Object k : keys) {
			Object val = obj.get(k);
			if (val != null) {
				if (val instanceof JSONArray) {
					JSONArray ar = (JSONArray)val;

					//System.out.println(k + " " + ar);
				} else if (val instanceof JSONObject) {
					JSONObject o = (JSONObject)val;
					//System.out.println(k + " " + o);
				} else {
					writeValue(k, val);
					//System.out.println(val.getClass().getName());
				}
			}
		}
		
		if (keys.contains("first_name") && keys.contains("last_name")) {
			writeValue("foaf:name", obj.get("first_name") + " " + obj.get("last_name"));
		}
		
//		if (keys.contains(LAT) && keys.contains(LON)) {
//			URI geonames = null;
//			try {
//				String lat = obj.get(LAT).toString();
//				String lon = obj.get(LON).toString();
//
//				URL geo = new URL("http://ws.geonames.org/findNearbyPlaceName?lat=" + lat + "&lng=" + lon);
//				HttpURLConnection geocon = (HttpURLConnection)geo.openConnection();
//				BufferedReader br2 = new BufferedReader(new InputStreamReader(geocon.getInputStream()));
//
//				String line2 = null;
//				while ((line2 = br2.readLine()) != null) {
//					if (line2.startsWith("<geonameId>")) {
//						int end = line2.indexOf("</geonameId>");
//
//						geonames = new URI("http://sws.geonames.org/" + line2.substring(11, end) + "/");
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			if (geonames != null) {	
			
			//writeValue("foaf:based_near", "http://geowrap.openlids.org/findNearbyPlaceName?lat=" + lat + "&lng=" + lon + "#point");
//		}

		if (obj.get("twitter_username") != null) {
			String un = obj.get("twitter_username").toString().trim();
			if (un.length() > 0) {
				writeValue("owl:sameAs", "http://twitter2foaf.appspot.com/id/" + un);
				writeValue("owl:sameAs", "http://semantictweet/" + un + "#me");
				//writeValue("rdfs:seeAlso", "http://twitterwrap.ontologycentral.com/" + un);
			}
		}
		
		if (obj.get("city") != null && obj.get("state_code") != null && uri != null) {
			try {
				String city = obj.get("city").toString();
				Object state = obj.get("state_code").toString();
				String location = URLEncoder.encode(city + ", " + state, "utf-8");
				String company = uri.substring(uri.indexOf("company/") + "company/".length());

				company = URLEncoder.encode(company.replace('-', ' ').toLowerCase(), "utf-8");

				//writeValue("rdfs:seeAlso", "http://jobswrap.ontologycentral.com/jobs?uri=" + URLEncoder.encode(uri + "#" + id, "utf-8") + "&c=" + company + "&l=" + location);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		_ch.writeEndElement();
	}
	
	public void convertCompany(JSONObject obj, String uri) throws XMLStreamException {
		String[] acdate = { "founded_year" , "founded_month", "founded_day" };

		_ch.writeStartElement("Company");
		_ch.writeAttribute("rdf:ID", "id");
		
		try {		
			writeValue("founded", getDate(obj, acdate));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		if (obj.get("permalink") != null) {
			String pl = obj.get("permalink").toString().trim();
			if (pl.length() > 0) {
				//writeValue("owl:sameAs", "http://cb.semsol.org/company/" + pl + "#self");
				//writeValue("owl:sameAs", "http://patwrap.ontologycentral.com/company?name=" + pl + "#id");
			}
		}
		
		if (obj.get("twitter_username") != null) {
			String un = obj.get("twitter_username").toString().trim();
			if (un.length() > 0) {
				//writeValue("owl:sameAs", "http://twitter2foaf.appspot.com/id/" + un);
				writeValue("owl:sameAs", "http://semantictweet.com/" + un + "#me");
				//writeValue("rdfs:seeAlso", "http://twitterwrap.ontologycentral.com/" + un);
			}
		}
		
		for (Object k : obj.keySet()) {
			Object val = obj.get(k);
			if (val != null) {
				if (val instanceof JSONArray) {
					JSONArray ar = (JSONArray)val;
					
					if ("products".equals(k.toString())) {
						convertProducts(ar);
					} else if ("competitions".equals(k.toString())) {
						convertCompetitors(ar);
					} else if ("acquisitions".equals(k.toString())) {
						convertAcquisitions(ar);
					} else if ("relationships".equals(k.toString())) {
						convertRelationships(ar);
					} else if ("providerships".equals(k.toString())) {
						convertProviderships(ar);
					} else if ("funding_rounds".equals(k.toString())) {
						convertFundingRounds(ar);
					} else if ("offices".equals(k.toString())) {
						convertOffices(ar, uri);
					}

					//System.out.println(k + " " + ar);
				} else if (val instanceof JSONObject) {
					JSONObject o = (JSONObject)val;
					//System.out.println(k + " " + o);
				} else {
					writeValue(k, val);
					//System.out.println(val.getClass().getName());
				}
			}
		}
		
		_ch.writeEndElement();
	}
	
	public void convertProducts(JSONArray ar) throws XMLStreamException {
		for (int i = 0; i < ar.size(); i ++) {
			JSONObject obj = (JSONObject)ar.get(i);

			writeValue("product", "/product/" + obj.get("permalink") + "#id");
		}
	}

	public void convertOffices(JSONArray ar, String uri) throws XMLStreamException {
		for (int i = 0; i < ar.size(); i ++) {
			_ch.writeStartElement("office");

			JSONObject obj = (JSONObject)ar.get(i);
			
			String id = obj.get("country_code") + " " + obj.get("city") + " " + obj.get("description");
			id = id.replace(' ', '-').toLowerCase();
			
			convertGeneric(obj, id, uri);
			
			_ch.writeEndElement();
		}
	}

	public void convertCompetitors(JSONArray ar) throws XMLStreamException {
		for (int i = 0; i < ar.size(); i ++) {
			JSONObject comp = (JSONObject)ar.get(i);
			JSONObject obj = (JSONObject)comp.get("competitor");

			writeValue("competitor", "/company/" + obj.get("permalink") + "#id");
		}
	}
	
	public void convertProviderships(JSONArray ar) throws XMLStreamException {
		for (int i = 0; i < ar.size(); i ++) {
			JSONObject ac = (JSONObject)ar.get(i);

			Boolean past = (Boolean)ac.get("is_past");
			
			if (past == null || past == false) {
				_ch.writeStartElement("providership");

				_ch.writeStartElement("rdf:Description");

				writeValue("title", ac.get("title"));
				
				try {				
					String type = ((JSONObject)ac.get("firm")).get("type_of_entity").toString();
					writeValue("firm", "/" + type + "/" + ((JSONObject)ac.get("firm")).get("permalink") + "#id");
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				
				_ch.writeEndElement();
				
				_ch.writeEndElement();
			}
		}
	}
	
	public void convertRelationships(JSONArray ar) throws XMLStreamException {
		for (int i = 0; i < ar.size(); i ++) {
			JSONObject ac = (JSONObject)ar.get(i);

			Boolean past = (Boolean)ac.get("is_past");
			
			if (past == null || past == false) {
				_ch.writeStartElement("relationship");

				_ch.writeStartElement("rdf:Description");

				writeValue("title", ac.get("title"));				
				writeValue("person", "/person/" + ((JSONObject)ac.get("person")).get("permalink") + "#id");

				_ch.writeEndElement();
				
				_ch.writeEndElement();

			}
		}
	}
	
	public void convertFundingRounds(JSONArray ar) throws XMLStreamException {
		String[] acdate = { "funded_year" , "funded_month", "funded_day" };

		for (int i = 0; i < ar.size(); i ++) {
			JSONObject ac = (JSONObject)ar.get(i);
			
			_ch.writeStartElement("funding");
						
			_ch.writeStartElement("rdf:Description");

			writeValue("dc:date", getDate(ac, acdate));

			writeValue("raised_currency_code", "http://ontologycentral.com/2009/05/currency/iso-4217#" + ac.get("raised_currency_code"));

			writeValue("round_code", ac.get("round_code"));

			Number l = (Number)ac.get("raised_amount");
			writeValue("raised_amount", l);

			writeValue("source_url", ac.get("source_url"));
			
			JSONArray inar = (JSONArray)ac.get("investments");
			
			for (int j = 0; j < inar.size(); j++) {
				JSONObject inob = (JSONObject)inar.get(j);
				if (inob.get("company") != null) {
					writeValue("investor", "/company/" + ((JSONObject)inob.get("company")).get("permalink") + "#id");
				} else if (inob.get("financial_org") != null) {
					writeValue("investor", "/financial-organization/" + ((JSONObject)inob.get("financial_org")).get("permalink") + "#id");				
				} else if (inob.get("person") != null) {
					writeValue("investor", "/person/" + ((JSONObject)inob.get("person")).get("permalink") + "#id");									
				}
			}
			
			_ch.writeEndElement();
			
			_ch.writeEndElement();
		}
	}

		
	public void convertAcquisitions(JSONArray ar) throws XMLStreamException {
		String[] acdate = { "acquired_year" , "acquired_month", "acquired_day" };

		for (int i = 0; i < ar.size(); i ++) {
			JSONObject ac = (JSONObject)ar.get(i);
			
			_ch.writeStartElement("acquisition");
						
			_ch.writeStartElement("rdf:Description");
			
			writeValue("company", "/company/" + ((JSONObject)ac.get("company")).get("permalink") + "#id");

			writeValue("dc:date", getDate(ac, acdate));

			writeValue("price_currency_code", "http://ontologycentral.com/2009/05/currency/iso-4217#" + ac.get("price_currency_code"));

			writeValue("term_code", ac.get("term_code"));

			writeValue("price_amount", ac.get("price_amount"));

			writeValue("source_url", ac.get("source_url"));
			 
			_ch.writeEndElement();
			
			_ch.writeEndElement();
		}
	}
	
	public void writeValue(Object k, Object val) throws XMLStreamException {
		if (val == null) {
			return;
		}
		
		if (val instanceof String) {
			String s = (String)val;
			if (s.trim().length() == 0) {
				return;
			}
			
			// XXX here do JSON -> Java string conversion
			
			_ch.writeStartElement(k.toString());

			if (s.startsWith("http")) {
				try {
					URI u = new URI(s);
					
					String path = u.getPath();
					if (path == null || path.length() == 0) {
						path = "/";
					}
					
					u = new URI(u.getScheme(), u.getAuthority(), path, u.getQuery(), u.getFragment());
					
					_ch.writeAttribute("rdf:resource", u.toString());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			} else if (s.startsWith("/")) {
				_ch.writeAttribute("rdf:resource", s);
			} else {
				_ch.writeCharacters(stripHTML(s));
			}
			
			_ch.writeEndElement();
		} else if (val instanceof Number) {
			NumberFormat f = NumberFormat.getInstance(); //loc);

			Number n = (Number)val;

			_ch.writeStartElement(k.toString());
			_ch.writeCharacters(f.format(n));
			_ch.writeEndElement();
		} else {
			_ch.writeStartElement(k.toString());
			_ch.writeCharacters(val.toString());
			_ch.writeEndElement();
		}
	}

	public String getDate(JSONObject jo, String[] preds) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < preds.length; i++) {
			String p = preds[i];
			
			Long l = (Long)jo.get(p);
			if (l != null) {
				if (i > 0) {
					sb.append("-");
				}

				String d = jo.get(p).toString();
				if (d.length() == 1) {
					sb.append(0);
				}
				sb.append(d);
			}
		}
		
		return sb.toString();
	}
	
	static String stripHTML(String s) {
		return s.replaceAll("\n", " ").replaceAll("\\<.*?\\>","").trim();
	}
}
*/

