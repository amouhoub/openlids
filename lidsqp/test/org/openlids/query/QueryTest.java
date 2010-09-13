package org.openlids.query;

import java.util.Iterator;

import org.openlids.query.arq.DatasetGraphWeb;
import org.openlids.query.arq.DatasetWeb;
import org.openlids.query.arq.GraphWeb;

import junit.framework.TestCase;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;

public class QueryTest extends TestCase {
	static String[] QUERIES = new String[] {
			"SELECT * WHERE { <http://harth.org/andreas/foaf#ah> ?p ?o . }",
			"SELECT ?sa WHERE { <http://harth.org/andreas/foaf#ah> <http://www.w3.org/2002/07/owl#sameAs> ?sa . }",
			"SELECT ?name WHERE { <http://harth.org/andreas/foaf#ah> <http://xmlns.com/foaf/0.1/knows> ?knows . ?knows <http://xmlns.com/foaf/0.1/name> ?name . } ",
			"SELECT ?label WHERE { <http://harth.org/andreas/foaf#ah> <http://xmlns.com/foaf/0.1/topic_interest> ?int . ?int <http://www.w3.org/2000/01/rdf-schema#label> ?label .  FILTER regex(?label, \"a\", \"i\")  } "
	};
	
	static long _lodq[] = new long[QUERIES.length];
	static long _swc[] = new long[QUERIES.length];
	
	public void testQuery() throws Exception {
		int i = 0;
		
		for (String queryString : QUERIES) {
			long time = System.currentTimeMillis();

			Query query = QueryFactory.create(queryString) ;

			GraphWeb go = new GraphWeb();
			DatasetGraphWeb dgo = new DatasetGraphWeb(go);
			DatasetWeb dowl = new DatasetWeb(dgo);

			QueryExecution engine = QueryExecutionFactory.create(query, dowl) ;

			try {
				Iterator<QuerySolution> results = engine.execSelect() ;
				for ( ; results.hasNext() ; ) {
					QuerySolution soln = results.next() ;
					System.out.println(soln);
				}
			} finally {
				engine.close() ;
			}

			long time1 = System.currentTimeMillis();

			System.err.println("query evaluated in " + (time1-time) + " ms");
			
			_lodq[i] = (time1-time);
			i++;
		}
	}
		
	public void testX() {
		for (int i = 0; i < _lodq.length; i++) {
			System.out.println("Query " + i + ": ours " + _lodq[i] + " ms"); //vs theirs " + _swc[i] + " ms");
		}
	}
}