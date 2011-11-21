package com.ontologycentral.twittersearchwrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

		File[] langResFiles = (File[])ctx.getAttribute(Listener.LANG_RESOURCES_FOLDER);
		LanguageDetector langdetect = new LanguageDetector(langResFiles);


		StringBuilder searchResult;
		Map<String,String> langTweets = new HashMap<String, String>();

		try {
			String query = req.getParameter("q");
			String annotationmethod = req.getParameter("annotationmethod");
			String language = req.getParameter("lang");
			String extern = "true";
			String maxlinksstring = req.getParameter("maxlinks");
			int maxlinks = 10;
			if(maxlinksstring.equals("no")){
				extern="false";
			}
			if(maxlinksstring.equals("max30")){
				maxlinks = 30;
				
			}
			if(maxlinksstring.equals("max50")){
				maxlinks = 50;
				
			}
			int numtweets = Integer.parseInt(req.getParameter("numtweets"));
			
			
			boolean guessLanguage = false;
			if( language == null || language.equals("auto")){
				guessLanguage = true;
			}

			query = URLEncoder.encode(query, "utf-8");
			System.out.println("SEARCH-QUERY:   " + query);

			URL u = new URL("http://search.twitter.com/search.atom?q=" + query + "&rpp=" + numtweets);
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

			
			Set<String> wikifyResult = new HashSet<String>();;
			
			if (annotationmethod.equals("sum")) {

			//###########################################################
			// Using Wikifier to Annotate SUM of tweets
			//###########################################################
				
				//WIKIFY search feed data
				//build strings containing text of tweets in each language
				String[] split = searchResult.toString().split("</title>");
				String tweet;
				String tweets;
				for (int i = 1; i < split.length - 1; i++) {
					tweet = split[i].split("<title>")[1];
					//get language of tweet
					if (guessLanguage) {
						language = langdetect.detectLanguage(tweet);
					}
					if (langTweets.containsKey(language)) {
						tweets = langTweets.get(language);
					} else {
						tweets = "";
					}
					tweets = tweets + " " + tweet;
					langTweets.put(language, tweets);
				}
			
				//if extern=true get content from referenced websites as string and use in wikifier
				if (extern == null) {
					extern = "false";
				}
				if (extern.equals("true")) {

					//get HTTP-links
					String[] contents = searchResult.toString().split(
							"</content>");

					StringBuilder tweetsHTML = new StringBuilder();

					for (int i = 1; i < contents.length - 1; i++) {
						String[] content = contents[i]
								.split("<content type=\"html\">");
						tweetsHTML.append(" " + content[1]);
					}

					String[] tweetsHTMLSplit = tweetsHTML.toString().split(
							"&lt;a href=\"");
					String[] urlarray = new String[tweetsHTMLSplit.length - 1];

					for (int i = 1; i < tweetsHTMLSplit.length; i++) {
						urlarray[i - 1] = tweetsHTMLSplit[i].toString().split(
								"\"")[0];
					}

					Set<String> urls = new HashSet<String>();

					for (int i = 0; i < urlarray.length; i++) {
						urls.add(urlarray[i]);
					}

					Iterator<String> urliter = urls.iterator();
					//maximum of maxlinks links used
					int count = 0;

					//get content of linked sites
					while (urliter.hasNext() && count < maxlinks) {
						String thisurl = urliter.next().toString();
						//don't use internal twitter links containing javascript which cannot be handled
						if (!thisurl.contains("twitter")) {
							String[] subjects = null;
							try {
								subjects = SubjectExtraction.extract(thisurl);
							} catch (Exception e) {
							}
							if (subjects != null) {
								count = count + 1;
								System.out.println("EXTERNAL URL No.: " + count
										+ " : " + thisurl);
								StringBuilder externalWebsite = new StringBuilder();
								for (String subject : subjects) {
									externalWebsite.append(subject + " ");
									System.out.println("WEBSITE SUBJECT : "
											+ subject);
								}
								String externalLanguage = language;
								if (guessLanguage) {
									externalLanguage = langdetect
											.detectLanguage(externalWebsite
													.toString());
								}
								System.out.println("WEBSITE LANGUAGE: "
										+ externalLanguage);
								if (langTweets.containsKey(externalLanguage)) {
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
				for (String lang : langTweets.keySet()) {
					wikifyResult.addAll(Wikify.startWikify(langTweets.get(lang), lang));
				}
				System.out.println("WIKIFY RESULTS:");
				System.out.println(wikifyResult);
			} else {
				
				//###########################################################
				// Using Wikifier to Annotate SINGLE tweets
				//###########################################################
				
				String[] split = searchResult.toString().split("</title>");
				String[] splitcontent = searchResult.toString().split("</content>");
				String tweet;
				for (int i = 1; i < split.length - 1; i++) {
					tweet = split[i].split("<title>")[1];
					
					//get external content if applicable
					String externalContent = "";
					if (extern == null) {
						extern = "false";
					}
					if (extern.equals("true")) {
						//check if tweet contains links. if so, add URL to hashset
						String tweetcontent = splitcontent[i-1].split("<content type=\"html\">")[1];
						String[] tweetscontentLinks = tweetcontent.toString().split(
								"&lt;a href=\"");
						String[] urlarray = new String[tweetscontentLinks.length - 1];

						for (int j = 1; j < tweetscontentLinks.length; j++) {
							urlarray[j - 1] = tweetscontentLinks[j].toString().split("\"")[0];
						}

						Set<String> urls = new HashSet<String>();

						for (int j = 0; j < urlarray.length; j++) {
							urls.add(urlarray[j]);
						}
						
						//get external content
						Iterator<String> urliter = urls.iterator();
						
						//maximum of maxlinks links used
						int count = 0;

						//get content of linked sites
						while (urliter.hasNext() && count < maxlinks) {
							String thisurl = urliter.next().toString();
							//don't use internal twitter links containing javascript which cannot be handled
							if (!thisurl.contains("twitter")) {
								String[] subjects = null;
								try {
									subjects = SubjectExtraction.extract(thisurl);
								} catch (Exception e) {
								}
								if (subjects != null) {
									count = count + 1;
									StringBuilder externalWebsite = new StringBuilder();
									for (String subject : subjects) {
										externalWebsite.append(subject + " ");
									}
									externalContent = externalContent + " " + externalWebsite;
								}
							}
					}

				}
				
					//add subjects to tweet
					tweet = tweet + externalContent;
					//get language of tweet
					if (guessLanguage) {
						language = langdetect.detectLanguage(tweet);
					}
					wikifyResult.addAll(Wikify.startWikify(tweet, language));
					System.out.println("WIKIFY RESULTS:");
					System.out.println(wikifyResult);
				}
			}
			
			
			//remove unwanted annotations
			wikifyResult.remove("Question_mark");
			wikifyResult.remove("Hypertext_Transfer_Protocol");
			wikifyResult.remove("Dash");

			
			//append DBPedia links to atom feed to include in transformation

			Iterator<String> entityIter = wikifyResult.iterator();
			StringBuffer seeAlso = new StringBuffer();
			
			while(entityIter.hasNext()){
				
				String nextentity = entityIter.next();
				
				Boolean append=true;
				
//				// Check if DBpedia website exists
//				String extResponse = "";
//				
//				BufferedReader extContent = null;
//				
//				try {
//					URL url = new URL("http://dbpedia.org/page/"+nextentity);
//					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//					connection.setRequestMethod("GET");
//					connection.setReadTimeout(10000);
//					connection.connect();
//
//					extContent = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
//					extResponse = connection.getResponseMessage();
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				
//				if (extResponse.equals("OK") && extContent != null) {
//					
//					String line          = "";
//					int cnt = 0;
//					
//					try {
//						while ((line = extContent.readLine()) != null && cnt<100)
//						{
//							if(line.contains("The requested entity is unknown")){
//								append=false;
//								break;
//							}
//							cnt++;
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
				
				if(append){				
				seeAlso.append("<seeAlso>");
				seeAlso.append("<link>");
				seeAlso.append("http://dbpedia.org/page/");
				seeAlso.append(nextentity);
				seeAlso.append("</link>");
				seeAlso.append("</seeAlso>");
				seeAlso.append("\n");
				}

			}
			
			String searchResultString = "";
			
			searchResultString = searchResult.toString().replaceAll("</feed>", "") + seeAlso.toString() + "\n</feed>";
			
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

}
