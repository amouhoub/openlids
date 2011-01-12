/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

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
public class BetaMemory extends ReteNode {

    Set<Node[]> tokens = new TreeSet<Node[]>(NodeComparator.NC);

    public void leftActivation(Node[] token) {
        if (tokens.add(token)) {
            for (ReteNode child : children) {
                child.leftActivation(token);
            }
        }
    }

    List<Node[]> get(int posBMem, Node node) {
        List<Node[]> results = new LinkedList<Node[]>();
        for(Node[] token : tokens) {
            if(token[posBMem].equals(node)) {
                results.add(token);
            }
        }
        return results;
    }

    @Override
    public void addChild(ReteNode aThis) {
        for(Node[] token : tokens) {
            aThis.leftActivation(token);
        }
        children.add(aThis);
    }

    void setParent(ReteNode parent) {
        this.parent = parent;
    }
}
