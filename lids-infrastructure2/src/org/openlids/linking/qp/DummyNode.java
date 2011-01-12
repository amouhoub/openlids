/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.LinkedList;
import java.util.List;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public class DummyNode extends BetaMemory {
    @Override
    List<Node[]> get(int posBMem, Node node) {
        List<Node[]> result = new LinkedList<Node[]>();
        result.add(new Node[0]);
        return result;
    }

    @Override
    public void addChild(ReteNode aThis) {
        if (aThis instanceof JoinNode) {
            ((JoinNode) aThis).leftActivation();
        }
        children.add(aThis);
    }
}