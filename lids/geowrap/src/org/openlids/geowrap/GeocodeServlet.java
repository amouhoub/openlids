package org.openlids.geowrap;

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
import java.util.StringTokenizer;

import javax.cache.Cache;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@SuppressWarnings("serial")
public class GeocodeServlet extends HttpServlet {
	public static String XSLT = "<?xml version='1.0' encoding='utf-8'?>\n" +
		"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>\n"+
		"<xsl:output method='text' encoding='utf-8'/>\n" +
		"<xsl:template match='GeocodeResponse'><xsl:apply-templates/></xsl:template>\n" +
		"<xsl:template match='result'><xsl:value-of select='geometry/location/lat'/>,<xsl:value-of select='geometry/location/lng'/></xsl:template>\n" +
		"<xsl:template match='*'/>\n" +
		"</xsl:stylesheet>";

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
		
		String address = req.getParameter("address");

		if (address == null) {
			resp.sendError(400, "please supply address parameter");
			return;
		}

		ServletContext ctx = getServletContext();
		Cache cache = (Cache)ctx.getAttribute(Listener.CACHE);
		StringReader sr = null;
		
		String lat = null;
		String lng = null;

		try {
			URL geo = new URL("http://maps.google.com/maps/api/geocode/xml?address=" + address + "&sensor=false");
			
   			if (cache.containsKey(geo)) {
   				sr = new StringReader((String)cache.get(geo));
   			}
   			
   			if (sr == null) {
   				HttpURLConnection conn = (HttpURLConnection)geo.openConnection();
   				InputStream is = conn.getInputStream();

   				if (conn.getResponseCode() != 200) {
   					resp.sendError(500, "Google returned " + conn.getResponseCode() + " " + streamToString(conn.getErrorStream()));
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
   			
			Source xsltSource = new StreamSource(new StringReader(XSLT));
			TransformerFactory factory = TransformerFactory.newInstance();

   			String text = new String();
			Source xmlSource = new StreamSource(sr);
			Result target = new StreamResult(text);
			Transformer trans = factory.newTransformer(xsltSource);
			trans.transform(xmlSource, target);		

			StringTokenizer st = new StringTokenizer(text, ",");

			lat = st.nextToken();
			lng = st.nextToken();
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendError(500, e.getMessage());
		}

		if (lat != null && lng != null) {
			resp.setHeader("Cache-Control", "public");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			resp.setHeader("Expires", RFC822.format(cal.getTime()));

			out.println("<?xml version='1.0'?>");
			out.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'");
			out.println("    xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'>\n");
			
			out.println("<rdf:Description rdf:ID='point'>");
			out.println("   <geo:lat>" + lat + "</geo:lat>");
			out.println("   <geo:lng>" + lng + "</geo:lng>");
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
