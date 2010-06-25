package com.ontologycentral.webquery.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class GraphStream extends GraphBase {
	Collection<Node[]> _data;
	
	public GraphStream(Collection<Node[]> data) {
		_data = data;
	}
	
	protected ExtendedIterator<Triple> graphBaseFind(TripleMatch tm) {
		Triple t = tm.asTriple();

		//System.out.println(t);
		
		Node[] bgp = new Node[3];
		
		if (t.getSubject().isURI()) {
			bgp[0] = new Resource(t.getSubject().getURI());
		} else if (t.getSubject().isBlank()) {
			bgp[0] = new BNode(t.getSubject().getBlankNodeLabel());
		} else if (t.getSubject().isVariable()) {
			bgp[0] = new Variable(t.getSubject().getName());
		}
		
		if (t.getPredicate().isURI()) {
			bgp[1] = new Resource(t.getPredicate().getURI());
		} else if (t.getPredicate().isVariable()) {
			bgp[1] = new Variable(t.getPredicate().getName());
		}
		
		if (t.getObject().isURI()) {
			bgp[2] = new Resource(t.getObject().getURI());
		} else if (t.getObject().isVariable()) {
			bgp[2] = new Variable(t.getObject().getName());
		}

		//System.out.println("evaluating " + bgp[0] + " " + bgp[1] + " " + bgp[2]);

		List<Node[]> results = new ArrayList<Node[]>();
		
		for (Node[] nx : _data) {
			if (match(bgp, nx)) {
				results.add(nx);
			}
		}
		
		return new MatchIterator(results.iterator());
	}
	
	static boolean match(Node[] bgp, Node[] nx) {
		for (int i = 0; i < bgp.length; i++) {
			Node n = bgp[i];
			if (n != null) { // null == variable ; !(n instanceof Variable)) {
				if (!n.equals(nx[i])) {
					return false;
				}
			}
		}

		return true;
	}
}
