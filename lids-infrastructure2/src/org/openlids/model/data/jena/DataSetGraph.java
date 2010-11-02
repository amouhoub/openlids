/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data.jena;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.util.Iterator;
import org.openlids.model.data.DataSet;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class DataSetGraph extends GraphBase {
    private final DataSet dataSet;

    public DataSetGraph(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch tm) {
        Triple t = tm.asTriple();

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

        Iterator<Node[]> results = dataSet.match(bgp);

        return new MatchIterator(results);
    }

}
