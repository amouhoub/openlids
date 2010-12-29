package edu.kit.aifb.lids.twitterwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;

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
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/rdf+xml");

		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();
		//OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

		StringReader sr = null;

		try {
			Map<String, String[]> params = req.getParameterMap();

			String url = Listener.generateURL("http://search.twitter.com/search.atom", params);

			URL u = new URL(url);

			_log.info("url: " + u);

			if (sr == null) {
				HttpURLConnection conn = (HttpURLConnection)u.openConnection();

				if (conn.getResponseCode() != 200) {
					resp.sendError(conn.getResponseCode(), u + ": " + Listener.streamToString(conn.getErrorStream()));
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

				sr = new StringReader(str);
			}

			Transformer t = (Transformer)ctx.getAttribute(Listener.SEARCH);

			resp.setHeader("Cache-Control", "public");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			resp.setHeader("Expires", Listener.RFC822.format(c.getTime()));

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
}
