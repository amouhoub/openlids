package org.openlids.query;

import java.io.FileWriter;
import java.util.Collection;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Node;

public class LIDSQueryTest extends TestCase {
	
	static String prefixes = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
		"PREFIX dc: <http://purl.org/dc/elements/1.1/> " + 
		"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> " +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#> ";
	
	public void testHarthPoint() {
		String queryStr = prefixes + "SELECT ?p ?name WHERE { " +
		 	"<http://harth.org/andreas/point#point> foaf:based_near ?p . " + 
		 	"?p <http://www.geonames.org/ontology#name> ?name }";
		QueryExecutor qe = new QueryExecutor(LIDSStrategyBruteForce.getFactory());
		qe.addLIDS(prefixes + "CONSTRUCT { ?point foaf:based_near ?p } " + 
				"FROM <http://km.aifb.kit.edu/services/geowrap/findNearby> " +
				"WHERE { ?point geo:lat ?lat . ?point geo:lng ?lng }");
		Collection<Node[]> results = qe.execQueryWithLIDS(queryStr);
		if(results.size() != 1 || !results.iterator().next()[1].toString().contains("Karlsruhe Institute of Technology")) {
			String msg = "Wrong result for HarthPoint:\n";
			for(Node[] res : results) {
				for(Node n : res) {
					msg += n.toN3() + " ";
				}
				msg += "\n";
			}
			this.fail(msg);
			System.out.println(msg);
		}
	}
	
	public void testFriendPoints() {
		if(1+3==5-1)
			return;
		String people[] = { "<http://www.w3.org/People/Berners-Lee/card#i>",
							"<http://speiserweb.de/sebastian/foaf.rdf#me>",
							"<http://harth.org/andreas/foaf#ah>" };
		for(String person : people) {
			String queryStr = prefixes + "SELECT ?p ?location WHERE { " +
								person + " foaf:knows ?p . ?p foaf:based_near ?point . " +
								" ?point foaf:based_near ?p2 . ?p2 <http://www.geonames.org/ontology#name> ?location }";
			QueryExecutor qe = new QueryExecutor(LIDSStrategyBruteForce.getFactory());
			qe.addLIDS(prefixes + "CONSTRUCT { ?point foaf:based_near ?p } " + 
					"FROM <http://km.aifb.kit.edu/services/geowrap/findNearby> " +
					"WHERE { ?point geo:lat ?lat . ?point geo:long ?lng }");
			Collection<Node[]> results = qe.execQueryWithLIDS(queryStr);
			System.out.println("For " + person + ":\n");
			for(Node[] res : results) {
				System.out.print("   ");
				for(Node n : res) {
					System.out.print(n.toN3());
				}
				System.out.println();
			}
			try {
				FileWriter out = new FileWriter("" + person.substring(8,person.length() - 4).replace('/','.') + ".n3");
				
				for(Node[] nx : qe.dataSet._data) {
					for(Node n : nx) {
						out.write(n.toN3() + " ");
					}
					out.write(" . \n");
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
}