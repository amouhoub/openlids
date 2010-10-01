package org.openlids.query;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.NumericLiteral;
import org.semanticweb.yars.nx.Resource;

public class BenchmarkTest extends TestCase {
	int N = 5;
	
	public void testCreateData() {
		for(N=5;N<100;N+=5) {
			System.out.println("Relevant for N = " + N);
			doIt(LIDSStrategyRelevant.getFactory());
			System.out.println("Bruteforce for N = " + N);
			doIt(LIDSStrategyBruteForce.getFactory());
		}
	}
		
	public void doIt(LIDSStrategyFactory fac) {
		//QueryExecutor qe = new QueryExecutor(LIDSStrategyBruteForce.getFactory());
		QueryExecutor qe = new QueryExecutor(fac); 
				
		for (int i = 0; i < N; i++) {
			Node[] nx = new Node[] { new Resource("http://example.org/resource1"), new Resource("http://example.org/pred-" + i), new NumericLiteral(i) };
			qe.dataSet._data.add(nx);	
			// System.out.println(Nodes.toN3(nx));
			
			
		}
		
//		for (int i = 0; i < N; i++) {
//			System.out.println("http://example.org/pred-" + i + " " + i+1);
//		}
		
		int i = 0;
		String str = new String();
		List<String> services = generate(str, i);
		
		for(String service : services) {
			qe.addLIDS(service);
			//System.out.println("Service: " + service);
		}
		
//		Collection<Node[]> results = qe.execQueryWithLIDS("SELECT ?o WHERE { <http://example.org/resource1> <http://example.org/caaaa0a1a2a3a4> ?o }");
		Collection<Node[]> results = qe.execQueryWithLIDS("SELECT ?o WHERE { <http://example.org/resource1> " + getLastPredicateName(str,i) + " ?o }");
		
		
//		System.out.println("dataset:");
//		for(Node[] nx : qe.dataSet._data) {
//			System.out.println(Nodes.toN3(nx));
//		}
//		System.out.println("====");
		
		for(Node[] nx : results) {
			System.out.println(Nodes.toN3(nx));
		}
		
	}
	
	List<String> generate(String s, int i) {
		List<String> services = new LinkedList<String>();
		if (i < N) {
			if (i > 0) {	
				String service = "CONSTRUCT { ?val <http://example.org/c" + "a" + s + "a" +i + "> ?o }" +
								 "FROM <http://geowrap.openlids.org/count/c" + "a" + s + "a" +i + ">" +
								 "WHERE {" +
								 ((i == 1) ? " ?val <http://example.org/pred-0> ?val1 ." : "  ?val <http://example.org/c" + s + "> ?val1 .") +
								 "  ?val <http://example.org/pred-" + i + "> ?val2 ." + 
								 "}";
				services.add(service);
				//System.out.println(s);
				s = "a" + s + "a" +i;

			} else {
				s = s+i;
			}

			i++;
			services.addAll(generate(s, i));
		}
		return services;		
	}
	
	String getLastPredicateName(String s, int i) {
		if(i < N) {
			if(i > 0) {
				s = "a" + s + "a" + i;
			} else {
				s = s+i;
			}
			i++;
		} else {
			return "<http://example.org/c" + s + ">";
		}
		return getLastPredicateName(s,i);
	}
}
