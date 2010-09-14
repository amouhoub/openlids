package org.openlids.query;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.util.CallbackSet;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

public class LookupTest extends TestCase {
	public void testLookup() throws Exception {
		URI uri = new URI("http://sws.geonames.org/2892794/");
		
		URL url = uri.toURL();
		
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("Accept", "application/rdf+xml");
		InputStream is = conn.getInputStream();

		if (conn.getResponseCode() != 200) {
			System.err.println("response code != 200");
			return;
		}

		String encoding = conn.getContentEncoding();
		if (encoding == null) {
			encoding = "ISO_8859-1";
		}

		CallbackSet cb = new CallbackSet();
		
		RDFXMLParser rdfxml = new RDFXMLParser(is, true, true, url.toString(), cb);
		
		for (Node[] nx : cb.getSet()) {
			System.out.println(Nodes.toN3(nx));
		}
	}
}
