package com.ontologycentral.twittersearchwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.yars.nx.DateTimeLiteral;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NumericLiteral;
import org.semanticweb.yars.nx.Resource;

public class Wikifier {
	Logger _log = Logger.getLogger(this.getClass().getName());

	String _uri;
	Resource _source;
	String _uagent;

	static String DELIM = " ------------ \n";

	public Wikifier(String uri, String uagent) {
		_uri = uri;
		_uagent = uagent;

		_source = new Resource(uri);
	}

	/**
	 * Should return a name,value pair (i.e. retain the predicate for which the
	 * entity was detected)
	 * 
	 * @param subj
	 * @param predobjs
	 * @return
	 */
	Set<String> wikify(Node subj, Set<Node[]> predobjs) {
		Set<String> result = new HashSet<String>();

		try {
			StringBuffer sb = new StringBuffer();
			for (Node[] po : predobjs) {
				Node o = po[1];

				if (o instanceof DateTimeLiteral) {
					_log.fine("date");
				} else if (o instanceof NumericLiteral) {
					_log.fine("number");
				} else if (o instanceof Literal) {
					sb.append(o.toString());
					sb.append(DELIM);
				} else if (o instanceof Resource) {
					_log.fine("resource");
				}
			}

			String payload = sb.toString();
			_log.fine(payload);

			URL u = new URL(_uri);

			// &minProbability=0.25
			String data = "task=wikify&wrapInXml=false&repeatMode=0&source="
					+ URLEncoder.encode(payload, "utf-8");

			URLConnection conn = u.openConnection();
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			conn.setRequestProperty("User-agent", _uagent);

			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(data);
			wr.flush();

			String encoding = "utf-8";
			if (conn.getContentEncoding() != null) {
				encoding = conn.getContentEncoding();
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), encoding));

			sb = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}
			in.close();

			String html = sb.toString().trim();

			result = getIDs(html);
		} catch (MalformedURLException e) {
			_log.info

			(subj.toN3() + " " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			_log.info

			(subj.toN3() + " " + e.getMessage());
		} catch (IOException e) {
			_log.info

			(subj.toN3() + " " + e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			_log.info

			(subj.toN3() + " " + e.getMessage());
		} catch (IllegalArgumentException e) {
			_log.info

			(subj.toN3() + " " + e.getMessage());
		}

		return result;
	}

	static Set<String> getIDs(String str) {
		Set<String> result = new HashSet<String>();

		Pattern pattern = Pattern.compile("\\[\\[.*?\\]\\]");

		Matcher m = pattern.matcher(str);
		while (m.find()) {
			String concept = m.group();

			// @@@ what to do when nothing matches?
			// java.lang.IndexOutOfBoundsException: No group 1
			if (concept.contains("|")) {
				String wiki = concept.substring(2, concept.indexOf("|"));
				wiki = wiki.replace(' ', '_');
				result.add(wiki);
			} else {
				String wiki = concept.substring(2, concept.indexOf("]"));
				wiki = wiki.replace(' ', '_');
				result.add(wiki);
			}
		}

		return result;
	}

	static String getWikipediaLinks(String str, String wikiuri) {
		Pattern pattern = Pattern.compile("\\[\\[.*?\\]\\]");

		StringBuffer sb = new StringBuffer();

		Matcher m = pattern.matcher(str);
		while (m.find()) {
			String concept = m.group();

			// @@@ what to do when nothing matches?
			// java.lang.IndexOutOfBoundsException: No group 1
			if (concept.contains("|")) {
				String wiki = concept.substring(2, concept.indexOf("|"));
				wiki = wiki.replace(' ', '_');
				String text = concept.substring(concept.indexOf("|") + 1,
						concept.length() - 2);
				m.appendReplacement(sb, "<a href=\"" + wikiuri + wiki + "\">"
						+ text + "</a>");
			} else {
				String wiki = concept.substring(2, concept.indexOf("]"));
				String text = wiki;
				wiki = wiki.replace(' ', '_');
				m.appendReplacement(sb, "<a href=\"" + wikiuri + wiki + "\">"
						+ text + "</a>");
			}
		}
		m.appendTail(sb);

		return sb.toString();
	}

	public Node getSource() {
		return _source;
	}
}