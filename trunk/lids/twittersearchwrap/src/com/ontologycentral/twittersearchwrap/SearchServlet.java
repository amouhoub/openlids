package com.ontologycentral.twittersearchwrap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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

		File[] langResFiles = (File[])ctx.getAttribute(Listener.LANG_RESOURCES_FOLDER);
		LanguageDetector langdetect = new LanguageDetector(langResFiles);


		StringBuilder searchResult;
		Map<String,String> langTweets = new HashMap<String, String>();

		try {
			String query = req.getParameter("q");

			String language = req.getParameter("lang");
			boolean guessLanguage = false;
			if( language == null || language.equals("auto")){
				guessLanguage = true;
			}

			query = URLEncoder.encode(query, "utf-8");
			System.out.println("SEARCH-QUERY:   " + query);

			URL u = new URL("http://search.twitter.com/search.atom?q=" + query + "&rpp=100");
			System.out.println("SEARCH-URL:     " + u);

			//execute query
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();

			if (conn.getResponseCode() != 200) {
				resp.sendError(conn.getResponseCode(), streamToString(conn.getErrorStream()));
				return;
			}

			String encoding = conn.getContentEncoding();
			if (encoding == null) {
				encoding = "utf-8";
			}

			//parse result into one string
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			String resultLine = null;
			searchResult = new StringBuilder();
			while ((resultLine = in.readLine()) != null) {
				searchResult = searchResult.append(resultLine + '\n');
			}
			in.close();

			if (searchResult.length() < 40) {
				resp.sendError(404, "response too short");
				return;
			}


			//WIKIFY search feed data
			//build strings containing text of tweets in each language
			String[] split = searchResult.toString().split("</title>");

			String tweet;
			String tweets;
			for(int i = 1; i<split.length-1; i++){
				tweet = split[i].split("<title>")[1];
				//get language of tweet
				if(guessLanguage){
					language = langdetect.detectLanguage(tweet);
				}
				if( langTweets.containsKey(language) ){
					tweets = langTweets.get(language);
				} else {
					tweets = "";
				}
				tweets = tweets + " " + tweet;
				langTweets.put(language, tweets);
			}

			//if extern=true get content from referenced websites as string and use in wikifier
			String extern = req.getParameter("extern");
			if(extern==null){
				extern = "false";
			}

			if(extern.equals("true")){

				//get HTTP-links
				String[] contents = searchResult.toString().split("</content>");

				StringBuilder tweetsHTML = new StringBuilder();

				for(int i = 1; i<contents.length-1; i++){
					String[] content = contents[i].split("<content type=\"html\">");
					tweetsHTML.append(" " + content[1]);  
				}

				String[] tweetsHTMLSplit = tweetsHTML.toString().split("&lt;a href=\"");
				String[] urlarray = new String[tweetsHTMLSplit.length-1];

				for(int i = 1; i<tweetsHTMLSplit.length; i++){
					urlarray[i-1] = tweetsHTMLSplit[i].toString().split("\"")[0];
				}

				Set<String> urls = new HashSet<String>();

				for(int i=0; i<urlarray.length;i++){
					urls.add(urlarray[i]);
				}

				Iterator<String> urliter = urls.iterator();
				//maximum of 30 links used
				int count=0;

				//get content of linked sites
				while (urliter.hasNext() && count<30) {
					String thisurl = urliter.next().toString();
					//don't use internal twitter links containing javascript which cannot be handled
					if(!thisurl.contains("twitter")){
					    String[] subjects = null;
						try {
							subjects = SubjectExtraction.extract(thisurl);
						} catch (Exception e) {
						}
						if (subjects!= null) {
							count=count+1;
							System.out.println("EXTERNAL URL No.: " + count + " : " + thisurl);
							StringBuilder externalWebsite = new StringBuilder();
							for(String subject:subjects){
								externalWebsite.append(subject + " ");
								System.out.println("WEBSITE SUBJECT : " + subject);
							}
							String externalLanguage = language;
							if(guessLanguage){
							    externalLanguage = langdetect.detectLanguage(externalWebsite.toString());
							}
							System.out.println("WEBSITE LANGUAGE: " + externalLanguage);
							if( langTweets.containsKey(externalLanguage) ){
								tweets = langTweets.get(externalLanguage);
							} else {
								tweets = "";
							}
							tweets = tweets + " " + externalWebsite;
							langTweets.put(externalLanguage, tweets);
						}
					}
				}
			}
			Set<String> wikifyResult = new HashSet<String>();
			for( String lang : langTweets.keySet() ){
				wikifyResult.addAll(Wikify.startWikify(langTweets.get(lang),lang));
			}
			System.out.println("WIKIFY RESULTS:");
			System.out.println(wikifyResult);

			//append DBPedia links to atom feed to include in transformation

			Iterator<String> entityIter = wikifyResult.iterator();
			StringBuffer seeAlso = new StringBuffer();
			while(entityIter.hasNext()){
				seeAlso.append("<seeAlso>");
				seeAlso.append("<link>");
				//				if(lang.equals("de")){
				//					seeAlso.append("http://de.dbpedia.org/resource/");
				//				}else{
				seeAlso.append("http://dbpedia.org/resource/");
				//				}
				seeAlso.append(entityIter.next());
				seeAlso.append("</link>");
				seeAlso.append("</seeAlso>");
				seeAlso.append("\n");

			}

			String searchResultString = searchResult.toString().replaceAll("</feed>", "") + seeAlso.toString() + "\n</feed>";

			Transformer t = (Transformer)ctx.getAttribute(Listener.T);

			resp.setHeader("Cache-Control", "public");
			//			Calendar c = Calendar.getInstance();
			//			c.add(Calendar.DATE, 1);
			//			resp.setHeader("Expires", RFC822.format(c.getTime()));

			try {
				StreamSource ssource = new StreamSource(new StringReader(searchResultString));
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

		//create element remover filter
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
