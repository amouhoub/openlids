package org.openlids.join;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.GEO;
import org.semanticweb.yars.nx.parser.NxParser;

public class WindowJoinTest extends TestCase {
	public static int MAX_CAPACITY = 10000;
	
	public void testJoin() throws Exception {
		Map<Node, Literal> lats = new HashMap<Node, Literal>();
		Map<Node, Literal> longs = new HashMap<Node, Literal>();
		
//		ArrayQueue<Node> latsq = new ArrayQueue<Node>(MAX_CAPACITY);
//		ArrayQueue<Node> longsq = new ArrayQueue<Node>(MAX_CAPACITY);
		
		FileInputStream fin = new FileInputStream("files/ssp-foaf.nt");

		NxParser nxp = new NxParser(fin);
		
		while (nxp.hasNext()) {
			Node[] nx = nxp.next();
			
			if (GEO.LAT.equals(nx[1])) {
				if (nx[2] instanceof Literal) {
					lats.put(nx[0], (Literal)nx[2]);
				}
				
				if (longs.containsKey(nx[0])) {
					System.out.println("lat " + nx[2] + " long " + longs.get(nx[0]));					
				}
			} else if (GEO.LONG.equals(nx[1])) {
				if (nx[2] instanceof Literal) {
					longs.put(nx[0], (Literal)nx[2]);
				}

				if (lats.containsKey(nx[0])) {
					System.out.println("lat " + lats.get(nx[0]) + " long " + nx[2]);					
				}
			}
		}
	}
}
