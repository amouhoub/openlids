/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data;

import java.util.Set;
import java.util.TreeSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;

/**
 *
 * @author ssp
 */
public abstract class TripleTransformer {
    
    public abstract Node[] transformTriple(DataSet dataSet, Node[] nx);


    public Set<Node[]> transformTriples(DataSet dataSet, Set<Node[]> triples) {
        TreeSet<Node[]> results = new TreeSet<Node[]>(NodeComparator.NC);
        for(Node[] nx : triples) {
            results.add(transformTriple(dataSet, nx));
        }
        return results;
    }
}
