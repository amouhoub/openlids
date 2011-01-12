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
        if( !aThis.getClass().toString().equals("class org.openlids.linking.qp.LIDSPNode") && !aThis.getClass().toString().equals("class org.openlids.linking.qp.LIDSPNode$1")) {
           // System.out.println("Partent: " + aThis.parent);
           // System.out.println("A subclass of " + aThis.getClass().getSuperclass());
           // System.err.println("AH " + aThis.getClass());
         //   throw new UnsupportedOperationException("Can I get a stacktrace?");
        }
        children.add(aThis);
    }
}
