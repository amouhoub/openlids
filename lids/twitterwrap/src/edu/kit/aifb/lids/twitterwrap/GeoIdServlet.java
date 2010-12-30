package edu.kit.aifb.lids.twitterwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
public class GeoIdServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/rdf+xml");

		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();
		//OutputStreamWriter osw = new OutputStreamWriter(os , "UTF-8");

		Map<String, String[]> params = req.getParameterMap();

		String path = req.getServletPath();

		_log.info("path: " + path);
		System.out.println("path: " + path);
		String pathinfo = req.getPathInfo();

		String url = Listener.generateURL("http://api.twitter.com/1" + path + pathinfo + ".json", params);

		URL u = new URL(url);

		_log.info("url: " + u);
		System.out.println("url: " + u);

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

		String json = sb.toString();

		resp.setHeader("Cache-Control", "public");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		resp.setHeader("Expires", Listener.RFC822.format(c.getTime()));

		// attributes might contain number:text properties, which are not allowed in XML
		// e.g "174368:admin_order_id", "174368:id"
		// strip 'em out: "attributes":{},
		json = json.replaceAll("\"attributes\":\\{.*?\\},", "");
		
		_log.info("json after cleansing: " + json);

		XMLSerializer serializer = new XMLSerializer(); 
		JSON jsob = JSONSerializer.toJSON(json); 
		String xml = serializer.write(jsob);

//		PrintWriter pw = new PrintWriter(os);
//		pw.println(xml);
//		pw.close();

		Transformer t = (Transformer)ctx.getAttribute(Listener.GEOID);

		_log.info("xml: " + xml);
		
		StringReader sr = new StringReader(xml);
		try {
			t.transform(new StreamSource(sr), new StreamResult(os));
		} catch (TransformerException e) {
			e.printStackTrace(); 
			resp.sendError(500, e.getMessage());
		}
		os.close();
	}
}
