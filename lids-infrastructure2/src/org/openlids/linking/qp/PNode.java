/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;

/**
 *
 * @author ssp
 */
public class PNode extends ReteNode {
    Node[] fields;

    public void setFields(Node[] fields) {
        this.fields = fields;
    }

    public void leftActivation(Node[] token) {
        System.out.println(Nodes.toN3(token));
    }

    @Override
    public void addChild(ReteNode aThis) {
        System.err.println("PNode addChild!");
        children.add(aThis);
    }
}