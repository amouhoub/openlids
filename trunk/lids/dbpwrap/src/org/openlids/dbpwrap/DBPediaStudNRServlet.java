package org.openlids.dbpwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

import org.json.simple.parser.JSONParser;


@SuppressWarnings("serial")
public class DBPediaStudNRServlet extends HttpServlet {

	JSONParser parser = new JSONParser();

	public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);


	static String namespaces = "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
	"    xmlns:foaf='http://xmlns.com/foaf/0.1/'\n" +
	"    xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'\n" +
	"    xmlns:v='http://www.w3.org/2006/vcard/ns#'\n" +
	"    xmlns:og='http://ogp.me/ns#'";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {


		String homepage = req.getParameter("homepage");

		if (homepage == null) {
			resp.sendError(400, "please supply homepage parameter.");
			return;
		}

		ServletContext ctx = getServletContext();
		Cache cache = (Cache)ctx.getAttribute(Listener.CACHE);


		
		try {
			URL url = new URL("http://dbpedia.org/sparql");
			
			// URL url = new URL("http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&should-sponge=&query=CONSTRUCT+{+%3Fs+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfStudents%3E+%3Fn+}+where+{+%3Fs+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2Fhomepage%3E+%3C" + URLEncoder.encode(homepage, "utf-8") + "%3Fs+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfStudents%3E+%3Fn+}&format=application%2Frdf%2Bxml&debug=on&timeout=");
			// URL url = new URL("http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&should-sponge=&query=SELECT+%3Fn+where+{+%3Fs+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2Fhomepage%3E+%3C" + URLEncoder.encode(homepage, "utf-8") + "%3E+.+%3Fs+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfStudents%3E+%3Fn+}&format=text%2Fhtml&debug=on&timeout=");

			String result = null;

			if(cache != null) {
				if(cache.containsKey(homepage)) {
					result = (String) cache.get(homepage);
				}
			}

			if(result == null) {

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				String postq = URLEncoder.encode("default-graph-uri","utf-8") + "=" + URLEncoder.encode("http://dbpedia.org","utf-8") + "&" +
							   URLEncoder.encode("should-sponge","utf-8") + "=" + "&" + 
							   URLEncoder.encode("query","utf-8") + "=" + URLEncoder.encode("CONSTRUCT { <http://localhost:8888/dbpediaStudNR?homepage=" + URLEncoder.encode(homepage,"utf-8") + "#university> <http://dbpedia.org/ontology/numberOfStudents> ?n } where { ?s <http://xmlns.com/foaf/0.1/homepage> <" + homepage + "> . {?s <http://dbpedia.org/ontology/numberOfStudents> ?n } UNION {?s <http://dbpedia.org/ontology/numberOfUndergraduateStudents> ?n} }", "utf-8") + "&" +
							   URLEncoder.encode("format","utf-8") + "=" + URLEncoder.encode("application/rdf+xml","utf-8");
				System.out.println(homepage);
				System.out.println(postq);
				wr.write(postq);
				wr.flush();
				
				InputStream is = conn.getInputStream();

				String encoding = conn.getContentEncoding();
				if (encoding == null) {
					encoding = "ISO_8859-1";
				}

				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

					BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));
	   				String l;
	   				StringBuilder sb = new StringBuilder();

	   				String maxMatch = "";
	   				while ((l = in.readLine()) != null) {
	   					if(l.contains("dbpedia-owl:numberOf")) {
	   						if(l.length() > maxMatch.length()) {
	   							maxMatch = l;
	   						} else if(l.length() == maxMatch.length() && l.compareTo(maxMatch) > 0) {
	   							maxMatch = l;
	   						}
	   						continue;
	   					}
	   					if(l.contains("</rdf:RDF>")) {
	   						sb.append(maxMatch);
	   					}
	   					sb.append(l);
	   					sb.append('\n');
	   				}
	   				in.close();

	   				result = sb.toString();
	   				
	   				result = result.replace(homepage, URLEncoder.encode(homepage,"utf-8"));
	   				
	   				

	   				if (cache != null) {
	   					cache.put(homepage, result);
	   				}
					
				} else {
					resp.sendError(400, "DBpedia SPARQL Endpoint returned Error " + conn.getResponseCode() + "\n" + conn.getResponseMessage());
					return;
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
			this.log("MalformedURLException " + e);
			// ...
		} catch (IOException e) {
			// ...
			this.log("IOException " + e);
		} 

		//		resp.setContentType("text/plain");
		//		resp.getWriter().println("Hello, world");
	}

}
