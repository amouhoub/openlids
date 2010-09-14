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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.cache.Cache;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		
		String lat = null;
		String lng = null;

		String str = null;
		
		try {
			URL geo = new URL("http://maps.google.com/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "utf-8") + "&sensor=false");

			if (cache != null) {
				if (cache.containsKey(geo)) {
					str = (String)cache.get(geo);
				}
			}
			
   			if (str == null) {
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

   				str = sb.toString();

   				if (cache != null) {
   					cache.put(geo, str);
   				}
   			}
   			
   			BufferedReader br2 = new BufferedReader(new StringReader(str));

   			boolean geom = false;
   			
   			String line = null;
   			while ((line = br2.readLine()) != null) {
   				if (geom == true) {
   					if (line.trim().startsWith("<lat>")) {
   						lat = line.trim().substring(5);
   						lat = lat.substring(0, lat.length()-6);
   					}
   					if (line.trim().startsWith("<lng>")) {
   						lng = line.trim().substring(5);
   						lng = lng.substring(0, lng.length()-6);
   					}
   				}

   				if (line.trim().startsWith("<geometry>")) {
   					geom = true;
   				}
   				
   				if (lat != null && lng != null) {
   					break;
   				}
   			}
   			
   			
//			StreamSource xsltSource = new StreamSource(new StringReader(XSLT));
//			TransformerFactory factory = TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader());
//			//"com.icl.saxon.TransformerFactoryImpl", this.getClass().getClassLoader());			
//			//net.sf.saxon.TransformerFactoryImpl", this.getClass().getClassLoader());
//			
//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
////   			StringWriter text = new StringWriter();
//			StreamSource xmlSource = new StreamSource(sr);
//			StreamResult target = new StreamResult(bout);
//			Transformer trans = factory.newTransformer(xsltSource);
//			trans.transform(xmlSource, target);		
//
//			StringTokenizer st = new StringTokenizer(bout.toString(), ",");
		} catch (Exception e) {
			e.printStackTrace();
			//throw new ServletException(e);
			resp.sendError(500, e.getMessage());
		}

		if (lat != null && lng != null) {
			resp.setHeader("Cache-Control", "public");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			resp.setHeader("Expires", RFC822.format(cal.getTime()));

			out.println("<?xml version='1.0'?>");
			out.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'");
			//out.println("    xmlns:foaf='http://xmlns.com/foaf/0.1/'\n");
			out.println("    xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'>\n");
			
			out.println("<rdf:Description rdf:ID='point'>");
			out.println("   <geo:location>");
			out.println("     <rdf:Description>");
			out.println("        <geo:lat>" + lat.trim() + "</geo:lat>");
			out.println("        <geo:lng>" + lng.trim() + "</geo:lng>");
			out.println("     </rdf:Description>");
			out.println("   </geo:location>");
			out.println("</rdf:Description>");

			out.println("</rdf:RDF>");
		} else {
			resp.sendError(500, str);
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
