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
import java.net.URL;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FindNearbyWikipediaServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
		StringReader sr = null;

		List<URI> geonames = new LinkedList<URI>();
		try {
			URL geo = new URL("http://ws.geonames.org/findNearbyWikipedia?lat=" + lat + "&lng=" + lng);

			HttpURLConnection conn = (HttpURLConnection)geo.openConnection();
			InputStream is = conn.getInputStream();

			if (conn.getResponseCode() != 200) {
				resp.sendError(500, "geonames returned " + conn.getResponseCode() + " " + Listener.streamToString(conn.getErrorStream()));
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

			BufferedReader br2 = new BufferedReader(sr);

			String line2 = null;
			while ((line2 = br2.readLine()) != null) {
				if (line2.startsWith("<wikipediaUrl>")) {
					int end = line2.indexOf("</wikipediaUrl>");
					String wpuri = line2.substring(14, end);
					if(wpuri.startsWith("http://en.wikipedia.org/wiki/")) {
						geonames.add(new URI("http://dbpedia.org/resource/" + wpuri.substring(29)));
					}
					// geonames.add(new URI("http://sws.geonames.org/" +  + "/"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendError(500, e.getMessage());
		}

		if (geonames != null) {
			resp.setContentType("application/rdf+xml");

			resp.setHeader("Cache-Control", "public");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			resp.setHeader("Expires", Listener.RFC822.format(cal.getTime()));

			out.println("<?xml version='1.0'?>");
			out.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'");
			out.println("    xmlns:foaf='http://xmlns.com/foaf/0.1/'>\n");

			out.println("<rdf:Description rdf:ID='point'>");
			for(URI geoname : geonames) {
				out.println("   <foaf:based_near rdf:resource='" + geoname + "'/>");
			}
			out.println("</rdf:Description>");

			out.println("</rdf:RDF>");
		} 

		out.close();
	}
}
