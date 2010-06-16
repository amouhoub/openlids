package org.openlids.whohoo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.openlids.interfacing.ServiceAnnotator;
import org.openlids.parser.ServiceParser;
import org.openlids.parser.ServiceParserJena;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class InterlidsTest extends TestCase {
	public void testInterlids() throws Exception {
		long time = System.currentTimeMillis();
		
		FileInputStream fin = new FileInputStream("files/ssp-foaf.nt");

		FileInputStream sin = new FileInputStream("files/lids.rq");
		String q = InterlidsTest.streamToString(sin);

		ServiceParser sp = new ServiceParserJena();

		ServiceAnnotator sas = new ServiceAnnotator(sp.parseServiceDescription(q));
		
		Model m = ModelFactory.createDefaultModel();
		m.read(fin, "http://example.org/", "N3");

		m = sas.annotate(m);

		m.write(System.out,"N-TRIPLE");
		
		long time1 = System.currentTimeMillis();

		System.err.println("time elapsed " + (time1-time) + " ms");
	}

	public static String streamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();

		if (is != null) {
			String line;

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
		}

		return sb.toString();
	}
}
