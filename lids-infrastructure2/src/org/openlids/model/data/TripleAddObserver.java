/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data;

import java.util.Set;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public abstract class TripleAddObserver {

    public abstract void notifyAddTriple(DataSet dataSet, Node[] nx);


    public void notifyAddTriples(DataSet dataSet, Set<Node[]> triples) {
        for(Node[] nx : triples) {
            notifyAddTriple(dataSet, nx);
        }
    }
}
