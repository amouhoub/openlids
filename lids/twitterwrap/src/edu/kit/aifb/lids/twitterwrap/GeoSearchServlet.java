package edu.kit.aifb.lids.twitterwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

@SuppressWarnings("serial")
public class GeoSearchServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/rdf+xml");

		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();
		//OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

		Map<String, String[]> params = req.getParameterMap();

		String path = req.getServletPath();

		_log.info("path: " + path);

		String url = Listener.generateURL("http://api.twitter.com/1" + path + ".xml", params);

		URL u = new URL(url);

		_log.info("url: " + u);

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

		resp.setHeader("Cache-Control", "public");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		resp.setHeader("Expires", Listener.RFC822.format(c.getTime()));

		XMLSerializer serializer = new XMLSerializer(); 
		JSON json = JSONSerializer.toJSON(str); 
		String xml = serializer.write(json);
		
		Transformer t = (Transformer)ctx.getAttribute(Listener.GEOSEARCH);

		StringReader sr = new StringReader(xml);
		
		try {
			StreamSource ssource = new StreamSource(sr);
			StreamResult sresult = new StreamResult(os);
			t.transform(ssource, sresult);
		} catch (TransformerException e) {
			e.printStackTrace(); 
			resp.sendError(500, e.getMessage());
		}
		
		os.close();
	}
}