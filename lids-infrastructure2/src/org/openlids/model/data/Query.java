/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data;

import java.util.ArrayList;
import java.util.List;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public abstract class Query {
    protected ArrayList<Node> headVars = new ArrayList<Node>();


    public void setHeadVars(List<Node> head) {
        headVars = new ArrayList<Node>();
        headVars.addAll(head);
    }

    public List<Node> getHeadVars() {
        return headVars;
    }

    public void addHeadVar(Variable hv) {
        headVars.add(hv);
    }

}
