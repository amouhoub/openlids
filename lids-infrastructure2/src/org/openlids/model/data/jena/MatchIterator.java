/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data.jena;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.Map1;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

/**
 *
 * @author ssp
 */
class MatchIterator implements ExtendedIterator<Triple> {
    Iterator<Node[]> _it;

    public MatchIterator(Iterator<Node[]> _it) {
        this._it = _it;
    }

    public boolean hasNext() {
        return _it.hasNext();
    }

    public Triple next() {
        Node[] nx = _it.next();
        com.hp.hpl.jena.graph.Node[] jena = new com.hp.hpl.jena.graph.Node[3];

        for (int i = 0; i < 3; i++) {
            if (nx[i] instanceof Resource) {
                jena[i] = com.hp.hpl.jena.graph.Node.createURI(((Resource) nx[i]).toString());
            } else if (nx[i] instanceof Literal) {
                jena[i] = com.hp.hpl.jena.graph.Node.createLiteral(((Literal) nx[i]).toString());
            } else if (nx[i] instanceof BNode) {
                jena[i] = com.hp.hpl.jena.graph.Node.createAnon(new AnonId(((BNode) nx[i]).toString()));
            } else {
                Logger.getLogger(MatchIterator.class.toString()).log(Level.INFO, "@!!!!!!!!!!!!! null for {0}", nx);
            }
		}

		Triple t = new Triple(jena[0], jena[1], jena[2]);

		return t;
	}

	public <X extends Triple> ExtendedIterator<Triple> andThen(Iterator<X> arg0) {
            throw new UnsupportedOperationException();
	}

	public ExtendedIterator<Triple> filterDrop(Filter<Triple> arg0) {
            throw new UnsupportedOperationException();
	}

	public ExtendedIterator<Triple> filterKeep(Filter<Triple> arg0) {
            throw new UnsupportedOperationException();
	}

	public <U> ExtendedIterator<U> mapWith(Map1<Triple, U> arg0) {
            throw new UnsupportedOperationException();
	}

	public Triple removeNext() {
            throw new UnsupportedOperationException();
	}

	public List<Triple> toList() {
            throw new UnsupportedOperationException();
	}

	public Set<Triple> toSet() {
            throw new UnsupportedOperationException();
	}

	public void close() {
	}

	public void remove() {
            throw new UnsupportedOperationException();
	}

}
