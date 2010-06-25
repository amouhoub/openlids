package org.openlids.streaming;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.openlids.whohoo.InterlidsTest;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.ontologycentral.webquery.query.DatasetGraphStream;
import com.ontologycentral.webquery.query.DatasetStream;
import com.ontologycentral.webquery.query.GraphStream;

public class StreamTest extends TestCase {
	public static int BUFSIZE = 10000;

	public void testQuery() throws Exception {
		InputStream sin = new FileInputStream("files/btc-2010/lids.rq");
		String queryString = InterlidsTest.streamToString(sin);

		Query query = QueryFactory.create(queryString) ;
		
		InputStream din = new GZIPInputStream(new FileInputStream("files/btc-2010/btc-2010-chunk-000.gz"));
		NxParser nxp = new NxParser(din);
		
		List<Node[]> data = new ArrayList<Node[]>();
		
		GraphStream go = new GraphStream(data);
		DatasetGraphStream dgo = new DatasetGraphStream(go);
		DatasetStream dowl = new DatasetStream(dgo);
		
		int i = 0;
		int count = 0;
		long time = System.currentTimeMillis();
		
		while (nxp.hasNext()) {
			Node[] nx = nxp.next();
			data.add(nx);
			i++;
			
			if ((i % LoadAllTest.BITSSIZE) == 0) {
				long time1 = System.currentTimeMillis();

				System.err.println("query over " + i + " triples evaluated  " + count + " results, in " + (time1-time) + " ms");

			}
			
			if ((i % BUFSIZE) == 0) {
				QueryExecution engine = QueryExecutionFactory.create(query, dowl) ;

				Iterator<QuerySolution> results = engine.execSelect() ;
				for ( ; results.hasNext() ; ) {
					QuerySolution soln = results.next() ;
					count++;
					//System.out.println(soln.toString());
				}
				
				//System.out.println(data.size());
				
				engine.close() ;

				data = new ArrayList<Node[]>(data.subList(data.size()-(int)((float)BUFSIZE*.05), data.size()-1));
				go = new GraphStream(data);
				dgo = new DatasetGraphStream(go);
				dowl = new DatasetStream(dgo);
			}
		}
		
		din.close();


		/*		
		Model m = ModelFactory.createDefaultModel();
		engine.execConstruct(m);
		m.write(System.out,"N-TRIPLE");
		*/
		
		long time1 = System.currentTimeMillis();

		System.err.println("query evaluated in " + (time1-time) + " ms");
	}
}
