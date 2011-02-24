package com.ontologycentral.twittersearchwrap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import javax.cache.Cache;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.ElementRemover;
import org.cyberneko.html.filters.Writer;


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
			String lang = req.getParameter("lang");
			if(lang==null){
				lang="en";
			}
//			String id = req.getRequestURI();
//			id = id.substring(id.lastIndexOf("/")+1);
			
			query = URLEncoder.encode(query, "utf-8");

			System.out.println("SEARCH-QUERY:   " + query);

			URL u = new URL("http://search.twitter.com/search.atom?q=" + query + "&rpp=100");
			
			System.out.println("SEARCH-URL:     " + u);
			
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
				
				// If extern=true get content from referenced websites as string and use in wikifier
				String extern = req.getParameter("extern");
				if(extern==null){
					extern = "false";
				}
				
				StringBuffer externalWebsites = new StringBuffer();
				
				if(extern.equals("true")){
					
					// Get HTTP-links
					String[] splitContent = str.split("</content>");
					StringBuilder tweetsHTML = new StringBuilder();
					
					for(int i = 1; i<splitContent.length-1; i++){
						String[] splitsplitContent = splitContent[i].split("<content type=\"html\">");
						tweetsHTML.append(" " + splitsplitContent[1]);	
					}
					
					String[] tweetsHTMLSplit = tweetsHTML.toString().split("&lt;a href=&quot;");
					String[] urls = new String[tweetsHTMLSplit.length-1];
					
					for(int i = 1; i<tweetsHTMLSplit.length; i++){
					    urls[i-1] = tweetsHTMLSplit[i].toString().split("&quot;&gt;")[0];
					}
					
					// Get content of linked sites
					for (int i = 0; i < urls.length; i++) {

						// Dont use internal twitter links containing javascript which cannot be handled
						if(!urls[i].contains("twitter")){
							
						    BufferedReader content = null;
							String response = "";
							String encoded = "UTF-8";
							try {
								// Create the HttpURLConnection
								URL url = new URL(urls[i]);
								HttpURLConnection connection = (HttpURLConnection) url.openConnection();
								content = null;
								connection.setRequestMethod("GET");
								connection.setReadTimeout(15*1000);
								connection.connect();

								// Read the output from the server
								content = new BufferedReader(new InputStreamReader(connection.getInputStream()));
								response = connection.getResponseMessage();
								try {
									if(connection.getContentEncoding()!=null){
										encoded = connection.getContentEncoding();
									};
								} catch (Exception e) {
									System.out.println("ERROR retrieving charset from " + urls[i]);
									System.out.println("Assuming UTF-8 Encoding...");
								}
								
							} catch (Exception e) {
								System.out.println("ERROR connecting to " + urls[i]);
								e.printStackTrace();
							}
						      
							// Create element remover filter
							ElementRemover remover;
							remover = new ElementRemover();
							remover.removeElement("script");
							remover.removeElement("link");
							remover.removeElement("style");
							remover.removeElement("CDATA");
							remover.removeElement("<!--");
							remover.removeElement("meta");
							

							if (response.equals("OK") && content != null) {
									System.out.println("EXTERNAL URL: " + urls[i]);
									String readerstring = getHtmlFilteredString(content, encoded);
									readerstring = readerstring.replaceAll("\\<.*?\\>", "");
									readerstring = readerstring.replaceAll("\\(.*?\\)", "");
									readerstring = readerstring.replaceAll("\\{.*?\\}", "");
									readerstring = readerstring.replaceAll("\\s+", " ");
									BufferedReader reader = new BufferedReader(new StringReader(readerstring));

									List<String> words = new ArrayList<String>();

									String lines = null;

									while ((lines = reader.readLine()) != null) {
										StringTokenizer st = new StringTokenizer(
												lines);

										while (st.hasMoreTokens()
												&& words.size() < 100) {
											String tok = st.nextToken();
											words.add(tok);
										}
									}
									for (int j = 0; j < words.size(); j++) {
										externalWebsites.append(words.get(j));
										externalWebsites.append(" ");
									}
									words.clear();

								
							}
						}
					}
					// Append external content to tweet content
					tweets.append(externalWebsites.toString().replaceAll("\\s+", " "));
				}
				Set<String> wikifyResult = Wikify.startWikify(tweets.toString(),lang);
				System.out.println("WIKIFY RESULTS:");
				System.out.println(wikifyResult);
			
				// Append DBPedia links to atom feed to include in transformation
				
				Iterator<String> it = wikifyResult.iterator();
				StringBuffer seeAlso = new StringBuffer();
				while(it.hasNext()){
					seeAlso.append("<seeAlso>");
					seeAlso.append("<link>");
					if(lang.equals("de")){
						seeAlso.append("http://de.dbpedia.org/resource/");
					}else{
						seeAlso.append("http://dbpedia.org/resource/");
					}
					seeAlso.append(it.next());
					seeAlso.append("</link>");
					seeAlso.append("</seeAlso>");
					seeAlso.append("\n");
					
				}
				
				str = str.replaceAll("</feed>", "") + seeAlso.toString() + "\n</feed>";
				
				sr = new StringReader(str);
			}

			Transformer t = (Transformer)ctx.getAttribute(Listener.T);

			resp.setHeader("Cache-Control", "public");
//			Calendar c = Calendar.getInstance();
//			c.add(Calendar.DATE, 1);
//			resp.setHeader("Expires", RFC822.format(c.getTime()));

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

	private String getHtmlFilteredString(Reader reader, String encoding)
	{
	 
	  // create element remover filter
	  ElementRemover remover;
	  remover = new ElementRemover();
	  remover.removeElement("script");
	  remover.removeElement("link");
	  remover.removeElement("style");
	  remover.removeElement("CDATA");
	  remover.removeElement("<!--");
	  remover.removeElement("meta");
	 
	  OutputStream stream = new ByteArrayOutputStream();
	
	  try
	  {
	    XMLDocumentFilter writer = new Writer(stream, encoding);
	 
	    XMLDocumentFilter[] filters = {remover, writer};
	 
	    XMLInputSource source = new XMLInputSource(null, null, null, reader, null);
	 
	    XMLParserConfiguration parser = new HTMLConfiguration();
	    parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
	 
	    parser.parse(source);
	 
	  } catch (Exception e) {
	 
	    e.printStackTrace();
	  }
	 
	  String content = stream.toString().trim();
	 
	  return content;
	}
	
}
