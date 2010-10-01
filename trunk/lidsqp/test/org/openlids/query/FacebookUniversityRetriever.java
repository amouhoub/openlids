package org.openlids.query;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FacebookUniversityRetriever {
	
	public static void main(String args[]) {
		JSONParser parser = new JSONParser();
		
		int i = 0;
		
		try {

			FileOutputStream out = new FileOutputStream("/Users/ssp/Documents/w/Code/openlids/lidsqp/eval_fb/unilist.rdf");
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter ch = factory.createXMLStreamWriter(out, "utf-8");
			ch.writeStartDocument("utf-8", "1.0");
			ch.writeStartElement("rdf:RDF");
			ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			ch.writeNamespace("foaf", "http://xmlns.com/foaf/0.1/");
			//ch.writeNamespace("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
			//ch.writeNamespace("v","http://www.w3.org/2006/vcard/ns#");
			ch.writeNamespace("og","http://ogp.me/ns#");

			ch.writeStartElement("rdf:Description");
			ch.writeAttribute("rdf:ID", "universities");

			
			boolean hasNext = true;
			
			while(hasNext) {
				URL url = new URL("https://graph.facebook.com/search?q=university&type=page&limit=25&offset=" + i);

				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				InputStream is = conn.getInputStream();

				String encoding = conn.getContentEncoding();
				if (encoding == null) {
					encoding = "ISO_8859-1";
				}

				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

					JSONObject page = (JSONObject) parser.parse(new InputStreamReader(is));
					List unis = (List) page.get("data");
					for(Object obj : unis) {
						JSONObject uni = (JSONObject) obj;
						ch.writeStartElement("foaf:topic");
						ch.writeStartElement("rdf:Description");
						ch.writeStartElement("og:id");
						ch.writeCharacters(uni.get("id").toString());
						ch.writeEndElement();
						ch.writeStartElement("foaf:name");
						ch.writeCharacters(uni.get("name").toString());
						ch.writeEndElement();
						ch.writeEndElement();
						ch.writeEndElement();
					}
					hasNext = false;
					if(page.containsKey("paging")) {
						if (((JSONObject) page.get("paging")).containsKey("next") ) {
							hasNext = true;
						}
					}
				}	
				i += 25;
				System.out.println("Nr: " + i);
			}
			
			ch.writeEndElement();
			ch.writeEndDocument();
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

}
