package org.openlids.query;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.NumericLiteral;
import org.semanticweb.yars.nx.Resource;

public class BenchmarkTest extends TestCase {
	int N = 5;
	
	public void testCreateData() {
		for (int i = 0; i < N; i++) {
			Node[] nx = new Node[] { new Resource("http://example.org/resource-" + i), new Resource("http://example.org/pred-" + i), new NumericLiteral(i) };
			
			System.out.println(Nodes.toN3(nx));
		}
		
//		for (int i = 0; i < N; i++) {
//			System.out.println("http://example.org/pred-" + i + " " + i+1);
//		}
		
		int i = 0;
		String str = new String();
		generate(str, i);
	}
	
	void generate(String s, int i) {
		if (i < N) {
			if (i > 0) {	
				System.out.println("CONSTRUCT { ?s <http://example.org/pred-" + "(" + s + ")" +i + "> ?val }");
				System.out.println("FROM <http://geowrap.openlids.org/count/c" + "(" + s + ")" +i + ">");
				System.out.println("WHERE {");
				System.out.println("  ?s <http://example.org/pred-" + s + "> ?val1 .");
				System.out.println("  ?s <http://example.org/pred-" + i + "> ?val2 .");
				System.out.println("}");
				//System.out.println(s);
				s = "(" + s + ")" +i;

			} else {
				s = s+i;
			}

			i++;
			generate(s, i);
		}
		
		//return in;
	}
}
