package edu.kit.ksri.lids.openlids;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import edu.kit.ksri.lids.interfacing.Transformator;
import edu.kit.ksri.lids.model.Variable;

public class TESSCompanyInterface extends HTTPServiceInterface {

	public static String getEndpoint() {
		return "http://tess.openlids.org/company";
	}
	
	public TESSCompanyInterface() {
		this.setTransformator(new Transformator() {

			public String transform(URI uri, String input, Map<Variable,List<String>> params) {
				String result = "";
				String id = uri + "#id";

				String namespace = "http://ontologycentral.com/2010/05/tess/vocab#";
				Logger.getLogger("Transformer").warning("Got URI: " + uri);

				result = "" + "<?xml version=\"1.0\" encoding=\"utf-8\"?><rdf:RDF xmlns=\"" +
				namespace + 
				"\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:skos=\"http://www.w3.org/2008/05/skos#\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\">\n" + 
				"<rdf:Description rdf:about=\"\">\n" +
				"\t<rdfs:comment>Source: Trademark Electronic Search System (http://tess2.uspto.gov/) via Open LIDS (http://openlids.org/).</rdfs:comment>\n" +
				"</rdf:Description>\n" +
				"<foaf:Organization rdf:ID=\"id\">\n";
				
				result += "\t<foaf:name>" + params.get(new Variable("name")).get(0).toString() + "</foaf:name>\n";


				//Pattern p = Pattern.compile("<TR>.*?<TD>[0-9]+</TD>.*?>([0-9]+)</a></TD>.*?<TD>.*?<TD>.*?>(.*?)</a>.*?<TD>.*?<TD>.*?>(.*?)</a>.*?</TR>");

				input = input.replace('\n', ' ');
				System.out.println(input);
				Pattern p = Pattern.compile("<TR>.*?<TD>[0-9]+</TD>.*?([0-9]+)./a>.*?<TD>.*?<TD>.*?([\\w\\s]+)./a>.*?<TD>.*?<TD>.*?([\\w\\s]+)./a>.*?</TR>", Pattern.MULTILINE);
				Matcher m = p.matcher(input);

				while(m.find()) {
					System.out.println("FIND");
					if(m.groupCount() == 3) {	
						result += "\t<trademark><rdf:Description>\n"
							+ "\t\t<serial_number>" + m.group(1) + "</serial_number>\n";
						// rdf:about=\"http://open-lids.appspot.com/tess/trademarks?serial_number=" + m.group(1) + "#trademark\">
						result += "\t\t<word_mark>" + m.group(2) + "</word_mark>\n";
						result += "\t\t<status>" + m.group(3) + "</status>\n";
						result += "\t</rdf:Description></trademark>";
					}

				}
				result += "</foaf:Organization></rdf:RDF>";
			
				return result;
			}
		});
	}



	@Override
	public HTTPRequest createRequest(Map<Variable, List<String>> params) {
		HTTPRequest req = new HTTPRequest();
		try {
			URL redir = new URL("http://tess2.uspto.gov/bin/gate.exe?f=login&p_lang=english&p_d=trmk");
			URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
			com.google.appengine.api.urlfetch.HTTPRequest appReq = new com.google.appengine.api.urlfetch.HTTPRequest(redir, HTTPMethod.GET, FetchOptions.Builder.doNotFollowRedirects());

			HTTPResponse resp = null;

			try {
				resp = fetchService.fetch(appReq);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.getLogger("CreateReq").severe(e.toString());
			} 

			String location = "";
			for(HTTPHeader header : resp.getHeaders()) {
				Logger.getLogger("CreateReq").warning("Header: " + header.getName() + " => " + header.getValue());
				if(header.getName().toLowerCase().equals("location")) {
					location = header.getValue();
				}
			}
			String state = "";
			for(String pair : location.split("&")) {
				Logger.getLogger("CreateReq").warning("Pair: " + pair);
				String p[] = pair.split("=");
				if(p != null && p.length >= 2) {
					if(p[0].equals("state")) {
						state = p[1]; 
					}
				}
			}
			Logger.getLogger(this.getClass().toString()).warning("State: " + state);
			System.out.println("State: " + state);

			req.setUrl(new URL("http://tess2.uspto.gov/bin/showfield"));
			req.addParam("f", "toc");
			req.addParam("state",state);
			req.addParam("p_search","searchstr");
			req.addParam("BackReference","");
			req.addParam("p_L","100");
			req.addParam("p_plural","no");
			req.addParam("p_s_PARA1", params.get(new Variable("name")).get(0));
			req.addParam("p_tagrepl~:","PARA1$ON");
			req.addParam("p_s_PARA2","");
			req.addParam("p_tagrepl~:","PARA2$ALL");
			req.addParam("a_default","search");
			req.addParam("a_search","Submit+Query");
		} catch(Exception e) {
			e.printStackTrace();
			Logger.getLogger("CreateReq").severe(e.toString());
		} 

		return req;
	}

}
