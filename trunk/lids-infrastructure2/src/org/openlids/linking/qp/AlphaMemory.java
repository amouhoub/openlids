/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;

/**
 *
 * @author ssp
 */
public
class AlphaMemory {

    Set<Node[]> triples = new TreeSet<Node[]>(NodeComparator.NC);
    Set<ReteNode> successors = new HashSet<ReteNode>();

    public void add(Node[] triple) {
        if(triples.add(triple)) {
            for(ReteNode jn : successors) {
                ((JoinNode) jn).rightActivation(triple);
            }
        }
    }

    List<Node[]> get(int posAMem, Node node) {
        List<Node[]> results = new LinkedList<Node[]>();
        for(Node[] triple : triples) {
            if(triple[posAMem].equals(node)) {
                results.add(triple);
            }
        }
        return results;
    }

    void addChild(ReteNode aThis) {
        for(Node[] triple : triples) {
            ((JoinNode) aThis).rightActivation(triple);
        }
        successors.add(aThis);
    }

    public Set<Node[]> getTriples() {
        return triples;
    }

}