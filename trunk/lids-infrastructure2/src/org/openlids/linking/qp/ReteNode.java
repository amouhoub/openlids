/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.HashSet;
import java.util.Set;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public abstract class ReteNode {
    Set<ReteNode> children = new HashSet<ReteNode>();
    ReteNode parent;

    public abstract void leftActivation(Node[] token);

    void addChild(ReteNode aThis) {
        children.add(aThis);
    }
}
