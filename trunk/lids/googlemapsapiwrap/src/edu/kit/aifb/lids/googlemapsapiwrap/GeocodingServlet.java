package edu.kit.aifb.lids.googlemapsapiwrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
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
public class GeocodingServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ServletContext ctx = getServletContext();

		OutputStream os = resp.getOutputStream();

		try {
			Map<String, String[]> params = req.getParameterMap();

			String path = req.getServletPath();

			_log.info("path: " + path);

                        if(!params.containsKey("sensor")) {
                            params.put("sensor", new String[] { "false" });
                        }
                        if(params.containsKey("lat") && params.containsKey("lng")) {
                            params.put("latlng", new String[] { "" + params.get("lat")[0] + "," + params.get("lng")[0] });
                            params.remove("lat");
                            params.remove("lng");
                        }

			String url = Listener.generateURL("http://maps.googleapis.com/maps/api/geocode/xml", params);

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

			StringReader sr = new StringReader(str);

			Transformer t = (Transformer)ctx.getAttribute(Listener.GEOCODING);

			t.setParameter("address", URLDecoder.decode(req.getParameter("address"), "utf-8"));
			resp.setContentType("application/rdf+xml");

			resp.setHeader("Cache-Control", "public");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			resp.setHeader("Expires", Listener.RFC822.format(c.getTime()));

			try {
				t.transform(new StreamSource(sr), new StreamResult(os));
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
