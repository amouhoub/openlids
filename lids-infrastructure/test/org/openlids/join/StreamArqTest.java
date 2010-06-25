package org.openlids.join;

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.openlids.whohoo.InterlidsTest;
import org.semanticweb.yars.nx.parser.NxParser;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder;

public class StreamArqTest extends TestCase {
	public void testQuery() throws Exception {
		FileInputStream sin = new FileInputStream("files/geo/lids.rq");
		String queryString = InterlidsTest.streamToString(sin);

		long time = System.currentTimeMillis();

		Query query = QueryFactory.create(queryString) ;
		
		NxParser nxp = new NxParser(new FileInputStream("files/geo/data.nt"));

		StageGeneratorStream sgen = new StageGeneratorStream(nxp);

		StageBuilder.setGenerator(ARQ.getContext(), sgen) ;

		QueryExecution engine = QueryExecutionFactory.create(query, ModelFactory.createDefaultModel()) ;

		try {
			Model m = ModelFactory.createDefaultModel();
			m = engine.execConstruct();
			
			System.out.println(m);

//			Iterator<QuerySolution> results = engine.execSelect() ;
//			for ( ; results.hasNext() ; ) {
//				QuerySolution soln = results.next() ;
//				System.out.println(soln);
//			}
		} finally {
			engine.close() ;
		}

		long time1 = System.currentTimeMillis();

		System.err.println("query evaluated in " + (time1-time) + " ms");

	}
}
