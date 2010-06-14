package org.openlids.feedwrap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedwrapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)	throws IOException {
//		if (req.getServerName().contains("appspot.com")) {
//			try {
//				URI re = new URI("http://feedwrap.openlids.org/" + req.getRequestURI() + "?" + req.getQueryString());
//				re = re.normalize();
//				resp.sendRedirect(re.toString());
//			} catch (URISyntaxException e) {
//				resp.sendError(500, e.getMessage());
//			}
//			return;
//		}

		OutputStream os = resp.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

		String uri = req.getParameter("uri");

		if (uri == null) {
			resp.sendError(400, "please supply uri parameter");
			return;
		}

		URL url = new URL(uri);
		
		resp.setContentType("application/rdf+xml");

		try {
			SyndFeedInput input = new SyndFeedInput();
			URLConnection conn = url.openConnection();
			SyndFeed feed = input.build(new XmlReader(conn));

			OutputFeed of = new OutputFeed();
			of.output(feed, osw, true);
		} catch (FeedException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}
}
