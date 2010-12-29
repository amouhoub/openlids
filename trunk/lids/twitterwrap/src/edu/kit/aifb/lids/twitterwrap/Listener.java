package edu.kit.aifb.lids.twitterwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class Listener implements ServletContextListener {
	public static String USERTIMELINE = "usertimeline";
	public static String SEARCH = "search";
	public static String TWEETS = "tweets";
	public static String USERS = "users";
	public static String FRIENDS = "friends";

	public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();

		TransformerFactory tf = TransformerFactory.newInstance(); //"org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader() ); 

		//System.out.println(ctx.getRealPath("/WEB-INF/timeline.xsl"));
		
		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/timeline.xsl")));
			ctx.setAttribute(USERTIMELINE, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/search.xsl")));
			ctx.setAttribute(SEARCH, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/tweets.xsl")));
			ctx.setAttribute(TWEETS, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/users.xsl")));
			ctx.setAttribute(USERS, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/friends.xsl")));
			ctx.setAttribute(FRIENDS, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public void contextDestroyed(ServletContextEvent event) {
		//ServletContext ctx = event.getServletContext();
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

	public static String generateURL(String url, Map<String, String[]> params) throws UnsupportedEncodingException {
		boolean first = true;

		StringBuilder sb = new StringBuilder();
		sb.append(url);

		for (Iterator<String> it = params.keySet().iterator(); it.hasNext(); )  {
			String name = it.next();
			String[] values = params.get(name);

			for (String value : values) {
				if (first) {
					first = false;
					sb.append("?");
				} else {
					sb.append("&");
				}

				sb.append(name);
				sb.append("=");
				sb.append(URLEncoder.encode(value, "utf-8"));
			}
		}

		return sb.toString();
	}
}