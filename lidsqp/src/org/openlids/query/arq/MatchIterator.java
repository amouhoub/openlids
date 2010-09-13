package org.openlids.query.arq;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.Map1;
import com.ontologycentral.ldspider.Main;

public class MatchIterator implements ExtendedIterator<Triple> {
	private final static Logger _log = Logger.getLogger(Main.class.getSimpleName());

	Iterator<Node[]> _it;
	
	public MatchIterator(Iterator<Node[]> it) {
		_it = it;
	}
	
	public boolean hasNext() {
		return _it.hasNext();
	}

	public Triple next() {
		Node[] nx = _it.next();
		com.hp.hpl.jena.graph.Node[] jena = new com.hp.hpl.jena.graph.Node[3];
		
		for (int i = 0; i < 3; i++) {
			if (nx[i] instanceof Resource) {
				jena[i] = com.hp.hpl.jena.graph.Node.createURI(((Resource)nx[i]).toString());
			} else if (nx[i] instanceof Literal) {
				jena[i] = com.hp.hpl.jena.graph.Node.createLiteral(((Literal)nx[i]).toString());
			} else if (nx[i] instanceof BNode) {
				jena[i] = com.hp.hpl.jena.graph.Node.createAnon(new AnonId(((BNode)nx[i]).toString()));
			} else {
				_log.info("@!!!!!!!!!!!!! null for " + nx);
			}
		}
		
		Triple t = new Triple(jena[0], jena[1], jena[2]);
		
		return t;
	}
	
	public <X extends Triple> ExtendedIterator<Triple> andThen(Iterator<X> arg0) {
		_log.info("andThem");
		throw new UnsupportedOperationException();
	}

	public ExtendedIterator<Triple> filterDrop(Filter<Triple> arg0) {
		_log.info("filterDrop");
		throw new UnsupportedOperationException();
	}

	public ExtendedIterator<Triple> filterKeep(Filter<Triple> arg0) {
		_log.info("filterKeep");
		throw new UnsupportedOperationException();
	}

	public <U> ExtendedIterator<U> mapWith(Map1<Triple, U> arg0) {
		_log.info("mapWith");
		throw new UnsupportedOperationException();
	}

	public Triple removeNext() {
		_log.info("removeNext");
		throw new UnsupportedOperationException();
	}

	public List<Triple> toList() {
		_log.info("toList");
		throw new UnsupportedOperationException();
	}

	public Set<Triple> toSet() {
		_log.info("toSet");
		throw new UnsupportedOperationException();
	}

	public void close() {
		;
	}

	public void remove() {
		_log.info("remove");
		throw new UnsupportedOperationException();
	}
}