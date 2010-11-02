/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlids.model.data.Query;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class QueryJena extends Query {

    com.hp.hpl.jena.query.Query jenaQuery;
    private Map<com.hp.hpl.jena.graph.Node,Variable> varMap = new HashMap<com.hp.hpl.jena.graph.Node,Variable>();
    private ArrayList<Node> headVars;

    public String toString() {
        return jenaQuery.toString();
    }

    public void setJenaQuery(com.hp.hpl.jena.query.Query jenaQuery) {
        this.jenaQuery = jenaQuery;
    }

    public com.hp.hpl.jena.query.Query getJenaQuery() {
        return jenaQuery;
    }

    public void addMap(com.hp.hpl.jena.graph.Node v, Variable hv) {
        varMap.put(v,hv);
    }

    public Variable getMapping(com.hp.hpl.jena.graph.Node v) {
        return varMap.get(v);
    }

    public void setHeadVars(List<Node> head) {
        headVars = new ArrayList<Node>();
        headVars.addAll(head);
    }

    public List<Node> getHeadVars() {
        return headVars;
    }

    

}
