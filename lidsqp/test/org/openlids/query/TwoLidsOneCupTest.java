package org.openlids.query;

import java.util.Collection;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.FOAF;
import org.semanticweb.yars.nx.namespace.GEO;

public class TwoLidsOneCupTest extends TestCase {
	Resource ADR = new Resource("http://example.org/ADR");
	
	String GEOCODE = "CONSTRUCT { ?point " + GEO.LAT.toN3() + " ?lat ; " + GEO.LONG.toN3() + " ?lng }\nFROM <http://km.aifb.kit.edu/services/geowrap/geocode>\nWHERE { ?point " + ADR.toN3() + " ?address . }";
	String FINDWIKI = "CONSTRUCT { ?point " + FOAF.BASED_NEAR.toN3() + " ?feature }\nFROM <http://km.aifb.kit.edu/services/geowrap/findNearbyWikipedia>\nWHERE { ?point " + GEO.LAT.toN3() + " ?lat ; " + GEO.LONG.toN3() + " ?lng . }";
	
	String query = "SELECT ?p ?feat WHERE { ?p <" + GEO.NS + "location> ?loc . ?loc <http://xmlns.com/foaf/0.1/based_near> ?feat }";
	
	public void testTwo() {
		Node[] triple = new Node[] { new Resource("http://example.org/kit"), ADR, new Literal("Englerstr.11,Karlsruhe,Germany") };

		System.out.println(Nodes.toN3(triple));
		System.out.println();
		System.out.println(GEOCODE);
		System.out.println();
		System.out.println(FINDWIKI);	
		
		QueryExecutor qe = new QueryExecutor(LIDSStrategyBruteForce.getFactory());
		qe.dataSet._data.add(triple);
		qe.addLIDS(GEOCODE);
		qe.addLIDS(FINDWIKI);
		
		Collection<Node[]> results = qe.execQueryWithLIDS(query);
		for(Node[] res : results) {
			System.out.println(Nodes.toN3(res));
		}
		
		System.out.println("data:");
		for(Node[] t : qe.dataSet._data) {
			System.out.println(Nodes.toN3(t));
		}
		
		
	}
}
