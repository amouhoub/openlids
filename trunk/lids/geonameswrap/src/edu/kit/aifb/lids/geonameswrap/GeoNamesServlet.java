package edu.kit.aifb.lids.geonameswrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
public class GeoNamesServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();

		try {
			Map<String, String[]> params = req.getParameterMap();

			String path = req.getServletPath();

			_log.info("path: " + path);

			String url = Listener.generateURL("http://ws.geonames.org" + path, params);

			System.out.println(url);
			
			URL u = new URL(url);

			_log.info("url: " + u);

			URL geo = new URL(url);
			
			HttpURLConnection conn = (HttpURLConnection)geo.openConnection();
			InputStream is = conn.getInputStream();

			if (conn.getResponseCode() != 200) {
				resp.sendError(500, "geonames returned " + conn.getResponseCode() + " " + Listener.streamToString(conn.getErrorStream()));
				return;
			}

			String encoding = conn.getContentEncoding();
			if (encoding == null) {
				encoding = "utf-8";
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
			StringReader sr = new StringReader(str);
			
			Transformer t = (Transformer)ctx.getAttribute(Listener.GEONAMES);

			resp.setContentType("application/rdf+xml");
			
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

//			BufferedReader br2 = new BufferedReader(sr);
//
//			String line2 = null;
//			while ((line2 = br2.readLine()) != null) {
//				if (line2.startsWith("<geonameId>")) {
//					int end = line2.indexOf("</geonameId>");
//
//					geonames = new URI("http://sws.geonames.org/" + line2.substring(11, end) + "/");
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendError(500, e.getMessage());
		}

//		resp.setContentType("application/rdf+xml");
//
//		resp.setHeader("Cache-Control", "public");
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.DATE, 1);
//		resp.setHeader("Expires", Listener.RFC822.format(cal.getTime()));
//
//		out.println("<?xml version='1.0'?>");
//		out.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'");
//		out.println("    xmlns:foaf='http://xmlns.com/foaf/0.1/'>\n");
//
//		out.println("<rdf:Description rdf:ID='point'>");
//		if (geonames != null) {
//			out.println("   <foaf:based_near rdf:resource='" + geonames + "'/>");
//		}
//		out.println("</rdf:Description>");
//
//		out.println("</rdf:RDF>");

		os.close();
	}
}
