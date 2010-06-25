package org.openlids.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.openlids.whohoo.InterlidsTest;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class LoadAllTest extends TestCase {
	public static int BITSSIZE = 50000;
	
	public void testQuery() throws Exception {
		InputStream sin = new FileInputStream("files/btc-2010/lids.rq");
		String queryString = InterlidsTest.streamToString(sin);

		Query query = QueryFactory.create(queryString) ;
		
		File dir = new File("files/btc-2010/bits");

		String[] bits = dir.list();
		
		int count = 0;

		for (int i = 0 ; i < bits.length; i++) {
			long time = System.currentTimeMillis();

			Model m = ModelFactory.createDefaultModel();

			for (int j = 0; j <= i; j++) {
				InputStream din = new GZIPInputStream(new FileInputStream("files/btc-2010/bits/" + bits[j]));
				m.read(din, "http://example.org", "N-TRIPLES");
				din.close();
			}

			QueryExecution engine = QueryExecutionFactory.create(query, m);

			Iterator<QuerySolution> results = engine.execSelect() ;
			for ( ; results.hasNext() ; ) {
				QuerySolution soln = results.next() ;
				count++;
				//System.out.println(soln.toString());
			}
			engine.close() ;
			
			long time1 = System.currentTimeMillis();

			System.err.println("query over " + (i+1)*BITSSIZE + " triples " + count + " results, evaluated in " + (time1-time) + " ms");
		}
	}
}
