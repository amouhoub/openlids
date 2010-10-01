package org.openlids.query;

import java.io.FileWriter;
import java.util.Collection;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;

public class LIDSFacebookTest extends TestCase {
	

	static String prefixes = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
							 "PREFIX dc: <http://purl.org/dc/elements/1.1/> " + 
							 "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> " +
							 "PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							 "PREFIX v: <http://www.w3.org/2006/vcard/ns#> " + 
							 "PREFIX og: <http://ogp.me/ns#> ";
	
	//static String baseServer = "http://km.aifb.kit.edu/services/geowrap/";
	static String baseServer = "http://localhost:8888/";
	
	static String findNearbyLIDS = prefixes + "CONSTRUCT { ?point foaf:based_near ?p } " +
											  "FROM <" + baseServer + "findNearby> " +
											  "WHERE { ?point geo:lat ?lat . ?point geo:long ?lng }";
	
	static String alsoNearbyLIDS = prefixes + "CONSTRUCT { ?spatial_entity foaf:based_near ?p } " +
											  "FROM <" + baseServer + "alsoNearby> " +
											  "WHERE { ?spatial_entity foaf:based_near ?point . ?point geo:lat ?lat . ?point geo:long ?lng }";
	
	static String geocodeLIDS = prefixes + "CONSTRUCT { ?entity foaf:based_near ?p . ?p geo:lat ?lat . ?p geo:long ?lng } " +
	  									   "FROM <" + baseServer + "geocode> " +
	  									   "WHERE { ?entity v:adr ?adr . ?adr v:street-address ?street . ?adr v:locality ?town . ?adr v:country-name ?country }";
	
	static String facebookLIDS = prefixes + "CONSTRUCT { ?page foaf:name ?name } " +
											"FROM <" + baseServer + "facewrap> " +
											"WHERE { ?page og:id ?facebookid } ";
	
	static String dbpediaStudNRLIDS = prefixes + "CONSTRUCT { ?university <http://dbpedia.org/ontology/numberOfStudents> ?n } " +
												 "FROM <" + baseServer + "dbpediaStudNR> " +
												 "WHERE { ?university foaf:homepage ?homepage } ";


	public void testMuseums() {
		if(3==2+1)
		return;
		String queryStr = prefixes + "SELECT ?name ?lat ?long WHERE { <file:/Users/ssp/Documents/w/Code/openlids/lidsqp/eval_fb/lists.rdf#museums> foaf:topic ?m . ?m foaf:name ?name . ?m foaf:based_near ?p . ?p geo:lat ?lat . ?p geo:long ?long }";
				
		QueryExecutor qMuseums = new QueryExecutor(LIDSStrategyBruteForce.getFactory());
		qMuseums.addLIDS(facebookLIDS);
		qMuseums.addLIDS(geocodeLIDS.replace("8888", "8889"));
		Collection<Node[]> resultsMuseums = qMuseums.execQueryWithLIDS(queryStr);
		TreeSet<Node[]> uniqKnow = new TreeSet<Node[]>(NodeComparator.NC);
		uniqKnow.addAll(resultsMuseums);
		for(Node[] museum : uniqKnow) {
			System.out.println("" + Nodes.toN3(museum));
		}
		try {
			FileWriter out = new FileWriter("/Users/ssp/Documents/w/Code/openlids/lidsqp/eval_fb/listsresolved.n3");

			for(Node[] nx : qMuseums.dataSet._data) {
				out.write(Nodes.toN3(nx) + "\n");
			}
			out.write("    \n");
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testUniversities() {
		String queryStr = prefixes + "SELECT ?name ?likes ?students WHERE { <file:/Users/ssp/Documents/w/Code/openlids/lidsqp/eval_fb/unilist.rdf#universities> foaf:topic ?u . ?u foaf:name ?name . ?u og:fan_count ?likes . ?u <http://dbpedia.org/ontology/numberOfStudents> ?students }";
		
		long startTime = System.currentTimeMillis();
		QueryExecutor qUnis = new QueryExecutor(LIDSStrategyBruteForce.getFactory());
		qUnis.addLIDS(facebookLIDS);
		qUnis.addLIDS(dbpediaStudNRLIDS);
		Collection<Node[]> resultsUnis = qUnis.execQueryWithLIDS(queryStr);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Total Query Execution Time: " + (endTime - startTime));
		System.out.println("URI Retrieval Time: " + qUnis.dataSet.uriRetrievalTimes);
		
		TreeSet<Node[]> uniqKnow = new TreeSet<Node[]>(NodeComparator.NC);
		uniqKnow.addAll(resultsUnis);
		for(Node[] uni : uniqKnow) {
			System.out.println("" + Nodes.toN3(uni));
		}
		
		try {
			FileWriter out = new FileWriter("/Users/ssp/Documents/w/Code/openlids/lidsqp/eval_fb/unis.n3");

			for(Node[] nx : qUnis.dataSet._data) {
				out.write(Nodes.toN3(nx) + "\n");
			}
			out.write("    \n");
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}