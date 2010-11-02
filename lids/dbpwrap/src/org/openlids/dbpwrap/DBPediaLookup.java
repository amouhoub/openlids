package org.openlids.dbpwrap;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.simple.parser.JSONParser;


@SuppressWarnings("serial")
public class DBPediaLookup extends HttpServlet {

	JSONParser parser = new JSONParser();

	public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);


	static String namespaces = "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
	"    xmlns:foaf='http://xmlns.com/foaf/0.1/'\n" +
	"    xmlns:geo='http://www.w3.org/2003/01/geo/wgs84_pos#'\n" +
	"    xmlns:v='http://www.w3.org/2006/vcard/ns#'\n" +
	"    xmlns:og='http://ogp.me/ns#'";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {


		String homepage = req.getParameter("homepage");

		if (homepage == null) {
			resp.sendError(400, "please supply homepage parameter.");
			return;
		}

		resp.setContentType("application/rdf+xml");
		OutputStream out = resp.getOutputStream();

		resp.setHeader("Cache-Control", "public");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		resp.setHeader("Expires", RFC822.format(cal.getTime()));

		try {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter ch;
		
			ch = factory.createXMLStreamWriter(out, "utf-8");
		
		ch.writeStartDocument("utf-8", "1.0");
		ch.writeStartElement("rdf:RDF");
		ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		ch.writeNamespace("dbp","http://dbpedia.org/ontology/");

		ch.writeStartElement("rdf:Description");
		ch.writeAttribute("rdf:about", "http://dbpwrap.openlids.org/DBPediaStudNR?homepage=" + URLEncoder.encode(homepage,"utf-8") + "#university");

		String nr1 = NRHash.hpToNr.get(homepage);
		int inr1 = 0;
		try {
			inr1 = Integer.parseInt(nr1);
		} catch(Exception e) {
		}
		String nr2 = NRHash.hpToNr.get(homepage + "/");
		int inr2 = 0;
		try {
			inr2 = Integer.parseInt(nr2);
		} catch(Exception e) {
		}

		String nr3 = NRHash.hpToNr.get(homepage.substring(0,homepage.length()-1));
		int inr3 = 0;
		try {
			inr3 = Integer.parseInt(nr3);
		} catch(Exception e) {
		}

		if(inr2 > inr1) {
			nr1 = nr2;
			inr1 = inr2;
		}
		if(inr3 > inr1) {
			nr1 = nr3;
			inr1 = inr3;
		}

		if(nr1 != null) {
			ch.writeStartElement("dbp:numberOfStudents");
			ch.writeCharacters(nr1);
			ch.writeEndElement();
		}

		ch.writeEndDocument();
		
		} catch (XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	} 

	//		resp.setContentType("text/plain");
	//		resp.getWriter().println("Hello, world");
}
