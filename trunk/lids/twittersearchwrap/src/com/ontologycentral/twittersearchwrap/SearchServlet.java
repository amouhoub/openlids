package com.ontologycentral.twittersearchwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.cache.Cache;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {
	public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/rdf+xml");

		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();
		//OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");
		
		Cache cache = (Cache)ctx.getAttribute(Listener.CACHE);
		StringReader sr = null;

		try {
			String query = req.getParameter("q");
//			String id = req.getRequestURI();
//			id = id.substring(id.lastIndexOf("/")+1);
			
			query = URLEncoder.encode(query, "utf-8");

			System.out.println(query);

			URL u = new URL("http://search.twitter.com/search.atom?lang=en&q=" + query + "&rpp=100");
			
			System.out.println(u);
			
			try {
				if (cache != null && cache.containsKey(u)) {
					sr = new StringReader((String)cache.get(u));
				}
			} catch (Exception e) {
				e.printStackTrace();
				sr = null;
			}

			if (sr == null) {
				HttpURLConnection conn = (HttpURLConnection)u.openConnection();

				if (conn.getResponseCode() != 200) {
					resp.sendError(conn.getResponseCode(), streamToString(conn.getErrorStream()));
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
				
//				if (cache != null) {
//					cache.put(u, str);
//				}
				
				
				// WIKIFY Search feed data

				String[] split = str.split("</title>");
				
				StringBuilder tweets = new StringBuilder();
				
				for(int i = 1; i<split.length-1; i++){
					
					String[] splitsplit = split[i].split("<title>");
					
					tweets.append(" " + splitsplit[1]);
					
				}

				Set<String> wikifyResult = Wikify.startWikify(tweets.toString());
				
				System.out.println(wikifyResult);
				
				// Append DBPedia links to atom feed to include in transformation
				
				Iterator it = wikifyResult.iterator();
				StringBuffer seeAlso = new StringBuffer();
				while(it.hasNext()){
					seeAlso.append("<seeAlso>");
					seeAlso.append("<link>");
					seeAlso.append("http://dbpedia.org/resource/");
					seeAlso.append(it.next());
					seeAlso.append("</link>");
					seeAlso.append("</seeAlso>");
					seeAlso.append("\n");
					
				}
				
				str = str.replaceAll("</feed>", "") + seeAlso.toString() + "\n</feed>";
				
				System.out.println(str);
				
				sr = new StringReader(str);
			}

			Transformer t = (Transformer)ctx.getAttribute(Listener.T);

			resp.setHeader("Cache-Control", "public");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			resp.setHeader("Expires", RFC822.format(c.getTime()));

			try {
				    StreamSource ssource = new StreamSource(sr);
				    StreamResult sresult = new StreamResult(os);
					t.transform(ssource, sresult);
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
	
	public static String streamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();

		if (is != null) {
			String line;

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
			} finally {
				is.close();
			}
		}

		return sb.toString();
	}
}
