package org.openlids.query.arq;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.util.iterator.NullIterator;

public class DatasetGraphWeb implements DatasetGraph {
	Graph _g;
	
	public DatasetGraphWeb(GraphWeb g) {
		_g = g;
	}

	public void close() {
		_g.close();	
	}

	public boolean containsGraph(Node graphNode) {
		return false;
	}

	public Graph getDefaultGraph() {
		return _g;
	}

	public Graph getGraph(Node graphNode) {
		return null;
	}

	public Lock getLock() {
		return null;
	}

	public Iterator<Node> listGraphNodes() {
		return NullIterator.instance ();
	}

	public int size() {
		return 0;
	}
}
