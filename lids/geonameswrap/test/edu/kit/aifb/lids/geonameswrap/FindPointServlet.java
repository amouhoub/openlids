package edu.kit.aifb.lids.geonameswrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.cache.Cache;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.kit.aifb.lids.geonameswrap.Listener;

@SuppressWarnings("serial")
public class FindPointServlet extends HttpServlet {
	public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getServerName().contains("appspot.com")) {
			try {
				URI re = new URI("http://geowrap.openlids.org/" + req.getRequestURI() + "?" + req.getQueryString());
				re = re.normalize();
				resp.sendRedirect(re.toString());
			} catch (URISyntaxException e) {
				resp.sendError(500, e.getMessage());
			}
			return;
		}
		
		resp.setContentType("application/rdf+xml");
		
		OutputStream os = resp.getOutputStream();
		PrintWriter out = new PrintWriter(os);
		//OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");
		
		String lat = req.getParameter("lat");
		String lng = req.getParameter("lng");

		if (lat == null || lng == null) {
			resp.sendError(400, "please supply lat and lng parameters");
			return;
		}
	
		ServletContext ctx = getServletContext();
		Cache cache = (Cache)ctx.getAttribute(Listener.CACHE);
		StringReader sr = null;

		URI geonames = null;
		try {
			URL geo = new URL("http://ws.geonames.org/findNearby?lat=" + lat + "&lng=" + lng);
			
   			if (cache.containsKey(geo)) {
   				sr = new StringReader((String)cache.get(geo));
   			}
   			
   			if (sr == null) {
   				HttpURLConnection conn = (HttpURLConnection)geo.openConnection();
   				InputStream is = conn.getInputStream();

   				if (conn.getResponseCode() != 200) {
   					resp.sendError(500, "geonames returned " + conn.getResponseCode() + " " + streamToString(conn.getErrorStream()));
   					return;
   				}

   				String encoding = conn.getContentEncoding();
   				if (encoding == null) {
   					encoding = "ISO_8859-1";
   				}

   				BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));
   				String l;
   				StringBuilder sb = new StringBuilder();

   				while ((l = in.readLine()) != null) {
   					sb.append(l);
   					sb.append('\n');
   				}
   				in.close();

   				String str = sb.toString();
   				sr = new StringReader(str);

   				cache.put(geo, str);
   			}
   			
   			BufferedReader br2 = new BufferedReader(sr);

   			String line2 = null;
   			while ((line2 = br2.readLine()) != null) {
   				if (line2.startsWith("<geonameId>")) {
   					int end = line2.indexOf("</geonameId>");

   					geonames = new URI("http://sws.geonames.org/" + line2.substring(11, end) + "/");
   				}
   			}
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendError(500, e.getMessage());
		}

		if (geonames != null) {
			resp.setHeader("Cache-Control", "public");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			resp.setHeader("Expires", RFC822.format(cal.getTime()));

			out.println("<?xml version='1.0'?>");
			out.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'");
			out.println("    xmlns:owl='http://www.w3.org/2002/07/owl#'");
			out.println("    xmlns:foaf='http://xmlns.com/foaf/0.1/'>\n");
			
			out.println("<rdf:Description rdf:ID='point'>");
			out.println("   <owl:sameAs rdf:resource='" + geonames + "' />");
			out.println("</rdf:Description>");

			out.println("</rdf:RDF>");
		}

		out.close();
	}

	public static String streamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();

		if (is != null) {
			String line;

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
		}
		
		return sb.toString();
	}
}
